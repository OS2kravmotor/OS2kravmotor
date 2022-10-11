package dk.digitalidentity.re.security;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import dk.digitalidentity.re.dao.GlobalEditorDao;
import dk.digitalidentity.re.dao.model.Community;
import dk.digitalidentity.re.dao.model.GlobalEditor;
import dk.digitalidentity.re.service.CommunityService;
import dk.digitalidentity.saml.extension.SamlLoginPostProcessor;
import dk.digitalidentity.saml.model.TokenUser;

@Component
public class RolePostProcesser implements SamlLoginPostProcessor {

	@Autowired
	private GlobalEditorDao globalEditorDao;

	@Autowired
	private CommunityService communityService;
	
	@Override
	public void process(TokenUser tokenUser) {
		List<GrantedAuthority> newAuthorities = new ArrayList<>();

		// default these roles to false
		tokenUser.getAttributes().put("editor", false);
		tokenUser.getAttributes().put("purchaser", false);
		tokenUser.getAttributes().put("admin", false);

		for (Iterator<? extends GrantedAuthority> iterator = tokenUser.getAuthorities().iterator(); iterator.hasNext();) {
			GrantedAuthority grantedAuthority = iterator.next();
			
			if ("ROLE_http://kravmotoren.dk/editor".equals(grantedAuthority.getAuthority())) {
				newAuthorities.add(new SimpleGrantedAuthority("ROLE_http://kravmotoren.dk/editor"));
				tokenUser.getAttributes().put("editor", true);
			}
			else if ("ROLE_http://kravmotoren.dk/purchaser".equals(grantedAuthority.getAuthority())) {
				newAuthorities.add(new SimpleGrantedAuthority("ROLE_http://kravmotoren.dk/purchaser"));
				tokenUser.getAttributes().put("purchaser", true);
			}
			else if ("ROLE_http://kravmotoren.dk/admin".equals(grantedAuthority.getAuthority())) {
				newAuthorities.add(new SimpleGrantedAuthority("ROLE_http://kravmotoren.dk/admin"));
				tokenUser.getAttributes().put("admin", true);
			}
		}

		// add global editor role to configured set of global editors
		boolean foundGlobalAdmin = false;
		for (GlobalEditor globalEditor : globalEditorDao.findAll()) {
			if (tokenUser.getUsername().equals(globalEditor.getUserId()) && globalEditor.getCvr().equals(tokenUser.getCvr())) {
				foundGlobalAdmin = true;
				break;
			}
		}
		
		List<Community> communities = communityService.getCommunities(tokenUser.getCvr());
		tokenUser.getAttributes().put("communityMember", (communities != null && communities.size() > 0));		
		tokenUser.getAttributes().put("globalEditor", foundGlobalAdmin);
		tokenUser.getAttributes().put("originalCvr", tokenUser.getCvr());

		tokenUser.setAuthorities(newAuthorities);
	}
}
