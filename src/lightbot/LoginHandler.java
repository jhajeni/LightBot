package lightbot;

import java.util.HashMap;
import java.util.Map;

import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.NickChangeEvent;
import org.pircbotx.hooks.events.QuitEvent;
import org.pircbotx.hooks.events.ServerResponseEvent;
import org.pircbotx.hooks.events.UserListEvent;

public class LoginHandler extends ListenerAdapter {
	
	private Map<String, String> logins = new HashMap<String, String>();
	
	private boolean finished = false;
	private String user;
	
	private PircBotX bot;
	
	public LoginHandler(PircBotX b) {
		bot = b;
		bot.getListenerManager().addListener(this);
	}
	
	public String getLogin(User user) {
		return getLogin(user.getNick());
	}
	
	public String getLogin(String user) {
		if(!logins.containsKey(user)) {
			bot.sendRawLineNow("WHOIS " + user);
			finished = false;
			this.user = user;
			while(!finished)
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
		if(logins.containsKey(user)) return (String) logins.get(user);
		return null;
	}
	
	@Override
	public void onServerResponse(ServerResponseEvent event) {
		if (event.getCode() == 330) {
			logins.put(user, event.getResponse().split(" ")[2]);
			finished = true;
		}
	}
	
	@Override
	public void onNickChange(NickChangeEvent event) {
		if(logins.containsKey(event.getOldNick())) {
			String login = logins.remove(event.getOldNick());
			logins.put(event.getNewNick(), login);
		}
	}
	
	@Override
	public void onQuit(QuitEvent event) {
		if(logins.containsKey(event.getUser().getNick()))
			logins.remove(event.getUser().getNick());
	}
}
