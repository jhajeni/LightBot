package lightbot;

public class CommandArgsRange extends Command {
	
	private int min;
	private int max;
	
	public CommandArgsRange(String name, String help, int min, int max) {
		this(name, help, false, min, max);
	}
	
	public CommandArgsRange(String name, String help, boolean parseQuotes, int min, int max) {
		super(name, help, parseQuotes);
		this.min = min;
		this.max = max;
	}

	@Override
	public boolean validArgs(int num) {
		return num >= min && num <= max;
	}

}
