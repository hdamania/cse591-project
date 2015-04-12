package swm.smarthome.data;

public enum States {
	ACTIONS("action",0), OTHER("other",1), DEVICE("device",2), LOCATION("location",3);
	private final String value;
	private final int index;

	States(String v, int i) {
		value = v;
		index = i;
	}

	public String value() {
		return value;
	}
	
	public int index() {
		return index;
	}

	public static States fromValue(String v) {
		for (States c : States.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}
	
}
