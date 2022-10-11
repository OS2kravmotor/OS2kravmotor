package dk.digitalidentity.re.service;

import java.security.SecureRandom;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dk.digitalidentity.re.dao.PurchaseAnswerDao;
import dk.digitalidentity.re.dao.PurchaseDao;
import dk.digitalidentity.re.dao.VendorOrganizationDao;
import dk.digitalidentity.re.dao.VendorUserDao;
import dk.digitalidentity.re.security.SecurityUtil;

@Service
public class VendorService {

    @Autowired
    VendorOrganizationDao vendorOrganizationDao;

    @Autowired
    VendorUserDao vendorUserDao;

    @Autowired
    PurchaseAnswerDao purchaseAnswerDao;

    @Autowired
    PurchaseDao purchaseDao;

    @Autowired
    SecurityUtil securityUtil;

    private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!@#$?_";
    private static final String special = "!@#$?_";
    private static SecureRandom rnd = new SecureRandom();
    private static final int length = 10;

    public String getRandomPassword() {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char c = AB.charAt(rnd.nextInt(AB.length()));
            if (special.indexOf(c) != -1) {
                long count = sb.toString().chars().mapToObj(cx -> (char) cx).collect(Collectors.toList()).stream().filter(cc -> special.indexOf(cc) != -1).count();
                if (count >= 2) {
                    --i;
                    continue;
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public String getRandomUsername() {
        int n = 10000 + rnd.nextInt(90000);
        return "vendor" + n;
    }

}
