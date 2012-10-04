package lightbot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.pircbotx.PircBotX;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

public class Main {

	private static List<LightBot> lbots = new ArrayList<LightBot>();

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("-----Lightbot by SmallDeadGuy-----");
		File file = new File("logs");
		if(!file.exists() || !file.isDirectory())
			file.mkdir();
		
		System.out.println("Loading config.json");
		Gson gson = new Gson();
		File config = new File("config.json");
		String json = "";
		if(config.exists()) {
			System.out.println("Config found, reading json");
			BufferedReader reader = new BufferedReader(new FileReader(config));
			String line = "";
			while((line = reader.readLine()) != null)
				json += (json.isEmpty() ? "" : "\n") + line;
			reader.close();
			
			BotProperties[] bots = gson.fromJson(json, BotProperties[].class);
			for(BotProperties bot : bots) {
				System.out.println("Creating bot " + bot.nick + " on server " + bot.server);
				LightBot lbot = new LightBot(bot);
				lbots.add(lbot);
			}
		}
		else {
			System.out.println("No config found, using default");
			BotProperties bot = new BotProperties();
			LightBot lbot = new LightBot(bot);
			lbots.add(lbot);
			
			json = gson.toJson(new BotProperties[] {bot}, BotProperties[].class);
			FileWriter writer = new FileWriter(config);
			writer.write(json);
			writer.close();
		}
	}
	
	public static void saveConfig() {
		System.out.println("Saving updated config");
		Gson gson = new Gson();
		List<BotProperties> props = new ArrayList<BotProperties>();
		for(LightBot lbot : lbots)
			props.add(lbot.config);
		String json = gson.toJson(props.toArray(new BotProperties[0]), BotProperties[].class);
		
		File config = new File("config.json");
		FileWriter writer;
		
		try {
			writer = new FileWriter(config);
			writer.write(json);
			writer.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
}

class BotProperties {
	public String server = "irc.esper.net";
	public int port = 6667;
	public String serverkey = "";

	public String nick = "LightBot";
	public String user = "";
	public String userpass = "";

	public String owner = "";
	public String[] admins = {};
	
	public char cmdprefix = '>';
	
	public String[] channels = {};
}
