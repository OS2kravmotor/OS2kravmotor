package dk.digitalidentity.re.dao.model.enums;

import lombok.Getter;

@Getter
public enum SolutionType {
	NOT_SET("enum.solutiontype.notset"),
	SAAS("enum.solutiontype.saas"),
	ON_PREMISE("enum.solutiontype.onpremise"),
	BOTH("enum.solutiontype.both");
	
	private String message;
	
	private SolutionType(String message) {
		this.message = message;
	}
}
