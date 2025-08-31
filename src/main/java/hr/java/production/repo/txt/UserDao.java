package hr.java.production.repo.txt;

import hr.java.production.model.Role;
import hr.java.production.model.User;
import hr.java.production.util.PasswordUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Klasa omogućava čitanje iz tekstualne datoteke korisnika.
 * Pruža metode za dohvat svih korisnika i pretragu korisnika po korisničkom imenu.
 */
public class UserDao {
    private static final String USER_FILE = "dat/txt/users.txt";

    /**
     * Dohvaća sve korisnike iz datoteke i vraća ih kao listu objekata tipa User.
     *
     * @return lista svih korisnika učitanih iz datoteke
     */
    public List<User> findAll() {
        try(Stream<String> lines = Files.lines(Path.of(USER_FILE))) {
            return lines
                    .map(line -> {
                        var parts = line.split(";", 4);
                        return new User(parts[0], parts[1], Role.valueOf(parts[2]), Long.parseLong(parts[3]));
                    })
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    /**
     * Sprema korisnika u tekstualnu datoteku s podacima o korisnicima.
     *
     * @param username korisničko ime korisnika
     * @param passwordPlaintext lozinka korisnika u običnom tekstualnom obliku
     * @param role uloga korisnika
     * @param linkedEntityId ID povezanog entiteta
     */
    public void save(String username, String passwordPlaintext, Role role, Long linkedEntityId) {
        String hashed = PasswordUtils.hash(passwordPlaintext);

        String line = String.join(";",
                username,
                hashed,
                role.name(),
                linkedEntityId.toString()
        );

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE, true))) {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            throw new UncheckedIOException("Greška u zapisu korisnika", e);
        }
    }

    /**
     * Pronalazi korisnika prema proslijeđenom korisničkom imenu.
     *
     * @param username korisničko ime koje se traži
     * @return objekt tipa Optional koji sadrži korisnika ako postoji, ili prazan Optional ako nije pronađen
     */
    public Optional<User> findByUsername(String username) {
        return findAll().stream()
                .filter(u -> u.username().equals(username))
                .findFirst();
    }
}