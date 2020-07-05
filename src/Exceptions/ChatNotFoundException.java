package Exceptions;

import Units.Requests.ChangeRequest;

import java.io.Serializable;

public class ChatNotFoundException extends Exception implements Serializable {
	private boolean flag;
	private ChangeRequest changeRequest;
	public ChatNotFoundException(boolean flag, ChangeRequest changeRequest) {
		this.flag = flag;
		this.changeRequest = changeRequest;
	}

	public boolean isFlag() {
		return flag;
	}

	public ChangeRequest getChangeRequest() {
		return changeRequest;
	}
}
