package dk.digitalidentity.re.dao.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import dk.digitalidentity.re.dao.model.enums.AnswerChoice;
import lombok.Data;
import lombok.ToString;

@Entity
@Data
@ToString(exclude = { "purchaseVendor" })
public class PurchaseVendorAnswer {

	@Id
	@Column
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column
	private String detail;

	@Column
	@Enumerated(EnumType.STRING)
	private AnswerChoice choice;

	@Column
	private String price;

	@OneToOne
	@JoinColumn(name = "purchase_requirement_id")
	@JsonBackReference
	private PurchaseRequirement requirement;

	@ManyToOne
	@JoinColumn(name = "purchase_vendor_id")
	@JsonManagedReference
	private PurchaseVendor purchaseVendor;
}
