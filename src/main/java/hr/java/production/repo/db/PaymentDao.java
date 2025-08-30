package hr.java.production.dao.db;

import hr.java.production.exception.DatabaseConnectionException;
import hr.java.production.exception.DatabaseException;
import hr.java.production.model.Invoice;
import hr.java.production.model.Payment;
import hr.java.production.util.DbUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class PaymentDao extends DbDao<Payment> {
    private static final String SELECT_BY_INVOICE_ID_SQL =
                """
                SELECT
                  id,
                  invoice_id,
                  amount,
                  paid_on,
                  transaction_id
                FROM payment
                WHERE invoice_id = ?
                ORDER BY id
                """;

    private static final String DELETE_BY_INVOICE_ID_SQL = "DELETE FROM payment WHERE invoice_id = ?";


    public PaymentDao() {
        super(Payment.class);
    }

    @Override
    protected void bindInsert(PreparedStatement ps, Payment p) throws SQLException {
        ps.setLong(1, p.getInvoice().getId());
        ps.setBigDecimal(2, p.getAmount());
        ps.setTimestamp(3, Timestamp.valueOf(p.getPaidOn()));
        ps.setString(4, p.getTransactionId());
    }

    @Override
    protected void bindUpdate(PreparedStatement ps, Payment p) throws SQLException {
        bindInsert(ps, p);
        ps.setLong(5, p.getId());
    }

    @Override
    protected Payment mapRow(ResultSet rs) throws SQLException {
        long id         = rs.getLong("id");
        long invoiceId  = rs.getLong("invoice_id");
        var amount      = rs.getBigDecimal("amount");
        LocalDateTime t = rs.getTimestamp("paid_on").toLocalDateTime();
        String txId     = rs.getString("transaction_id");

        Invoice invRef = Invoice.ref(invoiceId);

        return new Payment.Builder()
                .id(id)
                .invoice(invRef)
                .amount(amount)
                .paidOn(t)
                .transactionId(txId)
                .build();
    }

    public List<Payment> findByInvoiceId(long invoiceId) throws DatabaseException {
        try (Connection conn = DbUtil.connectToDatabase()) {
            return findByInvoiceId(conn, invoiceId);
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DatabaseException("Greška pri dohvaćanju uplata za račun ID=" + invoiceId, e);
        }
    }

    public List<Payment> findByInvoiceId(Connection conn, long invoiceId) throws DatabaseException {
        List<Payment> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_INVOICE_ID_SQL)) {
            ps.setLong(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Greška pri dohvaćanju uplata za račun ID=" + invoiceId, e);
        }
    }

    public void deleteByInvoiceId(long invoiceId) throws DatabaseException {
        try (Connection conn = DbUtil.connectToDatabase()) {
            deleteByInvoiceId(conn, invoiceId);
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DatabaseException("Greška pri brisanju uplata za račun ID=" + invoiceId, e);
        }
    }

    public void deleteByInvoiceId(Connection conn, long invoiceId) throws DatabaseException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_BY_INVOICE_ID_SQL)) {
            ps.setLong(1, invoiceId);
            ps.executeUpdate(); // 0+ redaka
        } catch (SQLException e) {
            throw new DatabaseException("Greška pri brisanju uplata za račun ID=" + invoiceId, e);
        }
    }

    @Override
    protected String getInsertSql() {
        return """
                INSERT INTO payment (
                  invoice_id,
                  amount,
                  paid_on,
                  transaction_id
                ) VALUES (?, ?, ?, ?)
                """;
    }

    @Override
    protected String getUpdateSql() {
        return """
                UPDATE payment SET
                  invoice_id     = ?,
                  amount         = ?,
                  paid_on        = ?,
                  transaction_id = ?
                WHERE id = ?
                """;
    }

    @Override
    protected String getDeleteSql() {
        return "DELETE FROM payment WHERE id = ?";
    }

    @Override
    protected String getSelectByIdSql() {
        return """
                SELECT
                  id,
                  invoice_id,
                  amount,
                  paid_on,
                  transaction_id
                FROM payment
                WHERE id = ?
                """;
    }

    @Override
    protected String getSelectAllSql() {
        return """
                SELECT
                  id,
                  invoice_id,
                  amount,
                  paid_on,
                  transaction_id
                FROM payment
                ORDER BY id
                """;
    }

}
