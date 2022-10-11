package dk.digitalidentity.re.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dk.digitalidentity.re.dao.CategoryDao;
import dk.digitalidentity.re.dao.model.Category;

@Service
public class CategoryService {

	@Autowired
	private CategoryDao categoryDao;

	public List<Category> findAll() {
		return categoryDao.findAll();
	}

	public Category getById(long id) {
		return categoryDao.getById(id);
	}

	public Category save(Category entity) {
		return categoryDao.save(entity);
	}

	public void delete(Category category) {
		categoryDao.delete(category);
	}
}
