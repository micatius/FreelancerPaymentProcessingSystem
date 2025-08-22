package hr.java.production.main;

import hr.java.production.dao.db.AddressDao;
import hr.java.production.dao.db.FreelancerDao;
import hr.java.production.dao.txt.UserDao;
import hr.java.production.exception.DatabaseAccessException;
import hr.java.production.exception.DatabaseException;
import hr.java.production.model.Address;
import hr.java.production.model.Freelancer;
import hr.java.production.util.PasswordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class Test {
    public static final Logger logger = LoggerFactory.getLogger(Test.class);
    public static void main(String[] args) {
//        UserDao userDao = new UserDao();

//        userDao.save("djuro", "lozinka1", Role.ADMIN, 1L);
//        userDao.save("mirko", "lozinka2", Role.ADMIN, 2L);
//        userDao.save("jurica", "lozinka3", Role.FINANCE, 3L);
//        userDao.save("vesna", "lozinka4", Role.FINANCE, 4L);
//        userDao.save("alen", "lozinka5", Role.FREELANCER, 1L);
//        userDao.save("goran", "lozinka6", Role.FREELANCER, 2L);
//        boolean valid = userDao.findByUsername("jurica")
//                .map(user -> PasswordUtils.verify("lozinka4", user.hashedPassword()))
//                .orElse(false);
//
//        logger.info("Password correct: {}", valid);

        FreelancerDao freelancerDao = new FreelancerDao();


        try {
            Optional<Freelancer> f = freelancerDao.findById(1L);
           // addressDao.save(new Address.Builder().street("Kralja Pesa").houseNumber("1").city("Zagreb").postalCode("10000").build());
            List<Freelancer> freelancerList = freelancerDao.findAll();
            freelancerList.forEach(System.out::println);
        } catch (DatabaseException e) {
            throw new RuntimeException(e);
        }

    }
}
