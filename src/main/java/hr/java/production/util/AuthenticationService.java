package hr.java.production.util;

import hr.java.production.dao.txt.UserDao;
import hr.java.production.exception.AuthenticationException;
import hr.java.production.model.User;

public class AuthenticationService {
    private final UserDao userDao = new UserDao();

    /**
     * Vraća korisnika nakon validacije korisničkog imena i lozinke.
     *
     * @param username korisničko ime koje se prijavljuje
     * @param passwordPlaintext lozinka u običnom tekstu za validaciju
     * @return korisnik koji je uspješno validiran
     * @throws AuthenticationException ako korisnik ne postoji ili je lozinka neispravna
     */
    public User login(String username, String passwordPlaintext) {
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("Ne postoji korisnik"));

        if (!PasswordUtils.verify(passwordPlaintext, user.hashedPassword())) {
            throw new AuthenticationException("Neispravna lozinka");
        }
        return user;
    }
}