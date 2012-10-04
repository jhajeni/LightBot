package lightbot.modules;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;

import lightbot.Command;
import lightbot.LightBot;
import lightbot.Module;

public class ModuleErmahgerd extends Module {

	public ModuleErmahgerd() {
		normalCommands.add(new Command("ermahgerd", "ermahgerd <>") {
			@Override
			public boolean validArgs(int num) {
				return num > 0;
			}
		});
	}

	@Override
	public String getName() {
		return "Ermahgerd";
	}

	@Override
	public String getVersion() {
		return "1.0.0";
	}

	@Override
	public void interpretCommand(String command, String[] args, User user, Channel channel, LightBot lbot, PircBotX bot, String argsUnparsed) {
		if(command.equalsIgnoreCase("ermahgerd")) { 
			String out = "";
			for(String word : args)
				out += (out.isEmpty() ? "" : " ") + ermahgerd(word);
			lbot.respond(user, channel, out);
		}
	}

	public String ermahgerd(String word) {
		word = word.toUpperCase();
		
		if(word.length() == 1)
			return word;
		
		switch(word) {
			case "AWESOME":			return "ERSUM";
			case "BANANA":			return "BERNERNER";
			case "BAYOU":			return "BERU";
			case "FAVORITE":
			case "FAVOURITE":		return "FRAVRIT";
			case "GOOSEBUMPS":		return "GERSBERMS";
			case "LONG":			return "LERNG";
			case "MY":				return "MAH";
			case "THE":				return "DA";
			case "THEY":			return "DEY";
			case "WE\"RE":			return "WER";
			case "YOU":				return "U";
			case "YOU\"RE":			return "YER";
		}

		String originalWord = word;

		if(originalWord.length() > 2)
			word = word.replaceAll("[AEIOU]$", "");

		word = word.replaceAll("[^\\w\\s]|(.)(?=\\1)", "");

		word = word.replaceAll("[AEIOUY]{2,}", "E");

		word = word.replaceAll("OW", "ER");

		word = word.replaceAll("AKES", "ERKS");

		word = word.replaceAll("[AEIOUY]", "ER");

		word = word.replaceAll("ERH", "ER");

		word = word.replaceAll("MER", "MAH");

		word = word.replaceAll("ERNG", "IN");

		word = word.replaceAll("ERPERD", "ERPED");

		word = word.replaceAll("MAHM", "MERM");

		if(originalWord.charAt(0) == 'Y')
			word = "Y" + word;

		word = word.replaceAll("[^\\w\\s]|(.)(?=\\1)", "");

		if(originalWord.length() > 2 && word.length() > 2 && originalWord.substring(originalWord.length() - 3).equals("LOW") && word.substring(word.length() - 3).equals("LER"))
			word = word.substring(0, word.length() - 4) + "LO";

		return word;
	}
}
