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
import dk.digitalidentity.re.dao.model.enums.CustomerAnswer;
import lombok.Data;
import lombok.ToString;

@Entity
@Data
@ToString(exclude = { "purchaseAnswer" })
public class PurchaseRequirementAnswer implements Comparable<PurchaseRequirementAnswer> {

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

	@Column
	private boolean answered;

	@Column
	private boolean dirtyCopy;
	
	@Column
	@Enumerated(EnumType.STRING)
	private CustomerAnswer customerAnswer;
	
	@Column
	private String customerComment;

	@OneToOne
	@JoinColumn(name = "purchase_requirement_id")
	@JsonBackReference
	private PurchaseRequirement requirement;

	@ManyToOne
	@JoinColumn(name = "purchase_answer_id")
	@JsonManagedReference
	private PurchaseAnswer purchaseAnswer;

	// compareTo to used to sort lists by requirement id
	@Override
	public int compareTo(PurchaseRequirementAnswer o) {
		return Long.compare(this.getRequirement().getRequirementId(),o.getRequirement().getRequirementId());
	}
}
