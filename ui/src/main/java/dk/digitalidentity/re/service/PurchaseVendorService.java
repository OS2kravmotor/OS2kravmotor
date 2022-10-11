package dk.digitalidentity.re.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dk.digitalidentity.re.dao.PurchaseVendorDao;
import dk.digitalidentity.re.dao.model.Purchase;
import dk.digitalidentity.re.dao.model.PurchaseVendor;
import dk.digitalidentity.re.security.SecurityUtil;

@Service
public class PurchaseVendorService {
	
	@Autowired
	private PurchaseVendorDao purchaseVendorDao;
	
	public boolean isUsernameUnique(String username) {
		return purchaseVendorDao.isUsernameUnique(username);
	}
	
	public PurchaseVendor getPurchaseVendor() {
		String username = SecurityUtil.getUser();
		if (username == null) {
			return null;
		}

		return purchaseVendorDao.getByUsername(username);
	}

	public void save(PurchaseVendor purchaseVendor) {
		purchaseVendorDao.save(purchaseVendor);
	}

	public List<PurchaseVendor> findByPurchase(Purchase purchase) {
		return purchaseVendorDao.findAllByPurchase(purchase);
	}
	
	public void delete(PurchaseVendor purchaseVendor) {
		purchaseVendorDao.delete(purchaseVendor);
	}
}
