package lightbot.modules;

import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;

import lightbot.Command;
import lightbot.LightBot;
import lightbot.Module;

public class ModuleSay extends Module {
	
	public ModuleSay() {
		normalCommands.add(new Command("say", "say <>") {
			@Override
			public boolean validArgs(int num) {
				return num > 0;
			}
		});
		normalCommands.add(new Command("do", "do <>") {
			@Override
			public boolean validArgs(int num) {
				return num > 0;
			}
		});
	}

	@Override
	public String getName() {
		return "Say";
	}

	@Override
	public String getVersion() {
		return "1.0.0";
	}

	@Override
	public void interpretCommand(String command, String[] args, User user, Channel channel, LightBot lbot, PircBotX bot, String argsUnparsed) {
		if(command.equalsIgnoreCase("say"))
			lbot.respond(user, channel, argsUnparsed);
		if(command.equalsIgnoreCase("do"))
			lbot.action(user, channel, argsUnparsed);
	}
}
