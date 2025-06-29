package hr.java.model;

public class User extends Person {

    protected User(Long id, String firstName, String lastName) {
        super(id, firstName, lastName);
    }

    protected User(String firstName, String lastName) {
        super(firstName, lastName);
    }
}
