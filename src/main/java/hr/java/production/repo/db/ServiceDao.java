package hr.java.production.repo.db;

import hr.java.production.exception.DatabaseConnectionException;
import hr.java.production.exception.DatabaseException;
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
    private static final String SELECT_BY_INVOICE_ID_SQL =
                """
                SELECT
                  id,
                  invoice_id,
                  service_name,
                  unit_fee,
                  quantity
                FROM service
                WHERE invoice_id = ?
                ORDER BY id
                """;
    private static final String DELETE_BY_INVOICE_ID_SQL = "DELETE FROM service WHERE invoice_id = ?";

    public ServiceDao() {
        super(Service.class);
    }


    @Override
    protected void bindInsert(PreparedStatement ps, Service s) throws SQLException {
        ps.setLong(1, s.getInvoiceId());         // NOT NULL
        ps.setString(2, s.getName());            // aka service_name
        ps.setBigDecimal(3, s.getUnitFee());
        ps.setInt(4, s.getQuantity());
    }

    @Override
    protected void bindUpdate(PreparedStatement ps, Service s) throws SQLException {
        bindInsert(ps, s);            // 1..4
        ps.setLong(5, s.getId());     // WHERE id=?
    }

    @Override
    protected Service mapRow(ResultSet rs) throws SQLException {
        long id        = rs.getLong("id");
        long invoiceId = rs.getLong("invoice_id");
        String name    = rs.getString("service_name");
        BigDecimal fee = rs.getBigDecimal("unit_fee");
        int quantity   = rs.getInt("quantity");

        Service s = new Service.Builder()
                .id(id)
                .serviceName(name)
                .unitFee(fee)
                .quantity(quantity)
                .build();
        s.setInvoiceId(invoiceId);
        return s;
    }

    /**
     * Dohvaća popis usluga vezanih za određenu fakturu na temelju ID-a fakture.
     *
     * @param invoiceId ID fakture za koji se dohvaćaju usluge
     * @return lista objekata klase Service vezanih uz određeni ID fakture
     * @throws DatabaseException u slučaju pogreške pri dohvaćanju podataka iz baze
     */
    public List<Service> findByInvoiceId(long invoiceId) throws DatabaseException {
        try (Connection conn = DbUtil.connectToDatabase()) {
            return findByInvoiceId(conn, invoiceId);
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DatabaseException("Greška pri dohvaćanju service stavki za račun ID=" + invoiceId, e);
        }
    }

    /**
     * Dohvaća popis usluga povezanih s određenom fakturom pomoću ID-a fakture.
     *
     * @param conn konekcija na bazu podataka
     * @param invoiceId ID fakture za koju se dohvaćaju usluge
     * @return lista objekata klase Service vezanih za navedeni ID fakture
     * @throws DatabaseException u slučaju greške pri dohvaćanju podataka iz baze
     */
    public List<Service> findByInvoiceId(Connection conn, long invoiceId) throws DatabaseException {
        List<Service> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_INVOICE_ID_SQL)) {
            ps.setLong(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Greška pri dohvaćanju service stavki za račun ID=" + invoiceId, e);
        }
    }

    /**
     * Briše sve stavke usluga povezane s određenom fakturom na temelju ID-a fakture.
     *
     * @param invoiceId ID fakture čije stavke usluga treba obrisati
     * @throws DatabaseException u slučaju greške prilikom pristupa bazi podataka
     */
    public void deleteByInvoiceId(long invoiceId) throws DatabaseException {
        try (Connection conn = DbUtil.connectToDatabase()) {
            deleteByInvoiceId(conn, invoiceId);
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DatabaseException("Greška pri brisanju service stavki za račun ID=" + invoiceId, e);
        }
    }

    /**
     * Briše sve stavke usluga povezane s određenom fakturom na temelju ID-a fakture.
     *
     * @param conn konekcija na bazu podataka
     * @param invoiceId ID fakture čije stavke usluga treba obrisati
     * @throws DatabaseException u slučaju greške prilikom pristupa bazi podataka
     */
    public void deleteByInvoiceId(Connection conn, long invoiceId) throws DatabaseException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_BY_INVOICE_ID_SQL)) {
            ps.setLong(1, invoiceId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Greška pri brisanju service stavki za račun ID=" + invoiceId, e);
        }
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
        return """
                SELECT
                  id,
                  invoice_id,
                  service_name,
                  unit_fee,
                  quantity
                FROM service
                ORDER BY id
                """;
    }

}
