package Units;

import java.io.Serializable;

public class Credential implements Serializable {
	private final String login;
	private final String password;
	private final int hash;
	private final boolean type;

	public Credential(String login, String password, boolean type) {
		this.login = login;
		this.password = password;
		this.type = type;
		this.hash = (login + password).hashCode();
	}

	public String getLogin() {
		return login;
	}

	public String getPassword() {
		return password;
	}

	public boolean isReg() {
		return type;
	}

	public int getHash() {
		return hash;
	}
}
