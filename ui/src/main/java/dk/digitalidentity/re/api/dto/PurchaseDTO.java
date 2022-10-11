package dk.digitalidentity.re.api.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("serial")
@Getter
@Setter
public class PurchaseDTO implements Serializable {
	private long id;
	private String title;
	private String description;
	private String status;
	private String email;
	private Date startTime;
	private Date endTime;
}
