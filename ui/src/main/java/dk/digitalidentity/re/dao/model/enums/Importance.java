package dk.digitalidentity.re.dao.model.enums;

public enum Importance {
	OPTIONAL("enum.importance.optional"),
	NORMAL("enum.importance.normal"),
	HIGH("enum.importance.high"),
	ABSOLUTE("enum.importance.absolute");

	private String value;

	private Importance(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}