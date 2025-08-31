package hr.java.production.model;

import hr.java.production.exception.ObjectValidationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Klasa koja predstavlja račun (invoice) izdan za određenog freelancera.
 * Račun sadrži datum izdavanja, datum dospijeća, stavke usluga i status plaćenosti.
 */
public class Invoice extends Entity {
    private Freelancer freelancer;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private List<Service> services;
    private Boolean paid;

    private Invoice(Long id) {
        super(id);
    }

    protected Invoice(Freelancer freelancer,
                      LocalDate invoiceDate,
                      LocalDate dueDate,
                      List<Service> services,
                      Boolean paid) {
        this(null, freelancer, invoiceDate, dueDate, services, paid);
    }

    protected Invoice(Long id,
                      Freelancer freelancer,
                      LocalDate invoiceDate,
                      LocalDate dueDate,
                      List<Service> services,
                      Boolean paid) {
        super(id);
        this.freelancer = freelancer;
        this.invoiceDate = invoiceDate;
        this.dueDate = dueDate;
        this.services = services;
        this.paid = paid;
    }

    public static class Builder extends Entity.Builder<Invoice, Builder> {
        private Freelancer freelancer;
        private LocalDate invoiceDate;
        private LocalDate dueDate;
        private List<Service> services = new ArrayList<>();
        private Boolean paid = false;

        public Builder freelancer(Freelancer freelancer) {
            this.freelancer = freelancer;
            return self();
        }

        public Builder invoiceDate(LocalDate invoiceDate) {
            this.invoiceDate = invoiceDate;
            return self();
        }

        public Builder dueDate(LocalDate dueDate) {
            this.dueDate = dueDate;
            return self();
        }

        public Builder addService(Service service) {
            this.services.add(service);
            return self();
        }

        public Builder services(List<Service> services) {
            this.services = services;
            return self();
        }

        public Builder paid(Boolean paid) {
            this.paid = paid;
            return self();
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public Invoice build() {
            if (freelancer == null) {
                throw new ObjectValidationException("Freelancer je obavezan");
            }
            if (invoiceDate == null) {
                throw new ObjectValidationException("Datum izdavanja je obavezan");
            }
            if (dueDate == null) {
                throw new ObjectValidationException("Datum dospijeća je obavezan");
            }
            if (dueDate.isBefore(invoiceDate)) {
                throw new ObjectValidationException("Datum dospijeća ne smije biti prije datuma izdavanja");
            }
            if (services == null || services.isEmpty()) {
                throw new ObjectValidationException("Račun mora sadržavati bar jednu stavku usluge");
            }
            return new Invoice(id, freelancer, invoiceDate, dueDate, services, paid);
        }
    }

    public static Invoice ref(Long id) {
        if (id == null) throw new ObjectValidationException("ID je obavezan za referencu računa.");
        return new Invoice(id);
    }

    /**
     * Računa ukupnu vrijednost računa zbrajanjem svih stavki.
     *
     * @return ukupni iznos računa
     */
    public BigDecimal getTotalCost() {
        return services.stream()
                .map(Service::calculateTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Provjerava je li račun dospio za plaćanje.
     *
     * @param today današnji datum
     * @return true ako je današnji datum nakon datuma dospijeća i račun nije plaćen
     */
    public Boolean isOverdue(LocalDate today) {
        return !paid && dueDate.isBefore(today);
    }

    public Freelancer getFreelancer() {
        return freelancer;
    }

    public void setFreelancer(Freelancer freelancer) {
        this.freelancer = freelancer;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }

    public Boolean isPaid() {
        return paid;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }

    /**
     * Dohvaća jedinstveni ID suradniak povezanog s ovom fakturom.
     *
     * @return ID freelancera kao Long vrijednost
     */
    public Long getFreelancerId() {
        return freelancer.getId();
    }



    @Override
    public String toString() {
        return "Invoice{" +
                "freelancer=" + freelancer +
                ", invoiceDate=" + invoiceDate +
                ", dueDate=" + dueDate +
                ", services=" + services +
                ", paid=" + paid +
                '}';
    }
}