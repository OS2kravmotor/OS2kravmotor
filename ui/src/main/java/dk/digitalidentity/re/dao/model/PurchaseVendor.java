package dk.digitalidentity.re.dao.model;

import java.util.Date;
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

import com.fasterxml.jackson.annotation.JsonBackReference;

import dk.digitalidentity.re.log.EventLoggable;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class PurchaseVendor implements EventLoggable {

	@Id
	@Column
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column
	private String name;

	@Column
	private String email;
	
	@Column
	private String username;
	
	@Column
	private String password;

	@Column
	private Date timestamp;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "it_system_id")
	private ItSystem itSystem;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "purchase_id")
	@JsonBackReference
	private Purchase purchase;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "purchaseVendor", orphanRemoval = true, cascade = CascadeType.ALL)
	@JsonBackReference
	private List<PurchaseVendorAnswer> details;

	@Override
	public String getEntityId() {
		return Long.toString(id);
	}
}
