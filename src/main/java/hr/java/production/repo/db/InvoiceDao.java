package hr.java.production.repo.db;

import hr.java.production.model.Freelancer;
import hr.java.production.model.Invoice;

import java.sql.*;
import java.time.LocalDate;

/**
 * Klasa InvoiceDao omogućuje pristup i upravljanje podacima o računima u bazi
 * podataka. Proširuje osnovne funkcionalnosti klase DbDao i uključuje
 * implementacije za umetanje, ažuriranje, brisanje i dohvaćanje računa
 */
public final class InvoiceDao extends DbDao<Invoice> {

    public InvoiceDao() {
        super(Invoice.class);
    }

    @Override
    protected void bindInsert(PreparedStatement ps, Invoice inv) throws SQLException {
        ps.setLong(1, inv.getFreelancerId());
        ps.setDate(2, Date.valueOf(inv.getInvoiceDate()));
        ps.setDate(3, Date.valueOf(inv.getDueDate()));
    }

    @Override
    protected void bindUpdate(PreparedStatement ps, Invoice inv) throws SQLException {
        bindInsert(ps, inv);
        ps.setLong(4, inv.getId());
    }

    @Override
    protected Invoice mapRow(ResultSet rs) throws SQLException {
        long id           = rs.getLong("id");
        long freelancerId = rs.getLong("freelancer_id");
        LocalDate invDate = rs.getDate("invoice_date").toLocalDate();
        LocalDate dueDate = rs.getDate("due_date").toLocalDate();

        Freelancer freelancerRef = Freelancer.ref(freelancerId);

        return new Invoice.Builder()
                .id(id)
                .freelancer(freelancerRef)
                .invoiceDate(invDate)
                .dueDate(dueDate)
                .build();
    }

    @Override
    protected String getInsertSql() {
        return """
            INSERT INTO invoice (
              freelancer_id,
              invoice_date,
              due_date
            ) VALUES (?, ?, ?)
            """;
    }

    @Override
    protected String getUpdateSql() {
        return """
            UPDATE invoice SET
              freelancer_id = ?,
              invoice_date  = ?,
              due_date      = ?
            WHERE id = ?
            """;
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
              due_date
            FROM invoice
            WHERE id = ?
            """;
    }

    @Override
    protected String getSelectAllSql() {
        return """
            SELECT
              id,
              freelancer_id,
              invoice_date,
              due_date
            FROM invoice
            ORDER BY id
            """;
    }
}
