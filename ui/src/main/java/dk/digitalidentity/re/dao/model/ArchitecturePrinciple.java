package dk.digitalidentity.re.dao.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.envers.Audited;

import dk.digitalidentity.re.log.EventLoggable;
import lombok.Data;

@Entity
@Audited
@Data
public class ArchitecturePrinciple implements EventLoggable {

	@Id
	@Column
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column
	private String name;

	@Column
	private String reference;

	@Override
	public String getEntityId() {
		return Long.toString(id);
	}
}
