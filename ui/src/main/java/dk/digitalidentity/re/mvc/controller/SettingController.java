package dk.digitalidentity.re.mvc.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import dk.digitalidentity.re.dao.model.Setting;
import dk.digitalidentity.re.dao.model.enums.CustomerSetting;
import dk.digitalidentity.re.mvc.form.SettingForm;
import dk.digitalidentity.re.security.RequireAdministratorRole;
import dk.digitalidentity.re.service.SettingService;

@RequireAdministratorRole
@Controller
public class SettingController {

	@Autowired
	private SettingService settingService;

	@RequestMapping(path = { "setting/" }, method = RequestMethod.GET)
	public String settings(Model model) {
		SettingForm settingForm = new SettingForm();
		
		settingForm.setOnlyEditorsCanRemoveMinimumRequirements(settingService.getBooleanValueByKey(CustomerSetting.ONLY_EDITORS_CAN_DESELECT_REQUIREMENT));
		settingForm.setAskVendorForPrice(settingService.getBooleanValueByKey(CustomerSetting.ASK_VENDOR_FOR_PRICE));
		settingForm.setApiKey(settingService.getStringValueByKey(CustomerSetting.APIKEY));

		model.addAttribute("settingForm", settingForm);

		return "setting/settings";
	}
	
	@RequestMapping(path = { "setting/" }, method = RequestMethod.POST)
	public String updateSettings(Model model, @ModelAttribute("settingForm") @Valid SettingForm settingForm, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			model.addAttribute(bindingResult.getAllErrors());
			model.addAttribute("saved", false);
			model.addAttribute("settingForm", settingForm);

			return "setting/settings";
		}

		// Only Editors Can Remove Minimum Requirements
		Setting onlyEditorsCanRemoveMinimumRequirements = settingService.getByKey(CustomerSetting.ONLY_EDITORS_CAN_DESELECT_REQUIREMENT);
		if (onlyEditorsCanRemoveMinimumRequirements != null) {
			onlyEditorsCanRemoveMinimumRequirements.setValue(settingForm.isOnlyEditorsCanRemoveMinimumRequirements() ? Boolean.TRUE.toString() : Boolean.FALSE.toString());
		}
		else {
			onlyEditorsCanRemoveMinimumRequirements = new Setting();
			onlyEditorsCanRemoveMinimumRequirements.setKey(CustomerSetting.ONLY_EDITORS_CAN_DESELECT_REQUIREMENT.toString());
			onlyEditorsCanRemoveMinimumRequirements.setValue(settingForm.isOnlyEditorsCanRemoveMinimumRequirements() ? Boolean.TRUE.toString() : Boolean.FALSE.toString());
		}
		settingService.save(onlyEditorsCanRemoveMinimumRequirements);

		// Ask Vendor For Price
		Setting askVendorForPrice = settingService.getByKey(CustomerSetting.ASK_VENDOR_FOR_PRICE);
		if (askVendorForPrice != null) {
			askVendorForPrice.setValue(settingForm.isAskVendorForPrice() ? Boolean.TRUE.toString() : Boolean.FALSE.toString());
		}
		else {
			askVendorForPrice = new Setting();
			askVendorForPrice.setKey(CustomerSetting.ASK_VENDOR_FOR_PRICE.toString());
			askVendorForPrice.setValue(settingForm.isAskVendorForPrice() ? Boolean.TRUE.toString() : Boolean.FALSE.toString());
		}
		settingService.save(askVendorForPrice);

		// ApiKey
		Setting apiKey = settingService.getByKey(CustomerSetting.APIKEY);
		if (apiKey != null) {
			apiKey.setValue(settingForm.getApiKey());
		}
		else {
			apiKey = new Setting();
			apiKey.setKey(CustomerSetting.APIKEY.toString());
			apiKey.setValue(settingForm.getApiKey());
		}
		settingService.save(apiKey);

		model.addAttribute("settingForm", settingForm);
		model.addAttribute("saved", true);

		return "setting/settings";
	}

}
