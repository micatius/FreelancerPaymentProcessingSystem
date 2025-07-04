package hr.java.production.model;

public enum Department {
    HR("Human Resources"),
    FINANCE("Finance Team"),
    PRODUCTION("Production"),
    SALES("Sales");

    private final String displayName;

    Department(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}