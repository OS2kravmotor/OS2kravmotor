package dk.digitalidentity.re.dao.model.enums;

public enum CustomerAnswer {
	NONE("enum.customerAnswer.none"),
	ACCEPTED("enum.customerAnswer.accepted"),
	ACCEPTED_WITH_COMMENT("enum.customerAnswer.accepted_with_comment"),
	REJECTED("enum.customerAnswer.rejected"),
	ELABORATION_NEEDED("enum.customerAnswer.elaboration_needed");

	private String value;

	private CustomerAnswer(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
