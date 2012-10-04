package lightbot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import org.pircbotx.hooks.Event;
import  org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.NoticeEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;

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
	
	private File logFile;

	public LightBot(BotProperties props) throws Exception {
		Calendar cl = Calendar.getInstance();
		logFile = new File("logs" + File.separator + props.nick + "_" + props.server + "_" + cl.get(Calendar.DAY_OF_MONTH) + "-" + cl.get(Calendar.MONTH) + "_" + cl.get(Calendar.HOUR) + "-" + cl.get(Calendar.MINUTE) + ".log");
		if(!logFile.exists()) logFile.createNewFile();
		
		log("-----Initializing bot-----");
		bot = new PircBotX();
		bot.getListenerManager().addListener(this);

		log("Nick: " + props.nick);
		bot.setName(props.nick);
		bot.setAutoNickChange(true);
		
		config = props;
		
		if(!config.server.isEmpty()) {
			if(config.port > 0) {
				if(!config.serverkey.isEmpty()) {
					log("Connecting to server: " + config.server + ":" + config.port + " " + config.serverkey);
					bot.connect(config.server, config.port, config.serverkey);
				}
				else {
					log("Connecting to server: " + config.server + ":" + config.port);
					bot.connect(config.server, config.port);
				}
			}
			else {
				log("Connecting to server: " + config.server);
				bot.connect(config.server);
			}
		}

		if(!config.user.isEmpty() && !config.userpass.isEmpty()) {
			log("Login: " + config.user + " " + config.userpass);
			bot.setLogin(config.user);
			bot.identify(config.userpass);
		}

		log("Command Prefix: " + config.cmdprefix);
		cmdPrefix = config.cmdprefix;

		log("Owner: " + config.owner);
		owner = config.owner;
		for(String admin : config.admins) admins.add(admin);
		
		String chans = "";
		for(String channel : config.channels) {
			chans += (chans.isEmpty() ? "" : ", ") + channel;
			bot.joinChannel(channel);
		}
		log("Joined channels: " + chans);

		loginHandler = new LoginHandler(bot);

		//TODO: proper module loader
		log("Loaded basic module");
		modules.add(new ModuleBasics());
		ModuleLoader loader = new ModuleLoader();
		URL modulePath = LightBot.class.getResource("LightBot.class");
		File lbotDir = new File(modulePath.getPath()).getParentFile();
		if(lbotDir.isDirectory())
			for(File f : lbotDir.listFiles())
				if(f.getName().equalsIgnoreCase("modules") && f.isDirectory()) {
					List<Module> ms = loader.loadAllModules(f);
					for(Module m : ms) {
						log("Loading module: " + m.getName() + " (" + m.getVersion() + ")");
						modules.add(m);
					}
				}
		
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
	
	public void interpretMessage(String message, User user, Channel channel, Event event) {
		if(message != null && message.startsWith(Character.toString(cmdPrefix)) && message.length() > 1) {
			String unparsed = message.substring(1);
			
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
					
					if(!isOwner(user)) {
						event.respond("You must be the bot owner to perform command '" + cmd + "'");
						return;
					}
				}
				else {
					cmds = m.getAdminCommands();
					if(cmds.contains(c)) {
						c = cmds.get(cmds.indexOf(c));
						
						if(!isAdmin(user)) {
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
						m.interpretCommand(cmd, args, user, channel, this, bot, argsUnparsed);
					else
						event.respond(cmdPrefix + c.getHelp());
				}
			}
		}
	}

	@Override
	public void onPrivateMessage(PrivateMessageEvent event) {
		interpretMessage(event.getMessage(), event.getUser(), null, event);
	}
	
	@Override
	public void onMessage(MessageEvent event) {
		interpretMessage(event.getMessage(), event.getUser(), event.getChannel(), event);
	}
	
	@Override
	public void onNotice(NoticeEvent event) {
		interpretMessage(event.getMessage(), event.getUser(), event.getChannel(), event);
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
	
	public void log(String message) {
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(logFile, true));
			writer.append(message);
			writer.newLine();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
