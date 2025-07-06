package hr.java.production.model;

/**
 * Predstavlja korisnika s atributima kao što su korisničko ime, lozinka u hash obliku,
 * uloga korisnika i ID povezanog entiteta. Podaci se spremaju kao nepromjenjivo stanje.
 */
public record User(String username, String hashedPassword, Role role, Long linkedEntityId) {}
