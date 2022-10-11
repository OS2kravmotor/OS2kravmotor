package dk.digitalidentity.re.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import dk.digitalidentity.re.dao.model.Community;

public interface CommunityDao extends CrudRepository<Community, Long> {
	public List<Community> findAll();
	public Community getById(long id);
	public Community getByCommunityCvr(String cvr);
}
