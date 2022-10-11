package dk.digitalidentity.re.dao.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import dk.digitalidentity.re.dao.model.enums.Importance;
import dk.digitalidentity.re.log.EventLoggable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "purchase_requirement")
public class PurchaseRequirement implements EventLoggable {

	@Id
	@Column
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column
	private long requirementId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "purchase_id")
	private Purchase purchase;

	@Column
	private String name;

	@Column
	private String description;

	@Column
	private String rationale;

	@Column
	private boolean infoRequirement;

	@Temporal(TemporalType.TIMESTAMP)
	@Column
	private Date lastChanged;

	@OneToOne
	private Category category;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "subcategory_id")
	private Subcategory subcategory;

	@Column
	@Enumerated(EnumType.STRING)
	private Importance importance;

	@Override
	public String getEntityId() {
		return Long.toString(id);
	}
}
