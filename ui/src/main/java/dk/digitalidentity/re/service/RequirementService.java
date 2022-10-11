package dk.digitalidentity.re.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dk.digitalidentity.re.dao.model.ArchitecturePrinciple;
import dk.digitalidentity.re.dao.model.Attachment;
import dk.digitalidentity.re.dao.model.Category;
import dk.digitalidentity.re.dao.model.LocalAttachment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.history.Revisions;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import dk.digitalidentity.re.Constants;
import dk.digitalidentity.re.dao.RequirementDao;
import dk.digitalidentity.re.dao.RequirementExtensionDao;
import dk.digitalidentity.re.dao.model.Community;
import dk.digitalidentity.re.dao.model.CommunityMember;
import dk.digitalidentity.re.dao.model.Domain;
import dk.digitalidentity.re.dao.model.Requirement;
import dk.digitalidentity.re.dao.model.RequirementExtension;
import dk.digitalidentity.re.dao.model.Tag;
import dk.digitalidentity.re.dao.model.enums.RequirementType;
import dk.digitalidentity.re.mvc.form.RequirementForm;
import dk.digitalidentity.re.security.SecurityUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RequirementService {
	private Locale locale = new Locale("da-DK");

	@Value("${email.sender}")
	private String senderEmailAddress;
	
	@Autowired
	private RequirementDao requirementDao;

	@Autowired
	private RequirementExtensionDao requirementExtensionDao;

	@Autowired
	private CommunityService communityService;

	@Autowired
	private SecurityUtil securityUtil;

	@Autowired
	private EmailService emailService;

	@Autowired
	private MessageSource messageSource;

	public Requirement getById(long id) {
		Requirement requirement = requirementDao.getByIdAndDeletedFalse(id);
		if (requirement != null && canRead(requirement)) {
			return requirement;
		}

		return null;
	}

	public List<Requirement> getAllDeleted() {
		return requirementDao.findAllByDeletedTrue().stream().filter(r -> canModify(r)).collect(Collectors.toList());
	}

	public List<Requirement> getByTagsContains(Tag tag) {
		String cvr = securityUtil.getCvr();
		List<Requirement> requirementsByCvr;

		List<String> communities = communityService.getCommunities(cvr).stream().map(Community::getCommunityCvr).collect(Collectors.toList());

		if (cvr != null) {
			requirementsByCvr = requirementDao.findByTagsContainsAndCvrInAndDeletedFalse(tag, Stream.concat(Arrays.asList(cvr, Constants.DEFAULT_CVR).stream(), communities.stream()).collect(Collectors.toList()));
		}
		else {
			requirementsByCvr = requirementDao.findByTagsContainsAndCvrInAndDeletedFalse(tag, Arrays.asList(Constants.DEFAULT_CVR));
		}

		return requirementsByCvr;
	}

	public List<Requirement> getByTagsAvailableForAll() {
		String cvr = securityUtil.getCvr();
		List<Requirement> requirementsByCvr;

		List<String> communities = communityService.getCommunities(cvr).stream().map(Community::getCommunityCvr).collect(Collectors.toList());

		if (cvr != null) {
			requirementsByCvr = requirementDao.findByAvailableForAllTagsTrueAndCvrInAndDeletedFalse(Stream.concat(Arrays.asList(cvr, Constants.DEFAULT_CVR).stream(), communities.stream()).collect(Collectors.toList()));
		}
		else {
			requirementsByCvr = requirementDao.findByAvailableForAllTagsTrueAndCvrInAndDeletedFalse(Arrays.asList(Constants.DEFAULT_CVR));
		}

		return requirementsByCvr;
	}

	public List<Requirement> getByDomainsContains(Domain domain) {
		String cvr = securityUtil.getCvr();
		List<Requirement> requirementsByCvr;

		List<String> communities = communityService.getCommunities(cvr).stream().map(Community::getCommunityCvr).collect(Collectors.toList());

		if (cvr != null) {
			requirementsByCvr = requirementDao.findByDomainsContainsAndCvrInAndDeletedFalse(domain, Stream.concat(Arrays.asList(cvr, Constants.DEFAULT_CVR).stream(), communities.stream()).collect(Collectors.toList()));
		}
		else {
			requirementsByCvr = requirementDao.findByDomainsContainsAndCvrInAndDeletedFalse(domain, Arrays.asList(Constants.DEFAULT_CVR));
		}

		return requirementsByCvr;
	}

	public void delete(Requirement requirement) {
		if (canModify(requirement)) {
			requirement.setDeleted(true);
			requirementDao.save(requirement);
		} else {
			throw new IllegalStateException("User is not allowed to delete requirement");
		}
	}

	public List<Requirement> getAllRequirements() {
		String cvr = securityUtil.getCvr();

		List<String> communities = communityService.getCommunities(cvr).stream().map(Community::getCommunityCvr).collect(Collectors.toList());

		if (cvr != null) {
			return requirementDao.findByCvrInAndDeletedFalse(Stream.concat(Arrays.asList(cvr, Constants.DEFAULT_CVR).stream(), communities.stream()).collect(Collectors.toList()));
		}

		return requirementDao.findByCvrInAndDeletedFalse(Arrays.asList(Constants.DEFAULT_CVR));
	}

	public List<Requirement> getAllRequirementsOfType(RequirementType type) {
		String cvr = securityUtil.getCvr();

		if (type.equals(RequirementType.TOBESHARED)) {
			return requirementDao.findByRequestedToBeSharedTrueAndDeletedFalse();
		}
		else if (cvr != null && type.equals(RequirementType.LOCAL)) {
			return requirementDao.findByCvrInAndDeletedFalse(Arrays.asList(cvr));
		}
		else if (type.equals(RequirementType.GLOBAL)) {
			return requirementDao.findByCvrInAndDeletedFalse(Arrays.asList(Constants.DEFAULT_CVR));
		}
		else if(type.equals(RequirementType.COMMUNITY)) {
			List<Community> communities = communityService.getCommunities(SecurityUtil.getMunicipalityCvr());
			return requirementDao.findByCvrInAndDeletedFalse(communities.stream().map(Community::getCommunityCvr).collect(Collectors.toList()));
		}

		return new ArrayList<>();
	}
	
	public List<Requirement> getAllRequirementsOfTypeAndCategory(RequirementType type, Category category) {
		String cvr = securityUtil.getCvr();

		if (cvr != null && type.equals(RequirementType.LOCAL)) {
			return requirementDao.findByCvrInAndCategoryAndDeletedFalse(Arrays.asList(cvr), category);
		}
		else if (type.equals(RequirementType.GLOBAL)) {
			return requirementDao.findByCvrInAndCategoryAndDeletedFalse(Arrays.asList(Constants.DEFAULT_CVR), category);
		}
		else if(type.equals(RequirementType.COMMUNITY)) {
			List<Community> communities = communityService.getCommunities(SecurityUtil.getMunicipalityCvr());
			return requirementDao.findByCvrInAndCategoryAndDeletedFalse(communities.stream().map(Community::getCommunityCvr).collect(Collectors.toList()), category);
		}

		return new ArrayList<>();
	}

	public void handlePromotionRequest(long id, boolean acceptRequirement, String rejectMessage) {
		Requirement requirement = getById(id);
		if (requirement == null || !canModify(requirement)) {
			throw new IllegalAccessError("User " + SecurityUtil.getUser() + " is not allowed to modify requirement " + id);
		}

		requirement.setRequestedToBeShared(false);
		if (acceptRequirement) {
			requirement.setCvr(Constants.DEFAULT_CVR);
		}

		if (requirement.getRequesterEmail() != null) {
			try {
				String email = requirement.getRequesterEmail();
				if (StringUtils.hasLength(email)) {
					String title = null;
					String message = null;
					
					if (acceptRequirement) {
						title = messageSource.getMessage("email.requirement.approve.title", null, locale);
						message = messageSource.getMessage("email.requirement.approve.body", new String[] { requirement.getName() }, locale);
					}
					else {
						title = messageSource.getMessage("email.requirement.reject.title", null, locale);
						message = messageSource.getMessage("email.requirement.reject.body", new String[] { requirement.getName(), rejectMessage }, locale);
					}
			
					emailService.sendMessage(senderEmailAddress, email, title, message);
					requirement.setRequesterEmail(null);
				}
			}
			catch (Exception ex) {
				log.warn("Error occured while trying to send email. ", ex);
			}
		}

		requirementDao.save(requirement);
	}

	public void changeOwnership(long id, Community community) {
		Requirement requirement = getById(id);
		if (requirement == null || !canModify(requirement)) {
			throw new IllegalAccessError("User " + SecurityUtil.getUser() + " is not allowed to modify requirement " + id);
		}

		boolean allowed = false;
		for (CommunityMember member : community.getCommunityMembers()) {
			if (SecurityUtil.getMunicipalityCvr().equals(member.getMunicipalityCvr())) {
				allowed = true;
			}
		}

		if (!allowed) {
			throw new IllegalAccessError("User " + SecurityUtil.getUser() + " is not allowed to promote to community " + community.getCommunityCvr() + " for requirement " + id);
		}

		requirement.setCvr(community.getCommunityCvr());
		requirementDao.save(requirement);
	}

	public Requirement save(Requirement requirement) {
		if (requirement.getId() > 0) {
			Requirement requirementFromDB = getById(requirement.getId());
			if (requirementFromDB == null || !canModify(requirementFromDB)) {
				throw new IllegalAccessError("User " + SecurityUtil.getUser() + " is not allowed to modify requirement " + requirement.getId());
			}

			requirement.setCvr(requirementFromDB.getCvr());
		}
		else {
			requirement.setCvr(securityUtil.getCvr());
		}

		if (canModify(requirement)) {
			return requirementDao.save(requirement);
		}

		throw new IllegalStateException("No user is logged in, unable to save requirement");
	}

	public RequirementExtension getRequirementExtension(Requirement requirement) {
		return requirementExtensionDao.getByRequirementAndCvr(requirement, SecurityUtil.getMunicipalityCvr());
	}

	public  RequirementExtension getRequirementExtensionByRequirementID(long id) {
		return getRequirementExtension(getById(id));
	}

	public void updateLocalExtensionsForCommunityRequirement(Requirement requirement, boolean favorite) {
		RequirementExtension extension = requirementExtensionDao.getByRequirementAndCvr(requirement, SecurityUtil.getMunicipalityCvr());
		
		if (extension != null) {
			extension.setFavorite(favorite);
		}
		else {
			extension = new RequirementExtension();
			extension.setCvr(SecurityUtil.getMunicipalityCvr());
			extension.setRequirement(requirement);
			extension.setFavorite(favorite);
		}

		requirementExtensionDao.save(extension);
	}

	public void updateLocalExtensionsOnly(RequirementForm requirement, List<Attachment> attachments, List<Long> attachmentsToBeRemoved) {
		if (requirement.getId() > 0) {
			Requirement req = getById(requirement.getId());

			if (req != null) {
				RequirementExtension extension = requirementExtensionDao.getByRequirementAndCvr(req, SecurityUtil.getMunicipalityCvr());

				if (extension != null) {
					extension.setDescription(requirement.getExtDescription());
					extension.setHelpText(requirement.getHelpText());
					extension.setInterestedParty(requirement.getInterestedParty());
					extension.setFavorite(requirement.isFavorite());
					extension.setDisableRequirement(requirement.isExtDisable());
					extension.setDisableRequirementReason(requirement.getExtDisableReason());

					for (Long id : attachmentsToBeRemoved) {
						extension.getAttachments().removeIf(a -> a.getId() == id);
					}

					extension.getAttachments().addAll(getAttachmentsAsLocalAttachments(attachments, extension));
				}
				else {
					extension = new RequirementExtension();
					extension.setCvr(SecurityUtil.getMunicipalityCvr());
					extension.setRequirement(req);
					extension.setDescription(requirement.getExtDescription());
					extension.setHelpText(requirement.getHelpText());
					extension.setInterestedParty(requirement.getInterestedParty());
					extension.setAttachments(getAttachmentsAsLocalAttachments(attachments, extension));
					extension.setFavorite(requirement.isFavorite());
					extension.setDisableRequirement(requirement.isExtDisable());
					extension.setDisableRequirementReason(requirement.getExtDisableReason());
				}

				requirementExtensionDao.save(extension);
				return;
			}
		}

		throw new IllegalStateException("Requirement with id " + requirement.getId() + " does not exist");
	}

	private List<LocalAttachment> getAttachmentsAsLocalAttachments(List<Attachment> attachments, RequirementExtension extension) {
		List<LocalAttachment> localAttachments = new ArrayList<>();
		for (Attachment a : attachments) {
			LocalAttachment attachment = new LocalAttachment();
			attachment.setName(a.getName());
			attachment.setRequirementExtension(extension);
			attachment.setUrl(a.getUrl());
			localAttachments.add(attachment);
		}
		return localAttachments;
	}

	public boolean canRead(Requirement requirement) {
		String cvr = securityUtil.getCvr();
		List<String> roles = SecurityUtil.getRoles();

		if (cvr.equals(requirement.getCvr())) {
			return true;
		}

		if (Constants.DEFAULT_CVR.equals(requirement.getCvr())) {
			return true;
		}

		// global editors can read requirements that are pending approval
		if (roles.contains("ROLE_http://kravmotoren.dk/globaleditor") && requirement.isRequestedToBeShared()) {
			return true;
		}

		// users can lookup requirements from communities
		//   get list of communities this user belongs to; filter only cvr;               toList of strings            check if contains cvr of requirement in question 
		if (communityService.getCommunities(cvr).stream().map(Community::getCommunityCvr).collect(Collectors.toList()).contains(requirement.getCvr())) {
			return true;
		}

		return false;
	}

	public boolean canModify(Requirement requirement) {
		String cvr = securityUtil.getCvr();
		List<String> roles = SecurityUtil.getRoles();

		// only editor can modify requirement (or global editor... need to add that check as well I think)
		if (!roles.contains("ROLE_http://kravmotoren.dk/editor")) {
			return false;
		}

		List<Community> communities = communityService.getCommunities(SecurityUtil.getMunicipalityCvr());
		Set<String> communityCvrs = communities.stream().map(c -> c.getCommunityCvr()).collect(Collectors.toSet());
		
		// only owner can modify
		if (!Objects.equals(cvr, requirement.getCvr())) {
			boolean illegalAccess = true;

			// only global editors can modify global requirement
			if (roles.contains("ROLE_http://kravmotoren.dk/globaleditor")) {

				if (Constants.DEFAULT_CVR.equals(requirement.getCvr())) {
					illegalAccess = false;
				}

				// global editors can edit requirements that have been requested to be shared
				else if (requirement.isRequestedToBeShared()) {
					illegalAccess = false;
				}
			}
			
			// check for communities
			if (communityCvrs.contains(requirement.getCvr())) {
				illegalAccess = false;
			}

			if (illegalAccess) {
				return false;
			}
		}

		return true;
	}

	public List<Requirement> getRecentlyModified() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -3);
		Date date = cal.getTime();

		return requirementDao.findByCvrInAndLastChangedAfterAndDeletedFalse(Arrays.asList(Constants.DEFAULT_CVR), date);
	}

	public Revisions<Integer,Requirement> getRevisions(long id) {
		return requirementDao.findRevisions(id);
	}

	public long countByCategoryAndDeletedFalse(Category category) {
		return requirementDao.countByCategoryAndDeletedFalse(category);
	}

	public long countByDomainsContainsAndDeletedFalse(Domain domain) {
		return requirementDao.countByDomainsContainsAndDeletedFalse(domain);
	}

	public long countByPrinciplesContainsAndDeletedFalse(ArchitecturePrinciple principle) {
		return requirementDao.countByPrinciplesContainsAndDeletedFalse(principle);
	}

	public long countByTagsContainsAndDeletedFalse(Tag tag) {
		return requirementDao.countByTagsContainsAndDeletedFalse(tag);
	}
}
