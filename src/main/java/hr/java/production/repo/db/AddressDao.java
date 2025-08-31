package hr.java.production.repo.db;

import hr.java.production.model.Address;

import java.sql.*;

/**
 * Klasa AddressDao zadužena je za upravljanje operacijama pristupa podacima
 * za entitet Address u bazi podataka. Implementira metode za umetanje, ažuriranje,
 * brisanje i dohvaćanje podataka, uz mapiranje redaka iz rezultata upita na objekte klase Address.
 */
public final class AddressDao extends DbDao<Address> {

    public AddressDao() {
        super(Address.class);
    }

    @Override
    protected void bindInsert(PreparedStatement ps, Address entity) throws SQLException {
        ps.setString(1, entity.getStreet());
        ps.setString(2, entity.getHouseNumber());
        ps.setString(3, entity.getCity());
        ps.setString(4, entity.getPostalCode());
    }


    @Override
    protected void bindUpdate(PreparedStatement ps, Address entity) throws SQLException {
        bindInsert(ps, entity);
        ps.setLong(5, entity.getId());
    }


    @Override
    protected Address mapRow(ResultSet rs) throws SQLException {
        return new Address.Builder()
                .id(rs.getLong("id"))
                .street(rs.getString("street"))
                .houseNumber(rs.getString("house_number"))
                .city(rs.getString("city"))
                .postalCode(rs.getString("postal_code"))
                .build();
    }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO address(street, house_number, city, postal_code) VALUES (?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSql() {
        return """
                UPDATE address SET
                  street        = ?,
                  house_number  = ?,
                  city          = ?,
                  postal_code   = ?
                WHERE id = ?
                """;
    }

    @Override
    protected String getSelectByIdSql() {
        return """
                SELECT
                  id,
                  street,
                  house_number,
                  city,
                  postal_code
                FROM address
                WHERE id = ?
                """;
    }

    @Override
    protected String getDeleteSql() {
        return "DELETE FROM address WHERE id = ?";
    }

    @Override
    protected String getSelectAllSql() {
        return """
        SELECT
          id,
          street,
          house_number,
          city,
          postal_code
        FROM address
        ORDER BY id
        """;
    }
}