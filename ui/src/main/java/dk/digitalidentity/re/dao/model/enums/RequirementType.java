package dk.digitalidentity.re.dao.model.enums;

import lombok.Getter;

@Getter
public enum RequirementType {
	GLOBAL("html.requirement.requirementtype.shared"),
	LOCAL("html.requirement.requirementtype.local"),
	TOBESHARED("html.requirement.requirementtype.local"),//TODO we probably won't display this in UI
	COMMUNITY("html.requirement.requirementtype.community");
	
	private String message;

	private RequirementType(String message) {
		this.message = message;
	}
};