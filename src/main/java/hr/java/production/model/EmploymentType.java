package hr.java.production.model;

/**
 * Enumeracija koja predstavlja tip zaposlenja radnika.
 */
public enum EmploymentType {
    FULL_TIME("Puno radno vrijeme"),
    PART_TIME("Nepuno radno vrijeme"),
    INTERN("Pripravnik");

    private final String displayName;

    EmploymentType(String displayName) {
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