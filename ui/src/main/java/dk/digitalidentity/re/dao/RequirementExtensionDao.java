package dk.digitalidentity.re.dao;

import org.springframework.data.repository.CrudRepository;

import dk.digitalidentity.re.dao.model.Requirement;
import dk.digitalidentity.re.dao.model.RequirementExtension;

public interface RequirementExtensionDao extends CrudRepository<RequirementExtension, Long> {
	RequirementExtension getByRequirementAndCvr(Requirement requirement, String cvr);
	RequirementExtension getByRequirementIdAndCvr(Long requirementId, String cvr);
}
