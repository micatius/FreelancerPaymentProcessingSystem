package hr.java.production.dao.db;

import hr.java.production.exception.DatabaseAccessException;
import hr.java.production.exception.DatabaseConnectionException;
import hr.java.production.model.Service;
import hr.java.production.util.DbUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Klasa ServiceDao upravlja operacijama pristupa podatcima u bazi koje su
 * povezane s entitetom Service. Pruža implementacije za umetanje, ažuriranje,
 * dohvaćanje i brisanje podataka te metode specifične za rad s entitetom Service.
 */
public final class ServiceDao extends DbDao<Service> {

    public ServiceDao() {
        super(Service.class);
    }

    @Override
    protected String getInsertSql() {
        return """
                INSERT INTO service (
                  invoice_id,
                  service_name,
                  unit_fee,
                  quantity
                ) VALUES (?, ?, ?, ?)
                """;
    }

    @Override
    protected void bindInsert(PreparedStatement ps, Service s) throws SQLException {
        ps.setLong(1, s.getInvoiceId());
        ps.setString(2, s.getName());
        ps.setBigDecimal(3, s.getUnitFee());
        ps.setInt(4, s.getQuantity());
    }

    @Override
    protected String getUpdateSql() {
        return """
                UPDATE service SET
                  invoice_id   = ?,
                  service_name = ?,
                  unit_fee     = ?,
                  quantity     = ?
                WHERE id = ?
                """;
    }

    @Override
    protected void bindUpdate(PreparedStatement ps, Service s) throws SQLException {
        bindInsert(ps, s);
        ps.setLong(5, s.getId());
    }

    @Override
    protected String getDeleteSql() {
        return "DELETE FROM service WHERE id = ?";
    }

    @Override
    protected String getSelectByIdSql() {
        return """
                SELECT
                  id,
                  invoice_id,
                  service_name,
                  unit_fee,
                  quantity
                FROM service
                WHERE id = ?
                """;
    }

    @Override
    protected String getSelectAllSql() {
        // same columns as selectById, but no WHERE
        return getSelectByIdSql()
                .replace("WHERE id = ?", "ORDER BY id");
    }

    @Override
    protected Service mapRow(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        long invoiceId = rs.getLong("invoice_id");
        String name = rs.getString("service_name");
        BigDecimal fee = rs.getBigDecimal("unit_fee");
        int quantity = rs.getInt("quantity");

        // Build the Service instance
        Service s = new Service.Builder()
                .id(id)
                .serviceName(name)
                .unitFee(fee)
                .quantity(quantity)
                .build();

        // Associate it with its invoice
        s.setInvoiceId(invoiceId);
        return s;
    }

    /**
     * Dohvaća listu svih usluga vezanih za određenu fakturu na temelju ID-a računa.
     *
     * @param invoiceId ID fakture na temelju kojeg se dohvaćaju usluge
     * @return lista usluga povezanih s danim ID-em fakture
     * @throws DatabaseAccessException ako dođe do pogreške prilikom pristupa bazi podataka
     */
    public List<Service> findByInvoiceId(long invoiceId) throws DatabaseAccessException {
        List<Service> list = new ArrayList<>();
        try (Connection conn = DbUtil.connectToDatabase();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id, invoice_id, service_name, unit_fee, quantity " +
                             "FROM service WHERE invoice_id = ? ORDER BY id"
             )
        ) {
            ps.setLong(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DatabaseAccessException(
                    "Greška pri dohvaćanju service stavki za račun ID=" + invoiceId, e
            );
        }
    }
}
