package Units.Chats;

import Units.UID;

public abstract class Chat {
	private int ID;

	public Chat(int ID) {
		this.ID = ID;
	}

	public Chat(){}

	public int getID() {
		return ID;
	}
}
