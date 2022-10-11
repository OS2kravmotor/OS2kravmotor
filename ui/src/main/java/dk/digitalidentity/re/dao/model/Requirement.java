package dk.digitalidentity.re.dao.model;

import dk.digitalidentity.re.Constants;
import dk.digitalidentity.re.dao.model.enums.Importance;
import dk.digitalidentity.re.log.EventLoggable;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import java.util.Date;
import java.util.List;

@Entity
@Audited
@Data
public class Requirement implements EventLoggable {

	@Id
	@Column
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column
	private String name;

	@Column
	private String description;

	@Column
	private String cvr;

	@Column
	private String rationale;

	@Column
	private boolean deleted;

	@Column
	private boolean infoRequirement;

	@Column
	private boolean favorite;

	@Column
	private String notes;

	@Column
	private String helpText;

	@Column
	private String interestedParty;

	@Column(name="request_share")
	private boolean requestedToBeShared;

	@Column(name="request_share_email")
	private String requesterEmail;

	@Column(name="available_for_all_domains")
	private boolean availableForAllDomains;

	@Column(name="available_for_all_tags")
	private boolean availableForAllTags;

	@Column(name="relevant_for_onpremise")
	private boolean relevantForOnPremise;

	@Column(name="relevant_for_saas")
	private boolean relevantForSaas;

	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column
	private Date lastChanged;

	@Enumerated(EnumType.STRING)
	private Importance importance;

	@OneToOne
	private Category category;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "requirement_domain", joinColumns = { @JoinColumn(name = "requirement_id") }, inverseJoinColumns = { @JoinColumn(name = "domain_id") })
	private List<Domain> domains;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "requirement_tag", joinColumns = { @JoinColumn(name = "requirement_id") }, inverseJoinColumns = { @JoinColumn(name = "tag_id") })
	private List<Tag> tags;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "requirement_principle", joinColumns = { @JoinColumn(name = "requirement_id") }, inverseJoinColumns = { @JoinColumn(name = "architecture_principle_id") })
	private List<ArchitecturePrinciple> principles;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "requirement", orphanRemoval = true)
	private List<Attachment> attachments;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "subcategory_id")
	private Subcategory subcategory;

	@Override
	public String getEntityId() {
		return Long.toString(id);
	}

	public boolean isShared(){
		return Constants.DEFAULT_CVR.equals(this.getCvr());
	}
}
