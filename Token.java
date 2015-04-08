import java.io.*;
import java.util.*;

public class Token {

	private String name;

	public Token() {
		name = "";
	}

	public Token(String name) {
		this.name = name;

		// System.out.println("Found " + name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}