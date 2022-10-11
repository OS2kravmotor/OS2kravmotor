package dk.digitalidentity.re.dao;

import dk.digitalidentity.re.dao.model.VendorOrganization;
import org.springframework.data.repository.CrudRepository;

public interface VendorOrganizationDao extends CrudRepository<VendorOrganization, Long> {
    VendorOrganization getById(long id);
    VendorOrganization getByDomain(String domain);

}
