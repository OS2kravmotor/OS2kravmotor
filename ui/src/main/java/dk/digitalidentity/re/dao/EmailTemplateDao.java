package dk.digitalidentity.re.dao;

import org.springframework.data.repository.CrudRepository;

import dk.digitalidentity.re.dao.model.EmailTemplate;
import dk.digitalidentity.re.dao.model.enums.EmailTemplateType;

public interface EmailTemplateDao extends CrudRepository<EmailTemplate, Long> {
	EmailTemplate findByTemplateTypeAndCvr(EmailTemplateType type, String cvr);

	EmailTemplate findByIdAndCvr(long id, String cvr);
}
