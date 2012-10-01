package lightbot;

public class CommandArgsFixed extends Command {
	
	private int args;
	
	public CommandArgsFixed(String name, String help, int num) {
		this(name, help, false, num);
	}
	
	public CommandArgsFixed(String name, String help, boolean parseQuotes, int num) {
		super(name, help, parseQuotes);
		args = num;
	}

	@Override
	public boolean validArgs(int num) {
		return num == args;
	}
}
