package dk.digitalidentity.re.mvc.form;

import org.hibernate.validator.constraints.Length;

import lombok.Data;

@Data
public class SettingForm {
	private boolean onlyEditorsCanRemoveMinimumRequirements;
	private boolean askVendorForPrice;
	@Length(min=8, message="html.error.settings.apikey")
	private String apiKey;
}
