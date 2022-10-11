package dk.digitalidentity.re.dao;

import dk.digitalidentity.re.dao.model.PurchaseRequirementAnswer;
import org.springframework.data.repository.CrudRepository;

public interface PurchaseRequirementAnswerDao extends CrudRepository<PurchaseRequirementAnswer, Long> {
    PurchaseRequirementAnswer getById(long id);
}
