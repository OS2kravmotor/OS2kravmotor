package dk.digitalidentity.re.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import dk.digitalidentity.re.dao.ItSystemDao;
import dk.digitalidentity.re.dao.model.ItSystem;

@Service
public class ItSystemService {

	@Autowired
	private ItSystemDao itSystemDao;

	public ItSystem save(ItSystem entity) {
		return itSystemDao.save(entity);
	}

	public ItSystem getById(long id) {
		return itSystemDao.getById(id);
	}

	public List<ItSystem> findByVendor(String vendor) {
		return itSystemDao.findByVendor(vendor);
	}

	@Cacheable(value = "myCache")
	public List<ItSystem> findAll() {
		return itSystemDao.findAll();
	}
	
	@CacheEvict(value = "myCache", allEntries = true)
	public void clearCache() {
		
	}
}
