package dk.digitalidentity.re.dao;

import dk.digitalidentity.re.dao.model.ArchitecturePrinciple;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PrincipleDao extends CrudRepository<ArchitecturePrinciple, Long> {
    public List<ArchitecturePrinciple> findAll();
    public ArchitecturePrinciple getById(long id);
    public ArchitecturePrinciple getByName(String name);
}
