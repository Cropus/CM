package Units.Requests;

import java.io.Serializable;

public class ChangeRequest implements Serializable {
	private int chatID;

	public ChangeRequest(int chatID) {
		this.chatID = chatID;
	}

	public int getChatID() {
		return chatID;
	}
}
