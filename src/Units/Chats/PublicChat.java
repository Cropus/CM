package Units.Chats;

import Units.Message;
import Units.UID;
import Units.User;

import java.util.ArrayList;

public class PublicChat extends Chat{
	private int ID;
	private String name;
	private ArrayList<Message> messages = new ArrayList<>();

	public PublicChat(String name) {
		this.ID = UID.generate();
		this.name = name;
	}

	public void addMessage(Message blank) {
		messages.add(blank);
	}

	public int getID() {
		return ID;
	}

	public String getName() {
		return name;
	}

	public ArrayList<Message> getMessages() {
		return messages;
	}
}
