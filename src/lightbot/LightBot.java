package lightbot;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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

import com.google.gson.Gson;

public class LightBot extends ListenerAdapter {
	
	public String owner;
	public List<String> admins = new ArrayList<String>();

	private PircBotX bot;

	public char cmdPrefix;

	public LoginHandler loginHandler;

	public List<Module> modules = new ArrayList<Module>();
	private Map<String, Module> commandCache = new HashMap<String, Module>();
	
	public BotProperties config;

	public LightBot(BotProperties props) throws Exception {
		bot = new PircBotX();
		bot.getListenerManager().addListener(this);

		bot.setName(props.nick);
		bot.setAutoNickChange(true);
		
		config = props;
		
		if(!config.server.isEmpty()) {
			if(config.port > 0) {
				if(!config.serverkey.isEmpty()) bot.connect(config.server, config.port, config.serverkey);
				else bot.connect(config.server, config.port);
			}
			else bot.connect(config.server);
		}

		if(!config.user.isEmpty() && !config.userpass.isEmpty()) {
			bot.setLogin(config.user);
			bot.identify(config.userpass);
		}

		cmdPrefix = config.cmdprefix;

		owner = config.owner;
		for(String admin : config.admins) admins.add(admin);
		
		for(String channel : config.channels)
			bot.joinChannel(channel);

		loginHandler = new LoginHandler(bot);

		//TODO: proper module loader
		modules.add(new ModuleBasics());
		ModuleLoader loader = new ModuleLoader();
		URL modulePath = LightBot.class.getResource("LightBot.class");
		File lbotDir = new File(modulePath.getPath()).getParentFile();
		if(lbotDir.isDirectory())
			for(File f : lbotDir.listFiles())
				if(f.getName().equalsIgnoreCase("modules") && f.isDirectory())
					modules.addAll(loader.loadAllModules(f));
		
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
			
			Pattern pattern = Pattern.compile("(.+?)\\s+(.+)");
			Matcher matcher = pattern.matcher(unparsed);
			if(matcher.matches()) {
				cmd = matcher.group(1).toLowerCase();
				argsUnparsed = matcher.group(2);
			}
			else {
				pattern = Pattern.compile("(.+?)\\s*");
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
						m.interpretCommand(cmd, args, event.getUser(), event.getChannel(), this, bot, argsUnparsed);
					else
						event.respond(cmdPrefix + c.getHelp());
				}
			}
		}
	}
	
	public void respond(User user, Channel channel, String message) {
		if(channel != null) bot.sendMessage(channel, message);
		else if(user != null) bot.sendMessage(user, message);
	}
	
	public void action(User user, Channel channel, String action) {
		if(channel != null) bot.sendAction(channel, action);
		else if(user != null) bot.sendAction(user, action);
	}
	
	public void updateConfig(boolean save) {
		config.admins = admins.toArray(new String[0]);
		List<String> channels = new ArrayList<String>();
		for(Channel c : bot.getChannels()) channels.add(c.getName());
		config.channels = channels.toArray(new String[0]);
		config.cmdprefix = cmdPrefix;
		config.nick = bot.getNick();
		config.owner = owner;
		config.user = bot.getLogin();
		config.userpass = bot.getPassword();
		if(save) Main.saveConfig();
	}
}
