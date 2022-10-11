package dk.digitalidentity.re.dao;

import dk.digitalidentity.re.dao.model.Purchase;
import dk.digitalidentity.re.dao.model.PurchaseAnswer;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PurchaseAnswerDao extends CrudRepository<PurchaseAnswer, Long> {
    PurchaseAnswer getById(long id);

    PurchaseAnswer getByPurchaseIdAndVendorOrganizationId(long purchaseId, long vendorOrganizationId);

    List<PurchaseAnswer> findByPurchaseId(long purchaseId);

    PurchaseAnswer getByPurchaseAndId(Purchase purchase, long id);
}
