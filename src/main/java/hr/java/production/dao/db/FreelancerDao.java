package hr.java.production.dao.db;

import hr.java.production.exception.DatabaseAccessException;
import hr.java.production.model.Address;
import hr.java.production.model.Freelancer;

import java.sql.*;

/**
 * Klasa FreelancerDao pruža metode za pristup i manipulaciju podacima o
 * freelancerima u bazi podataka. Nasljeđuje osnovne funkcionalnosti od klase
 * DbDao te implementira specifične SQL upite i mapiranje rezultata za entitet
 * Freelancer.
 */
public final class FreelancerDao extends DbDao<Freelancer> implements ChangeLogger {

    public FreelancerDao() {
        super(Freelancer.class);
    }

    @Override
    protected String getInsertSql() {
        return """
                INSERT INTO freelancer(
                  first_name,
                  last_name,
                  email,
                  phone_number,
                  address_id,
                  business_name,
                  business_id_no,
                  bank_account,
                  active
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
    }

    @Override
    protected void bindInsert(PreparedStatement ps, Freelancer f) throws SQLException {
        ps.setString(1, f.getFirstName());
        ps.setString(2, f.getLastName());
        ps.setString(3, f.getEmail());
        ps.setString(4, f.getPhoneNumber());
        ps.setLong(5, f.getAddress().getId());
        ps.setString(6, f.getBusinessName());
        ps.setString(7, f.getBusinessIdentificationNumber());
        ps.setString(8, f.getBankAccountNumber());
        ps.setBoolean(9, f.getActive());
    }

    @Override
    protected String getUpdateSql() {
        return """
                UPDATE freelancer SET
                  first_name   = ?,
                  last_name    = ?,
                  email        = ?,
                  phone_number = ?,
                  address_id   = ?,
                  business_name             = ?,
                  business_id_no            = ?,
                  bank_account              = ?,
                  active                    = ?
                WHERE id = ?
                """;
    }

    @Override
    protected void bindUpdate(PreparedStatement ps, Freelancer f) throws SQLException {
        bindInsert(ps, f);
        ps.setLong(10, f.getId());
    }

    @Override
    protected String getDeleteSql() {
        return "DELETE FROM freelancer WHERE id = ?";
    }

    @Override
    protected String getSelectByIdSql() {
        return """
                SELECT
                  id,
                  first_name,
                  last_name,
                  email,
                  phone_number,
                  address_id,
                  business_name,
                  business_id_no AS businessIdentificationNumber,
                  bank_account   AS bankAccountNumber,
                  active
                FROM freelancer
                WHERE id = ?
                """;
    }

    @Override
    protected String getSelectAllSql() {
        return getSelectByIdSql().replace("WHERE id = ?", "ORDER BY id");
    }

    @Override
    protected Freelancer mapRow(ResultSet rs) throws SQLException {
        long addrId = rs.getLong("address_id");
        Address address;
        try {
            address = new AddressDao()
                    .findById(addrId)
                    .orElseThrow(() -> new SQLException(
                            "Adresa s ID=" + addrId + " ne postoji"
                    ));
        } catch (DatabaseAccessException e) {
            throw new SQLException("Greška pri dohvaćanju adrese s ID=" + addrId, e);
        }

        return new Freelancer.Builder()
                .id(rs.getLong("id"))
                .firstName(rs.getString("first_name"))
                .lastName(rs.getString("last_name"))
                .email(rs.getString("email"))
                .phoneNumber(rs.getString("phone_number"))
                .address(address)
                .businessName(rs.getString("business_name"))
                .businessIdentificationNumber(
                        rs.getString("businessIdentificationNumber"))
                .bankAccountNumber(rs.getString("bankAccountNumber"))
                .active(rs.getBoolean("active"))
                .build();
    }
}
