package dk.digitalidentity.re.dao.model.enums;

import lombok.Getter;

@Getter
public enum CustomerSetting {
	ONLY_EDITORS_CAN_DESELECT_REQUIREMENT("false"),
	ASK_VENDOR_FOR_PRICE("false"),
	APIKEY(null);

	private String defaultValue;
	
	private CustomerSetting(String defaultValue) {
		this.defaultValue = defaultValue;
	}
}
