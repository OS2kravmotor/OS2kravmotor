package dk.digitalidentity.re.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import dk.digitalidentity.re.dao.model.JIRASprint;

public interface JIRASprintDao extends CrudRepository<JIRASprint, Long> {

	List<JIRASprint> findAll();

}
