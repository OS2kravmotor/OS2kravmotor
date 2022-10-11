package dk.digitalidentity.re.dao;

import dk.digitalidentity.re.dao.model.Domain;
import org.springframework.data.repository.CrudRepository;

public interface DomainDao extends CrudRepository<Domain, Long> {
    public Domain getById(long id);
    public Domain getByName(String name); // there is a unique constraint in the schema, so it's safe to assume that there is only one result
}
