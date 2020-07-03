package Units;

public class User {
	private int ID;
	private String login;
	private String password;
	private int hash;

	User(String login, String password) {
		this.login = login;
		this.password = password;
		this.ID = UID.generate();
		this.hash = this.password.hashCode();
	}

	public int getID() {
		return ID;
	}

	public String getLogin() {
		return login;
	}

	public int getHash() {
		return hash;
	}

	public String getPassword() {
		return password;
	}
}
