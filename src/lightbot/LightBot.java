package lightbot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.exception.IrcException;
import org.pircbotx.exception.NickAlreadyInUseException;
import  org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

public class LightBot extends ListenerAdapter {
	
	private boolean log;
	
	public String owner;
	public List<String> admins = new ArrayList<String>();

	private PircBotX bot;

	private char cmdPrefix;

	public LoginHandler loginHandler;

	private List<Module> modules = new ArrayList<Module>();
	private Map<String, Module> commandCache = new HashMap<String, Module>();

	public LightBot(BotProperties props) throws Exception {
		bot = new PircBotX();
		bot.getListenerManager().addListener(this);

		bot.setName(props.nick);
		bot.setAutoNickChange(true);
		
		if(!props.server.isEmpty()) {
			if(props.port > 0) {
				if(!props.serverkey.isEmpty()) bot.connect(props.server, props.port, props.serverkey);
				else bot.connect(props.server, props.port);
			}
			else bot.connect(props.server);
		}

		if(!props.user.isEmpty() && !props.userpass.isEmpty()) {
			bot.setLogin(props.user);
			bot.identify(props.userpass);
		}

		cmdPrefix = props.cmdprefix;
		log = props.log;

		owner = props.owner;
		for(String admin : props.admins) admins.add(admin);
		
		for(String channel : props.channels)
			bot.joinChannel(channel);

		loginHandler = new LoginHandler(bot);

		//TODO: proper module loader
		modules.add(new ModuleBasics(bot, this));

		for(Module m : modules) {
			for(Command c : m.getNormalCommands()) {
				if(c == null) continue;
				if(commandCache.containsKey(c.getName())) {
					Module m2 = commandCache.get(c.getName());
					System.out.println("Command '" + c.getName() + "' conflict between modules " + m.getName() + " (" + m.getVersion() + ") and " + m2.getName() + " (" + m2.getVersion() + "). Using command from " + m2.getName() + " (" + m2.getVersion() + ")");
					continue;
				}
				commandCache.put(c.getName(), m);
			}
			for(Command c : m.getAdminCommands()) {
				if(c == null) continue;
				if(commandCache.containsKey(c.getName())) {
					Module m2 = commandCache.get(c.getName());
					System.out.println("Command '" + c.getName() + "' conflict between modules " + m.getName() + " (" + m.getVersion() + ") and " + m2.getName() + " (" + m2.getVersion() + "). Using command from " + m2.getName() + " (" + m2.getVersion() + ")");
					continue;
				}
				commandCache.put(c.getName(), m);
			}
			for(Command c : m.getOwnerCommands()) {
				if(c == null) continue;
				if(commandCache.containsKey(c.getName())) {
					Module m2 = commandCache.get(c.getName());
					System.out.println("Command '" + c.getName() + "' conflict between modules " + m.getName() + " (" + m.getVersion() + ") and " + m2.getName() + " (" + m2.getVersion() + "). Using command from " + m2.getName() + " (" + m2.getVersion() + ")");
					continue;
				}
				commandCache.put(c.getName(), m);
			}
		}
	}

	public boolean isAdmin(User user) {
		return isOwner(user) || admins.contains(loginHandler.getLogin(user));
	}

	public boolean isOwner(User user) {
		return owner.equals(loginHandler.getLogin(user));
	}

	@Override
	public void onMessage(MessageEvent event) {
		if(event.getMessage() != null && event.getMessage().startsWith(Character.toString(cmdPrefix)) && event.getMessage().length() > 1) {
			String unparsed = event.getMessage().substring(1);
			
			String cmd = "";
			String argsUnparsed = "";
			
			Pattern pattern = Pattern.compile("(.+)\\s+(.+)");
			Matcher matcher = pattern.matcher(unparsed);
			if(matcher.matches()) {
				cmd = matcher.group(1).toLowerCase();
				argsUnparsed = matcher.group(2);
			}
			else {
				pattern = Pattern.compile("(.+)\\s*");
				matcher = pattern.matcher(unparsed);
				if(matcher.matches())
					cmd = matcher.group(1).toLowerCase();
			}
			
			
			if(commandCache.containsKey(cmd)) {
				Module m = commandCache.get(cmd);
				Command c = new CommandArgsFixed(cmd, "", 0);

				boolean found = true;
				List<Command> cmds = m.getOwnerCommands();
				if(cmds.contains(c)) {
					c = cmds.get(cmds.indexOf(c));
					
					if(!isOwner(event.getUser())) {
						event.respond("You must be the bot owner to perform command '" + cmd + "'");
						return;
					}
				}
				else {
					cmds = m.getAdminCommands();
					if(cmds.contains(c)) {
						c = cmds.get(cmds.indexOf(c));
						
						if(!isAdmin(event.getUser())) {
							event.respond("You must have admin privileges to perform command '" + cmd + "'");
							return;
						}
					}
					else {
						cmds = m.getNormalCommands();
						if(cmds.contains(c)) c = cmds.get(cmds.indexOf(c));
						else found = false;
					}
				}
				
				if(found) {
					String[] args = new String[0];
					if(!argsUnparsed.isEmpty()) {
						if(c.doParseQuotes()) {
							//TODO: finish this
						}
						else if(Pattern.matches("(.+\\s+.*|.*\\s+.+)", argsUnparsed))
							args = argsUnparsed.split("\\s+");
						else
							args = new String[] {argsUnparsed};
					}
					
					if(c.validArgs(args.length))
						m.interpretCommand(cmd, args, event.getUser(), event.getChannel());
					else
						event.respond(cmdPrefix + c.getHelp());
				}
			}
		}
	}
}
