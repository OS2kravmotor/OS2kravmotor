package dk.digitalidentity.re.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import dk.digitalidentity.re.dao.model.Setting;

public interface SettingDao extends CrudRepository<Setting, Long> {
	List<Setting> findByCvr(String cvr);
	Setting getById(long id);
	Setting getByKeyAndCvr(String key, String cvr);
}
