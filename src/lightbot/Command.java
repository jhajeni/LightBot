package lightbot;

public abstract class Command {
	private String name;
	private String help;
	private boolean parseQuotes;
	
	public Command(String n, String h) {
		this(n, h, false);
	}
	
	public Command(String n, String h, boolean p) {
		name = n.toLowerCase();
		help = h;
		parseQuotes = p;
	}
	
	public String getName() {
		return name;
	}
	
	public String getHelp() {
		return help;
	}
	
	public boolean doParseQuotes() {
		return parseQuotes;
	}
	
	public abstract boolean validArgs(int num);
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof String) return ((String) obj).equalsIgnoreCase(name);
		if(obj instanceof Command) return ((Command) obj).getName().equalsIgnoreCase(name);
		return false;
	}
}
