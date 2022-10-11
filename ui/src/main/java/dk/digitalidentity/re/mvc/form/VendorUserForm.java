package dk.digitalidentity.re.mvc.form;

import java.util.ArrayList;
import java.util.List;

import dk.digitalidentity.re.dao.model.PurchaseAnswer;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VendorUserForm {
	private long id;
	private String email;
	private boolean admin;

	private List<Long> purchaseAnswerIds = new ArrayList<>();
	private List<PurchaseAnswer> purchaseAnswers = new ArrayList<>();
}