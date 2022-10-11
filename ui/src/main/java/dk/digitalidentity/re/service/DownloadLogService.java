package dk.digitalidentity.re.service;

import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dk.digitalidentity.re.dao.DownloadLogDao;
import dk.digitalidentity.re.dao.model.DownloadLog;

@Service
public class DownloadLogService {

	@Autowired
	private DownloadLogDao downloadLogDao;

	public DownloadLog save(DownloadLog entity) {
		return downloadLogDao.save(entity);
	}

	public int countAllDownloadsInLast3Months() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -3);
		Date date = cal.getTime();

		return downloadLogDao.countByTimestampAfter(date);
	}
}
