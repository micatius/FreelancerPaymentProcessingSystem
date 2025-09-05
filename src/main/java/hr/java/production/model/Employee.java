package hr.java.production.model;

import hr.java.production.exception.ObjectValidationException;

/**
 * Klasa Employee predstavlja zaposlenika koji nasljeđuje osnovne atribute iz klase Worker
 * i dodaje specifične informacije poput datuma zapošljavanja, datuma raskida ugovora,
 * odjela u kojem je zaposlenik zaposlen te plaće.
 */
public class Employee extends Worker {
    private Department department;

    public Employee(String firstName, String lastName, String email, String phoneNumber, Address address,
                    Department department) {
        super(firstName, lastName, email, phoneNumber, address);
        this.department = department;
    }

    public Employee(Long id, String firstName, String lastName, String email, String phoneNumber, Address address,
                    Department department
                    ) {
        super(id, firstName, lastName, email, phoneNumber, address);
        this.department = department;
    }

    public static class Builder extends Worker.Builder<Employee, Builder> {
        private Department department;

        public Builder department(Department department) {
            this.department = department;
            return self();
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public Employee build() {
            validateWorkerFields();
            if (department == null) {
                throw new ObjectValidationException("Odjel je obavezan");
            }
            return new Employee(
                    id, firstName, lastName, email, phoneNumber, address, department
            );
        }
    }

    public Department getDepartment() {
        return department;
    }

    public Employee setDepartment(Department department) {
        this.department = department;
        return this;
    }

    @Override
    public Role getRole() {
        return department == Department.FINANCE ? Role.FINANCE : Role.ADMIN;
    }


    @Override
    public String toString() {
        return "Employee{" +
                ", department=" + department +
                '}';
    }
}
