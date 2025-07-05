package hr.java.production.model;

import hr.java.production.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Klasa Employee predstavlja zaposlenika koji nasljeđuje osnovne atribute iz klase Worker
 * i dodaje specifične informacije poput datuma zapošljavanja, datuma raskida ugovora,
 * odjela u kojem je zaposlenik raspoređen te mjesečne plaće.
 */
public class Employee extends Worker {
    private LocalDate hireDate;
    private LocalDate terminationDate;
    private Department department;
    private BigDecimal salary;

    public Employee(String firstName, String lastName, String email, String phoneNumber, Address address,
                    LocalDate hireDate, LocalDate terminationDate, Department department,
                    BigDecimal salary) {
        super(firstName, lastName, email, phoneNumber, address);
        this.hireDate = hireDate;
        this.terminationDate = terminationDate;
        this.department = department;
        this.salary = salary;
    }

    public Employee(Long id, String firstName, String lastName, String email, String phoneNumber, Address address,
                    LocalDate hireDate, LocalDate terminationDate, Department department,
                    BigDecimal salary) {
        super(id, firstName, lastName, email, phoneNumber, address);
        this.hireDate = hireDate;
        this.terminationDate = terminationDate;
        this.department = department;
        this.salary = salary;
    }

    public static class Builder extends Worker.Builder<Employee, Builder> {
        private LocalDate hireDate;
        private LocalDate terminationDate;
        private Department department;
        private BigDecimal salary;

        public Builder hireDate(LocalDate hireDate) {
            this.hireDate = hireDate;
            return self();
        }

        public Builder terminationDate(LocalDate terminationDate) {
            this.terminationDate = terminationDate;
            return self();
        }

        public Builder department(Department department) {
            this.department = department;
            return self();
        }

        public Builder salary(BigDecimal salary) {
            this.salary = salary;
            return self();
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public Employee build() {
            validateWorkerFields();
            if (hireDate == null) {
                throw new ValidationException("Datum zapošljavanja je obavezan");
            }
            if (department == null) {
                throw new ValidationException("Odjel je obavezan");
            }
            if (salary == null || salary.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("Plaća mora biti pozitivna");
            }
            return new Employee(
                    id, firstName, lastName, email, phoneNumber, address,
                    hireDate, terminationDate, department, salary
            );
        }
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public Employee setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
        return this;
    }

    public Optional<LocalDate> getTerminationDate() {
        return Optional.ofNullable(terminationDate);
    }

    public Employee setTerminationDate(LocalDate terminationDate) {
        this.terminationDate = terminationDate;
        return this;
    }

    public Department getDepartment() {
        return department;
    }

    public Employee setDepartment(Department department) {
        this.department = department;
        return this;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public Employee setSalary(BigDecimal salary) {
        this.salary = salary;
        return this;
    }

    @Override
    public Role getRole() {
        return department == Department.FINANCE ? Role.FINANCE : Role.ADMIN;
    }
}
