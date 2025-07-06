package hr.java.production.dao.db;

import hr.java.production.exception.DatabaseAccessException;
import hr.java.production.exception.DatabaseConnectionException;
import hr.java.production.model.Entity;
import hr.java.production.util.DbUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Apstraktna klasa koja implementira osnovne funkcionalnosti za rad s entitetima
 * u bazi podataka te omogućava mapiranje redaka baze u objekte entiteta.
 * Također podržava zapisivanje promjena kroz implementaciju sučelja ChangeLogger.
 *
 * @param <T> generički tip koji predstavlja tip entiteta kojim klasa upravlja
 */
public abstract class DbDao<T extends Entity> {

    private final Class<T> type;

    protected DbDao(Class<T> type) {
        this.type = type;
    }

    /**
     * Vraća SQL izraz za umetanje entiteta u bazu podataka.
     *
     * @return SQL izraz za INSERT operaciju
     */
    protected abstract String getInsertSql();

    /**
     * Veže parametre entiteta na dani PreparedStatement za SQL operaciju umetanja.
     *
     * @param ps      PreparedStatement na koji se vežu parametri
     * @param entity  entitet čiji se podaci koriste za umetanje
     * @throws SQLException ako dođe do greške prilikom rada s PreparedStatement objektom
     */
    protected abstract void bindInsert(PreparedStatement ps, T entity) throws SQLException;

    /**
     * Vraća SQL izraz za ažuriranje entiteta u bazi podataka.
     *
     * @return SQL izraz za UPDATE operaciju
     */
    protected abstract String getUpdateSql();

    /**
     * Veže parametre entiteta na PreparedStatement za SQL operaciju ažuriranja.
     *
     * @param ps PreparedStatement na koji se vežu parametri
     * @param entity entitet čiji se podaci koriste za ažuriranje
     * @throws SQLException ako dođe do greške prilikom rada s PreparedStatement objektom
     */
    protected abstract void bindUpdate(PreparedStatement ps, T entity) throws SQLException;

    /**
     * Vraća SQL izraz za brisanje entiteta iz baze podataka.
     *
     * @return SQL izraz za DELETE operaciju
     */
    protected abstract String getDeleteSql();

    /**
     * Mapira trenutni redak iz ResultSet objekta u instancu tipa T.
     *
     * @param rs ResultSet koji se koristi za dohvaćanje podataka trenutnog retka
     * @return instanca tipa T mapirana iz trenutnog retka
     * @throws SQLException ako dođe do greške pri pristupu ResultSet objektu
     */
    protected abstract T mapRow(ResultSet rs) throws SQLException;

    /**
     * Vraća SQL izraz za dohvaćanje entiteta prema ID-u.
     *
     * @return SQL izraz za SELECT operaciju prema ID-u
     */
    protected abstract String getSelectByIdSql();

    /**
     * Vraća SQL izraz za dohvaćanje svih zapisa iz baze podataka.
     *
     * @return SQL izraz za SELECT svih zapisa
     */
    protected abstract String getSelectAllSql();

    /**
     * Poziva se nakon spremanja entiteta u bazu podataka. Primarno se koristi
     * za dodatne akcije ili bilježenje promjena nakon završetka operacije spremanja.
     *
     * @param newVal novi entitet koji je spremljen u bazu podataka
     */
    protected void afterSave(T newVal)      { }

    /**
     * Poziva se nakon ažuriranja entiteta u bazi podataka. Omogućuje dodatne akcije
     * nakon završetka operacije ažuriranja.
     *
     * @param oldVal prethodno stanje entiteta prije ažuriranja
     * @param newVal novo ažurirano stanje entiteta
     */
    protected void afterUpdate(T oldVal, T newVal) { }

    /**
     * Poziva se nakon brisanja entiteta iz baze podataka. Koristi se za dodatne
     * akcije koje je potrebno izvršiti nakon brisanja.
     *
     * @param oldVal entitet koji je izbrisan iz baze podataka
     */
    protected void afterDelete(T oldVal)    { }

    /**
     * Sprema entitet u bazu podataka i postavlja generirani ID na entitet.
     *
     * @param entity entitet koji se sprema u bazu podataka
     * @throws DatabaseAccessException ako dođe do greške prilikom pristupa bazi podataka
     */
    public void save(T entity) throws DatabaseAccessException {
        try (Connection conn = DbUtil.connectToDatabase();
             PreparedStatement ps = conn.prepareStatement(getInsertSql(), Statement.RETURN_GENERATED_KEYS)) {
            bindInsert(ps, entity);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setId(keys.getLong(1));
                }
            }
            afterSave(entity);

        } catch (SQLException | DatabaseConnectionException e) {
            throw new DatabaseAccessException(
                    "Greška pri spremanju " + type.getSimpleName(), e);
        }
    }

    public void update(T entity) throws DatabaseAccessException {
        T old = findById(entity.getId())
                .orElseThrow(() -> new DatabaseAccessException(
                        type.getSimpleName() + " s ID=" + entity.getId() + " ne postoji"));

        try (Connection conn = DbUtil.connectToDatabase();
             PreparedStatement ps = conn.prepareStatement(getUpdateSql())) {
            bindUpdate(ps, entity);
            ps.executeUpdate();
            afterUpdate(old, entity);

        } catch (SQLException | DatabaseConnectionException e) {
            throw new DatabaseAccessException(
                    "Greška pri ažuriranju " + type.getSimpleName(), e);
        }
    }

    public void delete(Long id) throws DatabaseAccessException {
        T old = findById(id)
                .orElseThrow(() -> new DatabaseAccessException(
                        type.getSimpleName() + " s ID=" + id + " ne postoji"));

        try (Connection conn = DbUtil.connectToDatabase();
             PreparedStatement ps = conn.prepareStatement(getDeleteSql())) {
            ps.setLong(1, id);
            ps.executeUpdate();
            afterDelete(old);
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DatabaseAccessException(
                    "Greška pri brisanju " + type.getSimpleName() + " s ID=" + id, e);
        }
    }

    /**
     * Dohvaća opcionalni entitet iz baze podataka prema zadanom ID-u.
     *
     * @param id ID entiteta koji se traži
     * @return Optional s entitetom ako je pronađen, inače prazan Optional
     * @throws DatabaseAccessException ako dođe do greške pri pristupu bazi
     */
    public Optional<T> findById(Long id) throws DatabaseAccessException {
        try (Connection conn = DbUtil.connectToDatabase();
             PreparedStatement ps = conn.prepareStatement(getSelectByIdSql())) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DatabaseAccessException("Greška pri dohvaćanju objekta " +
                    type.getSimpleName() + " s ID=" + id, e);
        }
    }

    /**
     * Dohvaća listu svih objekata generičkog tipa T iz baze podataka.
     *
     * @return lista svih objekata generičkog tipa T
     * @throws DatabaseAccessException ako dođe do greške pri pristupu bazi podataka
     */
    public List<T> findAll() throws DatabaseAccessException {
        List<T> results = new ArrayList<>();
        try ( Connection conn = DbUtil.connectToDatabase();
              PreparedStatement ps = conn.prepareStatement(getSelectAllSql());
              ResultSet rs = ps.executeQuery() )
        {
            while (rs.next()) {
                results.add(mapRow(rs));
            }
            return results;

        } catch (SQLException | DatabaseConnectionException e) {
            throw new DatabaseAccessException(
                    "Greška pri dohvaćanju svih objekata " + type.getSimpleName(), e
            );
        }
    }
}
