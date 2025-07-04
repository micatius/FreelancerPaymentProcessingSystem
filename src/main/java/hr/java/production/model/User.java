package hr.java.production.model;

public record User(String username, String hashedPassword, Role role, long linkedEntityId) {}
