package dk.digitalidentity.re.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import dk.digitalidentity.re.dao.model.ItSystem;

public interface ItSystemDao extends CrudRepository<ItSystem, Long> {
	ItSystem getById(long id);

	List<ItSystem> findByVendor(String vendor);
	
	List<ItSystem> findAll();
}
