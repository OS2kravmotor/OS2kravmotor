package dk.digitalidentity.re.mvc.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import dk.digitalidentity.re.dao.model.Community;
import dk.digitalidentity.re.mvc.dto.CommunityData;
import dk.digitalidentity.re.mvc.dto.NavData;
import dk.digitalidentity.re.security.SecurityUtil;
import dk.digitalidentity.re.service.CommunityService;

@Controller
public class CommunityController {

	@Autowired
	private CommunityService communityService;

	@Autowired
	private SecurityUtil securityUtil;

	@GetMapping(value = { "/getCommunityNavData" })
	public ResponseEntity<NavData> getCommunityNavData(HttpServletRequest request) {
		Map<Long, String> communities = new HashMap<>();
		List<Community> srcCommunities = new ArrayList<>();
		
		String cvr = SecurityUtil.getMunicipalityCvr();
		if (cvr != null) {
			srcCommunities = communityService.getCommunities(cvr);
			communities = srcCommunities.stream().collect(Collectors.toMap(Community::getId, Community::getName));
		}

		// Extract the community from json and append selection
		String selectedCvr = securityUtil.getCvr();
		List<CommunityData> communityDataList = new ArrayList<>();
		for (Entry<Long, String> entry : communities.entrySet()) {
			CommunityData communityData = new CommunityData();
			communityData.setId(entry.getKey());
			communityData.setName(entry.getValue());
			communityData.setSelected(srcCommunities.stream().filter(c -> c.getId() == entry.getKey()).findAny().get().getCommunityCvr().equals(selectedCvr));

			communityDataList.add(communityData);
		}
		
		// add global editor context if logged in person is globalEditor
		if (SecurityUtil.getBooleanAttribute("globalEditor")) {
			CommunityData communityData = new CommunityData();
			communityData.setId(-1);
			communityData.setName("Kravredaktion");
			communityData.setSelected(securityUtil.getCvr().equals("00000000"));

			communityDataList.add(communityData);
		}

		NavData navData = new NavData();
		navData.setCommunityContext((request.getSession().getAttribute("cvr") != null));
		navData.setCommunities(communityDataList);

		return new ResponseEntity<NavData>(navData, HttpStatus.OK);
	}

	@GetMapping(value = { "/getCommunities" })
	public ResponseEntity<List<CommunityData>> getCommunities() {
		List<CommunityData> communities = new ArrayList<>();

		String cvr = securityUtil.getCvr();
		if (cvr != null) {
			communities = communityService.getCommunities(cvr).stream().map(community -> {
				CommunityData communityData = new CommunityData();
				communityData.setId(community.getId());
				communityData.setName(community.getName());

				return communityData;
			}).collect(Collectors.toList());
		}

		return new ResponseEntity<List<CommunityData>>(communities, HttpStatus.OK);
	}

	@GetMapping(value = { "/switchContext/{id}" })
	public String switchContext(@PathVariable("id") long id, HttpServletRequest request) {
		
		// -1 means switch to global editor
		if (id == -1 && SecurityUtil.getBooleanAttribute("globalEditor")) {
			request.getSession().setAttribute("cvr", "00000000");
			request.getSession().removeAttribute("name");
			securityUtil.switchToGlobalEditor();
		}
		else if (id >= 1) {
			Community community = communityService.getById(id);

			if (community != null) {
				String municipalityCvr = SecurityUtil.getMunicipalityCvr();
				
				if (community.getCommunityMembers().stream().anyMatch(m -> municipalityCvr.equals(m.getMunicipalityCvr()))) {
					request.getSession().setAttribute("cvr", community.getCommunityCvr());
					request.getSession().setAttribute("name", community.getName());
				}
			}
		}
		
		return "redirect:/";
	}

	@GetMapping(value = { "/normalContext" })
	public String normalContext(HttpServletRequest request) {
		String cvr = (String) request.getSession().getAttribute("cvr"); 
		if ("00000000".equals(cvr)) {
			securityUtil.switchFromGlobalEditor();
		}
		
		request.getSession().removeAttribute("cvr");
		request.getSession().removeAttribute("name");

		return "redirect:/";
	}
}
