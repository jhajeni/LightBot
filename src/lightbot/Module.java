package lightbot;

import java.util.ArrayList;
import java.util.List;

import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.MessageEvent;

public abstract class Module {

	public Module(PircBotX b, LightBot lb) {
		bot = b;
	}

	protected PircBotX bot;
	protected LightBot lbot;

	public abstract String getName();
	public abstract String getVersion();

	protected List<Command> normalCommands = new ArrayList<Command>();
	protected List<Command> adminCommands = new ArrayList<Command>();
	protected List<Command> ownerCommands = new ArrayList<Command>();

	public List<Command> getNormalCommands() {
		return normalCommands;
	}

	public List<Command> getAdminCommands() {
		return adminCommands;
	}

	public List<Command> getOwnerCommands() {
		return ownerCommands;
	}

	public abstract void interpretCommand(String command, String[] args, User user, Channel channel);
	
	public void respond(User u, Channel c, String message) {
		System.out.println((u != null ? u.getNick() : "") + (c != null ? (u != null ? ", " : "") + c.getName() : "") + (u != null || c != null ? ": " : "") + message);
		if(c != null) {
			if(u != null) bot.sendMessage(c, u.getNick() + ": " + message);
			else bot.sendMessage(c, message);
		}
		else if(u != null) bot.sendMessage(u, message);
	}
}
