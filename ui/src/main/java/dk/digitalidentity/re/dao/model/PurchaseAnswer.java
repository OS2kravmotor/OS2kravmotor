package dk.digitalidentity.re.dao.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import dk.digitalidentity.re.dao.model.enums.SolutionType;
import dk.digitalidentity.re.log.EventLoggable;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class PurchaseAnswer implements EventLoggable {

	@Id
	@Column
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column
	private Date created;
	
	@Column
	private boolean vendorMustElaborate;

	@Column
	private boolean doneAnswering;

	@Column
	@Enumerated(EnumType.STRING)
	private SolutionType solutionType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "it_system_id")
	private ItSystem itSystem;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "purchase_id")
	private Purchase purchase;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "purchaseAnswer", orphanRemoval = true, cascade = CascadeType.ALL)
	@JsonBackReference
	private List<PurchaseRequirementAnswer> purchaseRequirementAnswers;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "vendor_user_purchase_answer", joinColumns = { @JoinColumn(name = "purchase_answer_id") }, inverseJoinColumns = { @JoinColumn(name = "vendor_user_id") })
	private List<VendorUser> vendorUsers = new ArrayList<>();

	@ManyToOne
	@JoinColumn(name = "vendor_organization_id")
	@JsonManagedReference
	private VendorOrganization vendorOrganization;

	@Override
	public String getEntityId() {
		return Long.toString(id);
	}

	public List<PurchaseRequirementAnswer> getPurchaseRequirementAnswers() {
		purchaseRequirementAnswers.sort(PurchaseRequirementAnswer::compareTo);
		return purchaseRequirementAnswers;
	}
}
