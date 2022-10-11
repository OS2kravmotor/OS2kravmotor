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
public class Setting implements EventLoggable {

	@Id
	@Column
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name="setting_key")
	private String key;

	@Column(name="setting_value")
	private String value;
	
	@Column
	private String cvr;

	@Override
	public String getEntityId() {
		return Long.toString(id);
	}
}
