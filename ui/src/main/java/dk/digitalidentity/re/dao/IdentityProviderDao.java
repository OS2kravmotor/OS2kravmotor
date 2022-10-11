package dk.digitalidentity.re.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import dk.digitalidentity.re.dao.model.IdentityProvider;

public interface IdentityProviderDao extends CrudRepository<IdentityProvider, Long> {
	List<IdentityProvider> findAll();
	IdentityProvider getByEntityId(String entityId);
	IdentityProvider getByCvr(String cvr);
}
