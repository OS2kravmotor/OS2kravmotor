package dk.digitalidentity.re.dao.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import dk.digitalidentity.re.log.EventLoggable;
import lombok.Data;

@Entity
@Data
public class GlobalEditor implements EventLoggable {

	@Id
	@Column
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column
	private String userId;
	
	@Column
	private String cvr;

	@Column
	private String email;

	@Override
	public String getEntityId() {
		return Long.toString(id);
	}
}
