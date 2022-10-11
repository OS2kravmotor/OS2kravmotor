package dk.digitalidentity.re.dao.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum JIRASprintState {
	CLOSED, ACTIVE, FUTURE;

	@JsonCreator
	public static JIRASprintState getEnumFromValue(String value) {
		for (JIRASprintState statusEnum : values()) {
			if (statusEnum.toString().toLowerCase().equals(value)) {
				return statusEnum;
			}
		}

		throw new IllegalArgumentException();
	}
	
	@Override
	public String toString() {
		return super.toString().toLowerCase();
	}
}
