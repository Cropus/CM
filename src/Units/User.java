package Units;

import Units.Chats.PM;
import Units.Chats.PublicChat;

import java.util.ArrayList;

public class User {
	private int ID;
	private String login;

	User(String login) {
		this.login = login;
		this.ID = UID.generate();
	}

	public int getID() {
		return ID;
	}

	public String getLogin() {
		return login;
	}

}
