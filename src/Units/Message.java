package Units;

public class Message {
	private int ID;
	private String text;
	private int from;
	private int to;

	public Message(String text, int from, int to) {
		this.ID = UID.generate();
		this.text = text;
		this.from = from;
		this.to = to;
	}

	public int getID() {
		return ID;
	}

	public String getText() {
		return text;
	}

	public int getFrom() {
		return from;
	}

	public int getTo() {
		return to;
	}
}
