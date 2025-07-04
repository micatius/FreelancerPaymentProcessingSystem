package hr.java.production.model;

public enum Role {
    FREELANCER("Freelancer"),
    EMPLOYEE("Internal Employee"),
    FINANCE("Finance Team");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}