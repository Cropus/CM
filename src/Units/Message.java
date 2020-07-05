package Units;

import java.io.Serializable;

public class Message implements Serializable {
	private int ID;
	private String text;
	private int from;
	private int to;
	private String name;

	public Message(String text, int to) {
		this.text = text;
		this.to = to;
	}

	public Message(int ID, String text, int from, int to, String name) {
		this.ID = ID;
		this.text = text;
		this.from = from;
		this.to = to;
		this.name = name;
	}

	public void setID(int ID) {
		this.ID = ID;
	}

	public void setFrom(int from) {
		this.from = from;
	}

	public void setName(String name) {
		this.name = name;
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

	public String getName() {
		return name;
	}
}
