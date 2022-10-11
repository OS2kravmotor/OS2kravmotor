package dk.digitalidentity.re.dao.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import dk.digitalidentity.re.log.EventLoggable;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class VendorUser implements EventLoggable {

	@Id
	@Column
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column
	private String email;

	@Column
	private String password;

	@Column
	private boolean admin;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vendor_organization_id")
	private VendorOrganization vendorOrganization;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "vendor_user_purchase_answer", joinColumns = { @JoinColumn(name = "vendor_user_id") }, inverseJoinColumns = { @JoinColumn(name = "purchase_answer_id") })
	private List<PurchaseAnswer> purchaseAnswers = new ArrayList<>();

	@Override
	public String getEntityId() {
		return Long.toString(id);
	}
}
