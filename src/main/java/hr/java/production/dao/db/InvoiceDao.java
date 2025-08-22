package hr.java.production.dao.db;

import hr.java.production.exception.DatabaseConnectionException;
import hr.java.production.exception.DatabaseException;
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
public final class InvoiceDao extends DbDao<Invoice> {

    public InvoiceDao() {
        super(Invoice.class);
    }

    @Override
    protected void bindInsert(PreparedStatement ps, Invoice inv) throws SQLException {
        ps.setLong(1, inv.getFreelancerId());                 // NOT NULL
        ps.setDate(2, Date.valueOf(inv.getInvoiceDate()));    // NOT NULL
        ps.setDate(3, Date.valueOf(inv.getDueDate()));        // NOT NULL
        ps.setBoolean(4, inv.isPaid());                       // NOT NULL
    }

    @Override
    protected void bindUpdate(PreparedStatement ps, Invoice inv) throws SQLException {
        bindInsert(ps, inv);          // 1..4
        ps.setLong(5, inv.getId());   // WHERE id=?
    }

    @Override
    protected Invoice mapRow(ResultSet rs) throws SQLException {
        long id           = rs.getLong("id");
        long freelancerId = rs.getLong("freelancer_id");
        LocalDate invDate = rs.getDate("invoice_date").toLocalDate();
        LocalDate dueDate = rs.getDate("due_date").toLocalDate();
        boolean paid      = rs.getBoolean("paid");

        Freelancer freelancerRef = new Freelancer.Builder()
                .id(freelancerId)
                .build();

        return new Invoice.Builder()
                .id(id)
                .freelancer(freelancerRef)
                .invoiceDate(invDate)
                .dueDate(dueDate)
                .paid(paid)
                .build();
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
        return """
            SELECT
              id,
              freelancer_id,
              invoice_date,
              due_date,
              paid
            FROM invoice
            ORDER BY id
            """;
    }
}
