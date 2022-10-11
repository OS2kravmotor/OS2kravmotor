package dk.digitalidentity.re.mvc.form;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dk.digitalidentity.re.dao.model.PurchaseVendor;
import dk.digitalidentity.re.dao.model.enums.Status;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchaseForm {
	private long id;
	private String title;
	private String description;
	private String status;
	private String email;
	private Date startTime;
	private Date endTime;
	private List<String> domains = new ArrayList<>();
	private List<RequirementForm> purchases = new ArrayList<>();
	private PurchaseVendor winner;
	private boolean requirementPriorityEnabled = true;
	// stupid ModelMapper setup will not work with two of the same type in the hierarchy
//	private PurchaseAnswer winnerPurchaseAnswer;

	public boolean isDone() {
		return status.equals(Status.ARCHIVED.toString()) || status.equals(Status.COMPLETED.toString());
	}

	public boolean hasWinner() {
		return winner != null;// || winnerPurchaseAnswer != null;
	}
}
