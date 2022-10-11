package dk.digitalidentity.re.dao;

import dk.digitalidentity.re.dao.model.Category;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CategoryDao extends CrudRepository<Category, Long> {
	public List<Category> findAll();
	public Category getById(long id);
	public Category getByName(String name); // there is a unique constraint in the schema, so it's safe to assume that there is only one result
}
