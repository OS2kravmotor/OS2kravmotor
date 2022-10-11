package dk.digitalidentity.re.dao.model.enums;

public enum AnswerChoice {
	YES("enum.answerchoice.yes"),
	YES_AS_AN_ADDON("enum.answerchoice.yes_as_an_addon"),
	YES_PARTIALLY("enum.answerchoice.yes_partially"),
	NO("enum.answerchoice.no"),
	NOT_RELEVANT("enum.answerchoice.not_relevant");
	
	private String displayName;

	private AnswerChoice(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
