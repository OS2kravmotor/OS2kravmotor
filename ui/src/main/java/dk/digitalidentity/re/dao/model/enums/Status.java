package dk.digitalidentity.re.dao.model.enums;

public enum Status {
	DRAFT("enum.status.draft"),
	ACTIVE("enum.status.active"),
	CANCELLED("enum.status.cancelled"),
	COMPLETED("enum.status.completed"),
	ARCHIVED("enum.status.archived");

	private String value;

	private Status(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
