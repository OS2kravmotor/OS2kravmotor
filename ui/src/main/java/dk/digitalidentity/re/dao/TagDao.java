package dk.digitalidentity.re.dao;

import dk.digitalidentity.re.dao.model.Tag;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TagDao extends CrudRepository<Tag, Long> {
    public List<Tag> findAll();
    public Tag getById(long id);
    public Tag getByName(String name); // unique constraint in schema
}
