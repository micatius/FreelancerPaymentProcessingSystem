package hr.java.production.repo.db;

import hr.java.production.exception.DatabaseConnectionException;
import hr.java.production.exception.DatabaseException;
import hr.java.production.model.Address;
import hr.java.production.util.DbUtils;

import java.sql.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    /**
     * Vraća mapu adresa gdje su ključevi identifikatori (ID) adresa, na temelju skupa danih ID-ova.
     *
     * @param conn Connection objekt za povezivanje s bazom podataka
     * @param ids Skup ID-ova adresa koje treba dohvatiti
     * @return Mapa koja povezuje ID-ove s pripadajućim Address objektima, ili prazna mapa ako nema rezultata
     * @throws SQLException ako se dogodi pogreška pri komunikaciji s bazom podataka
     */
    public Map<Long, Address> findByIds(Connection conn, Set<Long> ids) throws SQLException {
        if (ids == null || ids.isEmpty()) return Collections.emptyMap();

        String placeholders = ids.stream().map(x -> "?").collect(Collectors.joining(","));
        String sql = "SELECT id, street, house_number, city, postal_code FROM address " +
                "WHERE id IN (" + placeholders + ")";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            for (Long id : ids) ps.setLong(i++, id);
            try (ResultSet rs = ps.executeQuery()) {
                Map<Long, Address> map = new HashMap<>();
                while (rs.next()) {
                    Address a = mapRow(rs);
                    map.put(a.getId(), a);
                }
                return map;
            }
        }
    }

    public Map<Long, Address> findByIds(Set<Long> ids) throws DatabaseException {
        if (ids == null || ids.isEmpty()) return Collections.emptyMap();
        try (Connection c = DbUtils.connectToDatabase()) {
            return findByIds(c, ids);
        }
        catch (SQLException | DatabaseConnectionException e) {
            throw new DatabaseException("Dogodila se greška pri dohvaćanju adresa po ID-evima", e);
        }
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