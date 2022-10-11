package dk.digitalidentity.re.dao.model;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import dk.digitalidentity.re.dao.model.enums.JIRASprintState;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name="jira_sprint")
public class JIRASprint {

	@Id
	@Column
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	@Enumerated(EnumType.STRING)
	private JIRASprintState state;

	@Column
	private String name;

	@Column
	private Date startDate;

	@Column
	private Date endDate;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "sprint", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<JIRATask> tasks;
}
