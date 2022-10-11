package dk.digitalidentity.re.dao;

import java.util.Date;

import org.springframework.data.repository.CrudRepository;

import dk.digitalidentity.re.dao.model.DownloadLog;

public interface DownloadLogDao extends CrudRepository<DownloadLog, Long> {
	int countByTimestampAfter(Date date);
}
