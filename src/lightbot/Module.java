package lightbot;

import java.util.ArrayList;
import java.util.List;

import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.MessageEvent;

public abstract class Module {

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

	public abstract void interpretCommand(String command, String[] args, User user, Channel channel, LightBot lbot, PircBotX bot, String argsUnparsed);
}
