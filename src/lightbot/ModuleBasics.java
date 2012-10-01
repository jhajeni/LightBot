package lightbot;

import java.util.ArrayList;
import java.util.List;

import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.MessageEvent;

public class ModuleBasics extends Module {
	
	public ModuleBasics() {
		normalCommands.add(new CommandArgsFixed("getadmins", "getadmins", 0));
		normalCommands.add(new CommandArgsFixed("getowner", "getowner", 0));
		normalCommands.add(new CommandArgsFixed("getchannels", "getchannels", 0));
		
		adminCommands.add(new CommandArgsFixed("join", "join <channel>", 1));
		adminCommands.add(new CommandArgsRange("part", "part (channel)", 0, 1));
		
		ownerCommands.add(new CommandArgsFixed("addadmin", "addadmin <nick>", 1));
		ownerCommands.add(new CommandArgsFixed("deladmin", "deladmin <nick>", 1));
		ownerCommands.add(new CommandArgsFixed("setowner", "setowner <nick>", 1));
	}
	
	@Override
	public String getName() {
		return "Basics";
	}

	@Override
	public String getVersion() {
		return "1.0.0";
	}
	
	@Override
	public void interpretCommand(String command, String[] args, User user, Channel channel, LightBot lbot, PircBotX bot) {
		if(command.equalsIgnoreCase("getadmins")) {
			String admins = "";
			for(String a : lbot.admins) admins += (admins.isEmpty() ? "" : ", ") + a;
			lbot.respond(user, channel, admins.isEmpty() ? "No bot admins" : admins);
		}
		if(command.equalsIgnoreCase("getowner"))
			lbot.respond(user, channel, lbot.owner);
		if(command.equalsIgnoreCase("getchannels")) {
			String channels = "";
			for(Channel c : bot.getChannels()) channels += (channels.isEmpty() ? "" : ", ") + c.getName();
			lbot.respond(user, channel, channels);
		}
		
		if(command.equalsIgnoreCase("join"))
			bot.joinChannel(args[0]);
		if(command.equalsIgnoreCase("part")) {
			if(args.length == 1) {
				boolean parted = false;
				for(Channel c : bot.getChannels())
					if(c.getName().equalsIgnoreCase(args[0])) {
						bot.partChannel(c);
						parted = true;
					}
				if(!parted) lbot.respond(user, channel, "I'm not in channel " + args[0]);
			}
			else if(channel != null)
				bot.partChannel(channel);
			else
				lbot.respond(user, channel, "No channel context to part, specify optional channel argument");
		}
		
		if(command.equalsIgnoreCase("addadmin")) {
			String login = lbot.loginHandler.getLogin(args[0]);
			if(login != null) {
				lbot.respond(user, channel, "Adding " + args[0] + " (" + login + ") as admin");
				lbot.admins.add(login);
			}
			else lbot.respond(user, channel, "User " + args[0] + " not found or not logged in");
		}
		if(command.equalsIgnoreCase("deladmin")) {
			String login = lbot.loginHandler.getLogin(args[0]);
			if(login != null) {
				if(lbot.admins.contains(login)) lbot.admins.remove(login);
				else lbot.respond(user, channel, "User " + args[0] + " is not an admin");
			}
			else lbot.respond(user, channel, "User " + args[0] + " not found or not logged in");
		}
		if(command.equalsIgnoreCase("setowner")) {
			String login = lbot.loginHandler.getLogin(args[0]);
			if(login != null) lbot.owner = login;
			else lbot.respond(user, channel, "User " + args[0] + " not found or not logged in");
		}
	}
}
