package Units.Chats;

import Units.UID;
import Units.User;

import java.util.ArrayList;

public class PublicChat {
	private int ID;
	private String name;
	private ArrayList<User> users = new ArrayList<>();

	PublicChat (String name) {
		this.ID = UID.generate();
		this.name = name;
	}

	public void addUser(User noob) {
		users.add(noob);
	}

	public int getID() {
		return ID;
	}

	public String getName() {
		return name;
	}

	public ArrayList<User> getUsers() {
		return users;
	}
}
