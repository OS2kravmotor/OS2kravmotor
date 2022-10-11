package dk.digitalidentity.re.mvc.form;

import java.util.List;

import dk.digitalidentity.re.dao.model.Attachment;
import dk.digitalidentity.re.dao.model.LocalAttachment;
import dk.digitalidentity.re.dao.model.enums.AnswerChoice;
import dk.digitalidentity.re.dao.model.enums.Importance;
import lombok.Data;

@Data
public class VendorRequirementAnswerForm {
	private String name;
	private AnswerChoice choice;
	private String price;
	private String detail;
	private long purchaseRequirementId;
	private String description;
	private String rationale;
	private Importance importance;
	private boolean infoRequirement;
	private List<Attachment> attachments;
	private List<LocalAttachment> localAttachments;
}
