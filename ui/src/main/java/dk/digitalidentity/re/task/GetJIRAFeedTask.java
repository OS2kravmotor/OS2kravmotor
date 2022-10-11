package dk.digitalidentity.re.task;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import dk.digitalidentity.re.dao.model.JIRASprint;
import dk.digitalidentity.re.dao.model.JIRATask;
import dk.digitalidentity.re.mvc.dto.JIRAIssueDTO;
import dk.digitalidentity.re.mvc.dto.JIRAIssuesDTO;
import dk.digitalidentity.re.mvc.dto.JIRASprintDTO;
import dk.digitalidentity.re.mvc.dto.JIRASprintsDTO;
import dk.digitalidentity.re.service.JIRASprintService;
import lombok.extern.log4j.Log4j;

@Component
@EnableScheduling
@Log4j
public class GetJIRAFeedTask {

	@Value("${scheduled.enabled:false}")
	private boolean runScheduled;

	@Autowired
	private JIRASprintService jiraSprintService;
	
	// update once every four hours
	@Scheduled(fixedRate = 4 * 60 * 60 * 1000)
	public void getFeed() throws IOException {
		if (!runScheduled || !jiraSprintService.isConfigured()) {
			log.debug("Scheduling is disabled on this instance");
			return;
		}

		JIRASprintsDTO sprints = jiraSprintService.getSprints();

		for (JIRASprintDTO jiraSprint : sprints.getValues()) {
			JIRASprint sprint = new JIRASprint();
			sprint.setId(jiraSprint.getId());
			sprint.setName(jiraSprint.getName());
			sprint.setStartDate(jiraSprint.getStartDate());
			sprint.setEndDate(jiraSprint.getEndDate());
			sprint.setState(jiraSprint.getState());
			sprint.setTasks(new ArrayList<>());

			JIRAIssuesDTO issues = jiraSprintService.getIssues(jiraSprint.getId());

			for (JIRAIssueDTO issueDTO : issues.getIssues()) {
				JIRATask task = new JIRATask();
				task.setId(issueDTO.getId());
				task.setKey(issueDTO.getKey());
				task.setSummary(issueDTO.getFields().getSummary());

				task.setSprint(sprint);
				sprint.getTasks().add(task);
			}

			// if everything went as expected, update the sprint in the database
			jiraSprintService.save(sprint);
		}
	}
}