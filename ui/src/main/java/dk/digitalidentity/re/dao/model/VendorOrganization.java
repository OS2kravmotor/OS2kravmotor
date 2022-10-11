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
import javax.persistence.OneToMany;

import dk.digitalidentity.re.log.EventLoggable;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class VendorOrganization implements EventLoggable {

	@Id
	@Column
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column
	private String domain;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "vendor_organization_id")
	private List<PurchaseAnswer> purchaseAnswers = new ArrayList<>();

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "vendor_organization_id")
	private List<VendorUser> vendorUsers = new ArrayList<>();

	@Override
	public String getEntityId() {
		return Long.toString(id);
	}
}
