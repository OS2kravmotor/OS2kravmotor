package dk.digitalidentity.re.mvc.form;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import dk.digitalidentity.re.dao.model.LocalAttachment;
import dk.digitalidentity.re.dao.model.enums.Importance;
import dk.digitalidentity.re.dao.model.enums.RequirementType;
import dk.digitalidentity.re.mvc.dto.AttachmentDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequirementForm {
	private boolean selected;
	private long purchaseRequirementId;
	private long id;
	private String name;
	private String description;
	private String originalDescription;
	private String cvr;
	private String rationale;
	private boolean infoRequirement;
	private boolean relevantForOnPremise;
	private boolean relevantForSaas;
	private String notes;
	private String helpText;
	private String interestedParty;
	private boolean favorite;
	private boolean requestedToBeShared;
	private boolean availableForAllDomains;
	private boolean availableForAllTags;
	private Importance importance;
	private long category;
	private boolean shared;
	private Date lastChanged;

	private String extDescription;
	private boolean extDisable;
	private String extDisableReason;
	private RequirementType requirementType;

	private List<MultipartFile> files = new ArrayList<>();
	private List<MultipartFile> localFiles = new ArrayList<>();
	private List<AttachmentDTO> attachments = new ArrayList<AttachmentDTO>();
	private List<LocalAttachment> localAttachments = new ArrayList<>();
	private List<Long> removeAttachments = new ArrayList<>();
	private List<Long> removeLocalAttachments = new ArrayList<>();

	private List<Long> domains = new ArrayList<>();
	private List<String> tags = new ArrayList<>();
	private List<Long> principles = new ArrayList<>();
	private List<DomainForm> domainsRichObjects = new ArrayList<>();
	private List<TagForm> tagsRichObjects = new ArrayList<>();
	private List<PrincipleForm> principlesRichObjects = new ArrayList<>();
	private CategoryForm categoryRichObject;
	
	private long subcategory;
	private String subcategoryName;
}
