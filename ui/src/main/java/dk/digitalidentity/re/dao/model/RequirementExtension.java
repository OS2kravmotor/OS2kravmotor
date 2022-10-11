package dk.digitalidentity.re.dao.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import lombok.Data;

@Entity
@Data
public class RequirementExtension {

	@Id
	@Column
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "requirement_id")
	private Requirement requirement;

	@Column
	private String cvr;

	@Column
	private String description;
	
	@Column
	private String helpText;

	@Column
	private String interestedParty;

	@Column
	private boolean favorite;

	@Column
	private boolean disableRequirement;

	@Column
	private String disableRequirementReason;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "requirementExtension", orphanRemoval = true)
	private List<LocalAttachment> attachments;
}
