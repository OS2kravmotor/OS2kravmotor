package dk.digitalidentity.re.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import dk.digitalidentity.re.dao.model.Domain;
import dk.digitalidentity.re.dao.model.Purchase;
import dk.digitalidentity.re.dao.model.enums.Status;

public interface PurchaseDao extends CrudRepository<Purchase, Long>{
	long countByDomainsContains(Domain domain);
	Purchase getById(long id);
	List<Purchase> findByCvr(String cvr);
	List<Purchase> findByStatus(Status status);
	int countByStartTimeAfter(Date date);
}
