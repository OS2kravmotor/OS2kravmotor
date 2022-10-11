package dk.digitalidentity.re.dao.model;

import java.util.Date;
import java.util.List;

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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import dk.digitalidentity.re.dao.model.enums.Status;
import dk.digitalidentity.re.log.EventLoggable;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Purchase implements EventLoggable {

	@Id
	@Column
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column
	private String title;

	@Column
	private String description;
	
	@Column
	private String cvr;

	@Column
	@Enumerated(EnumType.STRING)
	private Status status;
	
	@Column(name="questionnaire_filled_out")
	private boolean questionnaireFilledOut;

	@Column
	private Date startTime;

	@Column
	private Date endTime;

	@Column
	private String email;
	
	@Column
	private boolean requirementPriorityEnabled;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "purchase")
	private List<PurchaseRequirement> requirements;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinTable(name="purchase_domain", joinColumns = @JoinColumn(name =  "purchase_id"), inverseJoinColumns = @JoinColumn(name = "domain_id"))
	private List<Domain> domains;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "purchase")
	private List<PurchaseAnswer> purchaseAnswers;

	@OneToOne
	private PurchaseVendor winner;

	@OneToOne
	private PurchaseAnswer winnerPurchaseAnswer;

	@Override
	public String getEntityId() {
		return Long.toString(id);
	}

	public boolean isDone() {
		return status.equals(Status.ARCHIVED) || status.equals(Status.COMPLETED);
	}

	public boolean hasWinner() {
		return winner != null || winnerPurchaseAnswer != null;
	}
}
