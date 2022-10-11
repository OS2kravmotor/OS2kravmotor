package dk.digitalidentity.re.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import dk.digitalidentity.re.dao.CmsMessageDao;
import dk.digitalidentity.re.dao.model.CmsMessage;

@Service
public class CmsMessageService {
	private Map<String, String> cmsMap;
	private LocalDateTime lastCheckedForUpdates = LocalDateTime.now();

	@Autowired
	private CmsMessageDao cmsMessageDao;
	
	public CmsMessage save(CmsMessage cmsMessage) {
		return cmsMessageDao.save(cmsMessage);
	}
	
	public CmsMessage getByCmsKey(String key) {
		return cmsMessageDao.findByCmsKey(key);
	}

	public void deleteById(long id) {
		cmsMessageDao.deleteById(id);
	}
	
	public Map<String, String> getCmsMap() {
		return cmsMap;
	}
	
	@Scheduled(fixedRate = 5 * 60 * 1000)
	public void updateCmsMap() {
		if (cmsMap == null || cmsMap.isEmpty()) {
			cmsMap = cmsMessageDao.findAll().stream().collect(Collectors.toMap(CmsMessage::getCmsKey, CmsMessage::getCmsValue));
		}
		else {
			List<CmsMessage> cmsMessages = cmsMessageDao.findAllByLastUpdatedAfter(lastCheckedForUpdates.minusMinutes(1L));
			for (CmsMessage cmsMessage : cmsMessages) {
				cmsMap.put(cmsMessage.getCmsKey(), cmsMessage.getCmsValue());
			}
		}
	}	
}