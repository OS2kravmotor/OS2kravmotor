package dk.digitalidentity.re.dao;

import org.springframework.data.repository.CrudRepository;

import dk.digitalidentity.re.dao.model.Category;
import dk.digitalidentity.re.dao.model.Purchase;
import dk.digitalidentity.re.dao.model.PurchaseRequirement;

public interface PurchaseRequirementDao extends CrudRepository<PurchaseRequirement, Long> {
	PurchaseRequirement getByIdAndPurchase(long requirementId, Purchase purchase);
	PurchaseRequirement getByRequirementIdAndPurchase(long requirementId, Purchase purchase);
	long countByCategory(Category category);
}
