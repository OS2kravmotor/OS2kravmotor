package dk.digitalidentity.re.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dk.digitalidentity.re.dao.CommunityDao;
import dk.digitalidentity.re.dao.model.Community;

@Service
public class CommunityService {

	@Autowired
	private CommunityDao communityDao;

	@Transactional
	public List<Community> getCommunities(String municipalityCvr) {
		return communityDao.findAll()
				.stream()
				.filter(c -> c.getCommunityMembers()
						.stream()
						.anyMatch(cm -> cm.getMunicipalityCvr().equals(municipalityCvr)))
				.collect(Collectors.toList());
	}

	public Community getById(long id) {
		return communityDao.getById(id);
	}

	public String getCommunityName(String cvr) {
		Community community = communityDao.getByCommunityCvr(cvr);

		if (community != null) {
			return community.getName();
		}

		return null;
	}
}
