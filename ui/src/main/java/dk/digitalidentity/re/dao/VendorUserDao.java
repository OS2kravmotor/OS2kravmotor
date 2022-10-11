package dk.digitalidentity.re.dao;

import org.springframework.data.repository.CrudRepository;

import dk.digitalidentity.re.dao.model.VendorUser;

public interface VendorUserDao extends CrudRepository<VendorUser, Long> {
    VendorUser getById(long id);
    VendorUser getByEmail(String email);
}
