package dk.digitalidentity.re.dao.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.hibernate.envers.Audited;

import dk.digitalidentity.re.log.EventLoggable;
import lombok.Getter;
import lombok.Setter;

@Entity
@Audited
@Getter
@Setter
public class Category implements EventLoggable {

	@Id
	@Column
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column
	private String name;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "category", orphanRemoval = true, cascade = CascadeType.ALL)
	private List<Subcategory> subcategories;

	@Override
	public String getEntityId() {
		return Long.toString(id);
	}
}
