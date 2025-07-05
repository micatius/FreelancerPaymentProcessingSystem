package hr.java.production.model;

public enum Role {
    FREELANCER("Freelancer"),
    ADMIN("Administration Team"),
    FINANCE("Finance Team");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}