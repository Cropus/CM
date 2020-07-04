package Units;

import java.io.Serializable;

public class Message implements Serializable {
	private int ID;
	private String text;
	private int from;
	private int to;

	public Message(String text, int to) {
		this.text = text;
		this.to = to;
	}

	public void setID(int ID) {
		this.ID = ID;
	}

	public void setFrom(int from) {
		this.from = from;
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
