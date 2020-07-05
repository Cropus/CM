package Units.Chats;

import Units.UID;
import Units.User;

public class PM extends Chat {
	private int ID;
	private User first;
	private User second;

	PM(User first, User second) {
		super();
		this.ID = UID.generate();
		this.first = first;
		this.second = second;
	}

	public int getID() {
		return ID;
	}

	public User getFirst() {
		return first;
	}

	public User getSecond() {
		return second;
	}
}
