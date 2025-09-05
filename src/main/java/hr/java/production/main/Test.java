package hr.java.production.main;


import hr.java.production.exception.DatabaseException;
import hr.java.production.log.BinaryChangeLogger;
import hr.java.production.log.ChangeLog;
import hr.java.production.model.Entity;
import hr.java.production.model.Freelancer;
import hr.java.production.service.InvoiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Test {
    public static final Logger logger = LoggerFactory.getLogger(Test.class);
    public static void main(String[] args) throws DatabaseException {
//        UserDao userDao = new UserDao();

//        userDao.save("djuro", "lozinka1", Role.ADMIN, 1L);
//        userDao.save("mirko", "lozinka2", Role.ADMIN, 2L);
//        userDao.save("jurica", "lozinka3", Role.FINANCE, 3L);
//        userDao.save("vesna", "lozinka4", Role.FINANCE, 4L);
//        userDao.save("ivan", "lozinka5", Role.FREELANCER, 1L);
//        userDao.save("goran", "lozinka6", Role.FREELANCER, 2L);
//        boolean valid = userDao.findByUsername("jurica")
//                .map(user -> PasswordUtils.verify("lozinka4", user.hashedPassword()))
//                .orElse(false);
//
//        logger.info("Password correct: {}", valid);

        InvoiceService is = new InvoiceService();

        List<InvoiceService.InvoiceView> invoices = is.findAll();
        System.out.println(invoices);
        BinaryChangeLogger cl = new BinaryChangeLogger();
        List<ChangeLog<Freelancer>> logs = cl.readAll(Freelancer.class);

        System.out.println(logs);

    }
}
