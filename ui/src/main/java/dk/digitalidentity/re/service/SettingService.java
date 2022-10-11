package dk.digitalidentity.re.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dk.digitalidentity.re.dao.SettingDao;
import dk.digitalidentity.re.dao.model.PurchaseVendor;
import dk.digitalidentity.re.dao.model.Setting;
import dk.digitalidentity.re.dao.model.enums.CustomerSetting;
import dk.digitalidentity.re.security.SecurityUtil;
import lombok.extern.log4j.Log4j;

@Service("SettingService")
@Log4j
public class SettingService {

	@Autowired
	private SettingDao settingDao;
	
	@Autowired
	private PurchaseVendorService purchaseVendorService;
	
	@Autowired
	private SecurityUtil securityUtil;

	public boolean getBooleanValueByKey(CustomerSetting customerSetting) {
		boolean value;
		
		Setting setting = getByKey(customerSetting);
		
		if (setting != null) {
			value = Boolean.parseBoolean(setting.getValue());
		}
		else {
			value = Boolean.parseBoolean(customerSetting.getDefaultValue());
		}

		return value;
	}

	public boolean getBooleanValueByKeyAndCvr(CustomerSetting customerSetting, String cvr )
	{
		Setting setting = settingDao.getByKeyAndCvr(customerSetting.toString(), cvr);
		boolean value;
		if (setting != null) {
			value = Boolean.parseBoolean(setting.getValue());
		}
		else {
			value = Boolean.parseBoolean(customerSetting.getDefaultValue());
		}

		return value;
	}
	
	public boolean getBooleanValueByKeyForVendor(CustomerSetting customerSetting) {
		PurchaseVendor purchaseVendor = purchaseVendorService.getPurchaseVendor();
		if (purchaseVendor == null) {
			log.error("Failed to lookup vendor");
			throw new RuntimeException("No vendor is logged in!");
		}

		Setting setting = settingDao.getByKeyAndCvr(customerSetting.toString(), purchaseVendor.getPurchase().getCvr());

		boolean value;
		if (setting != null) {
			value = Boolean.parseBoolean(setting.getValue());
		}
		else {
			value = Boolean.parseBoolean(customerSetting.getDefaultValue());
		}

		return value;
	}
	
	public String getStringValueByKey(CustomerSetting customerSetting) {
		String value;
		
		Setting setting = getByKey(customerSetting);
		
		if (setting != null) {
			value = setting.getValue();
		}
		else {
			value = customerSetting.getDefaultValue();
		}

		return value;
	}
	
	public Integer getIntegerValueByKey(CustomerSetting customerSetting) {
		Integer value;
		
		Setting setting = getByKey(customerSetting);
		
		if (setting != null) {
			value = Integer.parseInt(setting.getValue());
		}
		else {
			value = Integer.parseInt(customerSetting.getDefaultValue());
		}

		return value;
	}

	public Setting getByKey(CustomerSetting key) {
		return settingDao.getByKeyAndCvr(key.toString(), securityUtil.getCvr());
	}

	public List<Setting> getAll() {
		String cvr = securityUtil.getCvr();

		if (cvr != null) {
			return settingDao.findByCvr(cvr);
		}

		return null;
	}

	public void save(Setting setting) {
		if (hasAccess(setting)) {
			if (setting.getCvr() == null) {
				setting.setCvr(securityUtil.getCvr());
			}

			settingDao.save(setting);
		}
		else {
			throw new IllegalAccessError("User " + SecurityUtil.getUser() + " is not allowed to update Setting: " + setting.getId());
		}
	}

	private boolean hasAccess(Setting setting) {
		String cvr = securityUtil.getCvr();
		if (cvr != null && (cvr.equals(setting.getCvr()) || setting.getCvr() == null)) {
			return true;
		}
		
		return false;
	}
}
