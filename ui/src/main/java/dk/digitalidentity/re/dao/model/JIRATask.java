package dk.digitalidentity.re.dao.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name="jira_task")
public class JIRATask {

	@Id
	@Column
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name = "issue_key")
	private String key;

	@Column
	private String summary;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "jira_sprint_id")
	private JIRASprint sprint;
}
