package lightbot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
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
		Gson gson = new Gson();
		File config = new File("config.json");
		String json = "";
		if(config.exists()) {
			BufferedReader reader = new BufferedReader(new FileReader(config));
			String line = "";
			while((line = reader.readLine()) != null)
				json += (json.isEmpty() ? "" : "\n") + line;
			reader.close();
			
			BotProperties[] bots = gson.fromJson(json, BotProperties[].class);
			for(BotProperties bot : bots) {
				LightBot lbot = new LightBot(bot);
				lbots.add(lbot);
			}
		}
		else {
			BotProperties bot = new BotProperties();
			LightBot lbot = new LightBot(bot);
			lbots.add(lbot);
			
			json = gson.toJson(new BotProperties[] {bot}, BotProperties[].class);
			FileWriter writer = new FileWriter(config);
			writer.write(json);
			writer.close();
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
	
	public boolean log = true;
}
