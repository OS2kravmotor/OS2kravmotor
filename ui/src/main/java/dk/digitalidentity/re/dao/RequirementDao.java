package dk.digitalidentity.re.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.history.RevisionRepository;

import dk.digitalidentity.re.dao.model.ArchitecturePrinciple;
import dk.digitalidentity.re.dao.model.Category;
import dk.digitalidentity.re.dao.model.Domain;
import dk.digitalidentity.re.dao.model.Requirement;
import dk.digitalidentity.re.dao.model.Tag;

public interface RequirementDao extends CrudRepository<Requirement, Long>, RevisionRepository<Requirement, Long, Integer> {
	long countByCategoryAndDeletedFalse(Category category);
	long countByDomainsContainsAndDeletedFalse(Domain domain);
	long countByTagsContainsAndDeletedFalse(Tag tag);
	long countByPrinciplesContainsAndDeletedFalse(ArchitecturePrinciple principle);
	Requirement getByIdAndDeletedFalse(long id);
	Requirement getById(long id);
	List<Requirement> findAllByDeletedFalse();
	List<Requirement> findAllByDeletedTrue();
	List<Requirement> findByDomainsContainsAndCvrInAndDeletedFalse(Domain domain, Collection<String> cvrs);
	List<Requirement> findByTagsContainsAndCvrInAndDeletedFalse(Tag tag, Collection<String> cvrs);
	List<Requirement> findByAvailableForAllTagsTrueAndCvrInAndDeletedFalse(Collection<String> cvrs);
	List<Requirement> findByCvrInAndDeletedFalse(Collection<String> cvrs);
	List<Requirement> findByCvrInAndCategoryAndDeletedFalse(Collection<String> cvrs, Category category);
	List<Requirement> findByRequestedToBeSharedTrueAndDeletedFalse();
	List<Requirement> findByCvrInAndLastChangedAfterAndDeletedFalse(List<String> asList, Date date);
}
