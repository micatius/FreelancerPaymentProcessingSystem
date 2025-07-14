package hr.java.production.dao.db;

import hr.java.production.exception.DatabaseAccessException;
import hr.java.production.exception.DatabaseConnectionException;
import hr.java.production.model.Freelancer;
import hr.java.production.model.Invoice;
import hr.java.production.model.Service;
import hr.java.production.util.DbUtil;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Klasa InvoiceDao omogućuje pristup i upravljanje podacima o računima u bazi
 * podataka. Proširuje osnovne funkcionalnosti klase DbDao i uključuje
 * implementacije za umetanje, ažuriranje, brisanje i dohvaćanje računa, uz
 * podršku za rad s pripadajućim uslugama (services) povezanima s računima.
 */
public final class InvoiceDao extends DbDao<Invoice> implements ChangeLogger {

    private final ServiceDao serviceDao = new ServiceDao();
    private final FreelancerDao freelancerDao = new FreelancerDao();

    public InvoiceDao() {
        super(Invoice.class);
    }

    @Override
    protected String getInsertSql() {
        return """
            INSERT INTO invoice (
              freelancer_id,
              invoice_date,
              due_date,
              paid
            ) VALUES (?, ?, ?, ?)
            """;
    }

    @Override
    protected void bindInsert(PreparedStatement ps, Invoice inv) throws SQLException {
        ps.setLong(1, inv.getFreelancerId());
        ps.setDate(2, Date.valueOf(inv.getInvoiceDate()));
        ps.setDate(3, Date.valueOf(inv.getDueDate()));
        ps.setBoolean(4, inv.isPaid());
    }

    @Override
    protected String getUpdateSql() {
        return """
            UPDATE invoice SET
              freelancer_id = ?,
              invoice_date  = ?,
              due_date      = ?,
              paid          = ?
            WHERE id = ?
            """;
    }

    @Override
    protected void bindUpdate(PreparedStatement ps, Invoice inv) throws SQLException {
        bindInsert(ps, inv);
        ps.setLong(5, inv.getId());
    }

    @Override
    protected String getDeleteSql() {
        return "DELETE FROM invoice WHERE id = ?";
    }

    @Override
    protected String getSelectByIdSql() {
        return """
            SELECT
              id,
              freelancer_id,
              invoice_date,
              due_date,
              paid
            FROM invoice
            WHERE id = ?
            """;
    }

    @Override
    protected String getSelectAllSql() {
        return getSelectByIdSql().replace("WHERE id = ?", "ORDER BY id");
    }

    @Override
    protected Invoice mapRow(ResultSet rs) throws SQLException, DatabaseAccessException {
        long id           = rs.getLong("id");
        long freelancerId = rs.getLong("freelancer_id");
        LocalDate invDate = rs.getDate("invoice_date").toLocalDate();
        LocalDate dueDate = rs.getDate("due_date").toLocalDate();
        boolean paid      = rs.getBoolean("paid");

        Freelancer invoiceFreelancer = freelancerDao.findById(freelancerId).
                orElseThrow(() -> new DatabaseAccessException("Greška u dohvaćanju suradnika za fakturu s ID=" + id));
        List<Service> invoiceServices = serviceDao.findByInvoiceId(id);

        return new Invoice.Builder()
                .id(id)
                .freelancer(invoiceFreelancer)
                .services(invoiceServices)
                .invoiceDate(invDate)
                .dueDate(dueDate)
                .paid(paid)
                .build();
    }


    @Override
    public void save(Invoice inv) throws DatabaseAccessException {
        try (Connection conn = DbUtil.connectToDatabase()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(getInsertSql(), Statement.RETURN_GENERATED_KEYS)) {
                bindInsert(ps, inv);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        inv.setId(keys.getLong(1));
                    }
                }
            }

            for (Service s : inv.getServices()) {
                s.setInvoiceId(inv.getId());
                serviceDao.save(s);
            }

            conn.commit();
            logChange(type, null, inv);

        } catch (SQLException | DatabaseConnectionException e) {
            throw new DatabaseAccessException("Greška pri spremanju računa", e);
        }
    }


    @Override
    public void update(Invoice inv) throws DatabaseAccessException {
        Invoice old = findById(inv.getId())
                .orElseThrow(() -> new DatabaseAccessException("Račun s ID=" + inv.getId() + " ne postoji"));

        try (Connection conn = DbUtil.connectToDatabase();
             PreparedStatement del = conn.prepareStatement(
                     "DELETE FROM service WHERE invoice_id = ?")) {
            conn.setAutoCommit(false);
            del.setLong(1, inv.getId());
            del.executeUpdate();


            try (PreparedStatement ps = conn.prepareStatement(getUpdateSql())) {
                bindUpdate(ps, inv);
                ps.executeUpdate();
            }

            for (Service s : inv.getServices()) {
                s.setInvoiceId(inv.getId());
                serviceDao.save(s);
            }

            conn.commit();
            logChange(type, old, inv);

        } catch (SQLException | DatabaseConnectionException e) {
            throw new DatabaseAccessException("Greška pri ažuriranju računa", e);
        }
    }

    @Override
    public void delete(Long id) throws DatabaseAccessException {
        Invoice old = findById(id)
                .orElseThrow(() -> new DatabaseAccessException("Račun s ID=" + id + " ne postoji"));

        try (Connection conn = DbUtil.connectToDatabase()) {
            conn.setAutoCommit(false);

            try (PreparedStatement del = conn.prepareStatement(
                    "DELETE FROM service WHERE invoice_id = ?")) {
                del.setLong(1, id);
                del.executeUpdate();
            }

            super.delete(id);

            conn.commit();
            logChange(type, old, null);
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DatabaseAccessException("Greška pri brisanju računa", e);
        }
    }
}
