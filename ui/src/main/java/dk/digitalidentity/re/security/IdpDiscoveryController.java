package dk.digitalidentity.re.security;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import dk.digitalidentity.re.dao.IdentityProviderDao;
import dk.digitalidentity.re.dao.model.IdentityProvider;

@Controller
public class IdpDiscoveryController {

	@Autowired
	private IdentityProviderDao identityProviderDao;

	@RequestMapping(path = "discovery", method = RequestMethod.GET)
	public String discovery(Model model) {

		List<IdentityProvider> result = identityProviderDao.findAll().stream()
				.sorted((o1, o2) -> o1.getName().compareTo(o2.getName()))
				.collect(Collectors.toList());

		model.addAttribute("identityProviders", result);

		return "discovery";
	}
}
