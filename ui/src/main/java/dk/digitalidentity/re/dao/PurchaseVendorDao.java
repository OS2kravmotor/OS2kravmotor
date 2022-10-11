package dk.digitalidentity.re.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import dk.digitalidentity.re.dao.model.PurchaseVendor;
import dk.digitalidentity.re.dao.model.Purchase;

public interface PurchaseVendorDao extends CrudRepository<PurchaseVendor, Long> {
	PurchaseVendor getById(long id);
	PurchaseVendor getByPurchaseAndId(Purchase purchase, long id);
	List<PurchaseVendor> findAllByPurchase(Purchase purchase);
	PurchaseVendor getByUsernameAndPassword(String username, String password);
	PurchaseVendor getByUsername(String username);

	@Query("SELECT CASE WHEN COUNT(*) > 0 THEN false ELSE true END FROM PurchaseVendor pv JOIN pv.purchase where pv.username = :username")
	boolean isUsernameUnique(@Param("username") String username);
}
