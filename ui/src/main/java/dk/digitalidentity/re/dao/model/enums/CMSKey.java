package dk.digitalidentity.re.dao.model.enums;

import lombok.Getter;

@Getter
public enum CMSKey {
	FRONTPAGE("enum.cms.frontpage"),
	HELPPAGE("enum.cms.helppage");

	private String message;

	private CMSKey(String message) {
		this.message = message;
	}
}
