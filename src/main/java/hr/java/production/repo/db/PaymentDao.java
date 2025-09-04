package hr.java.production.repo.db;

import hr.java.production.exception.DatabaseConnectionException;
import hr.java.production.exception.DatabaseException;
import hr.java.production.model.Invoice;
import hr.java.production.model.Payment;
import hr.java.production.util.DbUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
        Long id         = rs.getLong("id");
        Long invoiceId  = rs.getLong("invoice_id");
        BigDecimal amount      = rs.getBigDecimal("amount");
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

    public Optional<Payment> findByInvoiceId(Connection conn, long invoiceId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_INVOICE_ID_SQL)) {
            ps.setLong(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                Payment first = mapRow(rs);
                if (rs.next()) {
                    throw new SQLException("Više uplata pronađeno za invoice_id=" + invoiceId);
                }
                return Optional.of(first);
            }
        }
    }

    public Optional<Payment> findByInvoiceId(long invoiceId) throws DatabaseException {
        try (Connection conn = DbUtil.connectToDatabase()) {
            return findByInvoiceId(conn, invoiceId);
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DatabaseException("Greška pri dohvaćanju uplate za račun ID=" + invoiceId, e);
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
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Greška pri brisanju uplata za račun ID=" + invoiceId, e);
        }
    }

    /**
     * Dohvaća mapu u kojoj su ključevi ID-evi faktura, a vrijednosti odgovarajući objekti Payment,
     * na temelju proslijeđenih ID-eva faktura.
     *
     * @param conn       otvoreni {@link Connection} prema bazi podataka
     * @param invoiceIds skup ID-eva faktura za pretragu
     * @return mapa koja mapira ID faktura na odgovarajući objekt {@link Payment};
     *         prazna mapa ako nisu pronađene odgovarajuće uplate
     * @throws SQLException ako dođe do greške prilikom pristupa bazi podataka
     */
    public Map<Long, Payment> findByInvoiceIds(Connection conn, Set<Long> invoiceIds) throws SQLException {
        if (invoiceIds == null || invoiceIds.isEmpty()) return Collections.emptyMap();

        String placeholders = invoiceIds.stream().map(x -> "?").collect(Collectors.joining(","));
        String sql = "SELECT id, invoice_id, amount, paid_on, transaction_id " +
                "FROM payment WHERE invoice_id IN (" + placeholders + ")";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            for (Long id : invoiceIds) ps.setLong(i++, id);

            try (ResultSet rs = ps.executeQuery()) {
                Map<Long, Payment> map = new HashMap<>();
                while (rs.next()) {
                    Payment p = mapRow(rs);
                    Long invId = p.getInvoice().getId();
                    if (map.putIfAbsent(invId, p) != null) {
                        throw new SQLException("Više uplata pronađeno za invoice_id=" + invId);
                    }
                }
                return map;
            }
        }
    }

    public Map<Long, Payment> findByInvoiceIds(Set<Long> invoiceIds) throws DatabaseException {
        if (invoiceIds == null || invoiceIds.isEmpty()) return Collections.emptyMap();
        try (Connection c = DbUtil.connectToDatabase()) {
            return findByInvoiceIds(c, invoiceIds);
        }
        catch (SQLException | DatabaseConnectionException e) {
            throw new DatabaseException("Greška u dohvaćanju uplata po ID-evima faktura", e);
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
