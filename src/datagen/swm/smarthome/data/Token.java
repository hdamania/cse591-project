package swm.smarthome.data;

public class Token {

	private String name;

	public Token() {
		name = "";
	}

	public Token(String name) {
		this.name = name;

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}