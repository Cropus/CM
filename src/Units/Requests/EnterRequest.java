package Units.Requests;

import java.io.Serializable;

public class EnterRequest implements Serializable {
	private String chatName;

	public EnterRequest(String chatName) {
		this.chatName = chatName;
	}

	public String getChatName() {
		return chatName;
	}
}
