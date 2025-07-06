package hr.java.production.dao.db;
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
    protected String getInsertSql() {
        return """
            INSERT INTO address(
              street,
              house_number,
              city,
              postal_code
            ) VALUES (?, ?, ?, ?)
            """;
    }

    @Override
    protected void bindInsert(PreparedStatement ps, Address entity) throws SQLException {
        ps.setString(1, entity.getStreet());
        ps.setString(2, entity.getHouseNumber());
        ps.setString(3, entity.getCity());
        ps.setString(4, entity.getPostalCode());
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
    protected void bindUpdate(PreparedStatement ps, Address entity) throws SQLException {
        bindInsert(ps, entity);
        ps.setLong(5, entity.getId());
    }

    @Override
    protected String getDeleteSql() {
        return "DELETE FROM address WHERE id = ?";
    }

    @Override
    protected Address mapRow(ResultSet rs) throws SQLException {
        // use the Builder to reconstruct the Address
        return new Address.Builder()
                .id(          rs.getLong("id"))
                .street(      rs.getString("street"))
                .houseNumber( rs.getString("house_number"))
                .city(        rs.getString("city"))
                .postalCode(  rs.getString("postal_code"))
                .build();
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
    protected String getSelectAllSql() {
        return getSelectByIdSql().replace("WHERE id = ?", "ORDER BY id");
    }
}