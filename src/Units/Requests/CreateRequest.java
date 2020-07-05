package Units.Requests;

import java.io.Serializable;

public class CreateRequest implements Serializable {
	private String name;

	public CreateRequest(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
