package hr.java.production.dao.db;

import hr.java.production.exception.DatabaseAccessException;
import hr.java.production.exception.DatabaseConnectionException;
import hr.java.production.exception.DatabaseException;
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

    protected final Class<T> type;

    protected DbDao(Class<T> type) {
        this.type = type;
    }

    /**
     * Veže parametre entiteta na dani PreparedStatement za SQL operaciju umetanja.
     *
     * @param ps      PreparedStatement na koji se vežu parametri
     * @param entity  entitet čiji se podaci koriste za umetanje
     * @throws SQLException ako dođe do greške prilikom rada s PreparedStatement objektom
     */
    protected abstract void bindInsert(PreparedStatement ps, T entity) throws SQLException;

    /**
     * Veže parametre entiteta na PreparedStatement za SQL operaciju ažuriranja.
     *
     * @param ps PreparedStatement na koji se vežu parametri
     * @param entity entitet čiji se podaci koriste za ažuriranje
     * @throws SQLException ako dođe do greške prilikom rada s PreparedStatement objektom
     */
    protected abstract void bindUpdate(PreparedStatement ps, T entity) throws SQLException;

    /**
     * Mapira trenutni redak iz ResultSet objekta u instancu tipa T.
     *
     * @param rs ResultSet koji se koristi za dohvaćanje podataka trenutnog retka
     * @return instanca tipa T mapirana iz trenutnog retka
     * @throws SQLException ako dođe do greške pri pristupu ResultSet objektu
     */
    protected abstract T mapRow(ResultSet rs) throws SQLException;

    /**
     * Sprema entitet u bazu podataka i postavlja generirani ID na entitet.
     *
     * @param entity entitet koji se sprema u bazu podataka
     * @throws DatabaseAccessException ako dođe do greške prilikom pristupa bazi podataka
     */
    public final void save(T entity) throws DatabaseException {
        try (Connection conn = DbUtil.connectToDatabase()) {
            save(conn, entity);
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DatabaseException("Greška pri spremanju " + type.getSimpleName(), e);
        }
    }

    /**
     * Sprema entitet u bazu podataka koristeći predanu vezu.
     *
     * @param conn   veza na bazu podataka
     * @param entity entitet koji se sprema
     * @throws DatabaseAccessException ako dođe do greške prilikom pristupa bazi podataka
     */
    public void save(Connection conn, T entity) throws DatabaseException {
        try (PreparedStatement ps = conn.prepareStatement(getInsertSql(), Statement.RETURN_GENERATED_KEYS)) {
            bindInsert(ps, entity);
            int affected = ps.executeUpdate();
            if (affected != 1) throw new SQLException("Očekivan 1 red, utjecano: " + affected);

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setId(keys.getLong(1));
                } else if (entity.getId() == null) {
                    throw new SQLException("Primarni ključ nije vraćen iz baze");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Greška pri spremanju " + type.getSimpleName(), e);
        }
    }

    /**
     * Ažurira postojeći entitet u bazi podataka. Ako entitet nema ID, ili ako dođe do greške
     * tijekom ažuriranja, baca se iznimka.
     *
     * @param entity entitet koji se treba ažurirati u bazi podataka
     * @throws DatabaseException ako entitet nema ID ili ako dođe do greške prilikom ažuriranja
     */
    public final void update(T entity) throws DatabaseException {
        if (entity.getId() == null)
            throw new DatabaseException("ID je obavezan za ažuriranje " + type.getSimpleName());
        try (Connection conn = DbUtil.connectToDatabase()) {
            update(conn, entity);
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DatabaseException("Greška pri ažuriranju " + type.getSimpleName(), e);
        }
    }

    /**
     * Ažurira postojeći entitet u bazi podataka pomoću zadane Connection instance.
     * Ako entitet nema ID ili ažuriranje ne uspije, baca se iznimka.
     *
     * @param conn veza na bazu podataka koja će se koristiti za izvršenje ažuriranja
     * @param entity entitet koji treba ažurirati u bazi podataka
     * @throws DatabaseException ako entitet nema ID, ako zapis ne postoji ili ako dođe do greške prilikom ažuriranja
     */
    public void update(Connection conn, T entity) throws DatabaseException {
        if (entity.getId() == null)
            throw new DatabaseException("ID je obavezan za ažuriranje " + type.getSimpleName());
        try (PreparedStatement ps = conn.prepareStatement(getUpdateSql())) {
            bindUpdate(ps, entity);
            int affected = ps.executeUpdate();
            if (affected != 1) {
                if (affected == 0)
                    throw new DatabaseException(type.getSimpleName() + " s ID=" + entity.getId() + " ne postoji");
                throw new SQLException("Očekivan 1 red, utjecano: " + affected);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Greška pri ažuriranju " + type.getSimpleName(), e);
        }
    }

    /**
     * Briše entitet iz baze podataka prema zadanom ID-u.
     *
     * @param id ID entiteta koji treba biti obrisan
     * @throws DatabaseException ako dođe do greške prilikom brisanja iz baze podataka
     */
    public final void delete(Long id) throws DatabaseException {
        try (Connection conn = DbUtil.connectToDatabase()) {
            delete(conn, id);
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DatabaseException("Greška pri brisanju " + type.getSimpleName() + " s ID=" + id, e);
        }
    }

    /**
     * Briše entitet iz baze podataka prema zadanom ID-u koristeći predanu vezu.
     *
     * @param conn veza na bazu podataka koja će se koristiti za izvršenje brisanja
     * @param id   ID entiteta koji treba biti obrisan
     * @throws DatabaseException ako dođe do greške prilikom brisanja iz baze podataka
     */
    public void delete(Connection conn, Long id) throws DatabaseException {
        try (PreparedStatement ps = conn.prepareStatement(getDeleteSql())) {
            ps.setLong(1, id);
            int affected = ps.executeUpdate();
            if (affected != 1) {
                if (affected == 0)
                    throw new DatabaseException(type.getSimpleName() + " s ID=" + id + " ne postoji");
                throw new SQLException("Očekivan 1 red, utjecano: " + affected);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Greška pri brisanju " + type.getSimpleName() + " s ID=" + id, e);
        }
    }

    /**
     * Dohvaća opcionalni entitet iz baze podataka prema zadanom ID-u.
     *
     * @param id ID entiteta koji se traži
     * @return Optional s entitetom ako je pronađen, inače prazan Optional
     * @throws DatabaseAccessException ako dođe do greške pri pristupu bazi
     */

    public final Optional<T> findById(Long id) throws DatabaseException {
        try (Connection conn = DbUtil.connectToDatabase()) {
            return findById(conn, id);
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DatabaseException("Greška pri dohvaćanju " + type.getSimpleName() + " s ID=" + id, e);
        }
    }

    /**
     * Dohvaća opcionalni entitet iz baze podataka na temelju danog ID-a koristeći predanu vezu.
     *
     * @param conn veza na bazu podataka koja se koristi za dohvaćanje podataka
     * @param id ID entiteta koji se traži
     * @return Optional s entitetom ako je pronađen, inače prazan Optional
     * @throws DatabaseException ako dođe do greške pri izvršenju upita ili pristupu bazi podataka
     */
    public Optional<T> findById(Connection conn, Long id) throws DatabaseException {
        try (PreparedStatement ps = conn.prepareStatement(getSelectByIdSql())) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Greška pri dohvaćanju " + type.getSimpleName() + " s ID=" + id, e);
        }
    }

    /**
     * Dohvaća listu svih objekata generičkog tipa T iz baze podataka.
     *
     * @return lista svih objekata generičkog tipa T
     * @throws DatabaseAccessException ako dođe do greške pri pristupu bazi podataka
     */
    public final List<T> findAll() throws DatabaseException {
        try (Connection conn = DbUtil.connectToDatabase()) {
            return findAll(conn);
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DatabaseException("Greška pri dohvaćanju svih objekata " + type.getSimpleName(), e);
        }
    }

    /**
     * Dohvaća sve zapise generičkog tipa T iz baze podataka koristeći predanu vezu na bazu.
     *
     * @param conn veza na bazu podataka koja se koristi za izvođenje upita
     * @return lista svih objekata generičkog tipa T iz baze podataka
     * @throws DatabaseException ako dođe do greške prilikom pristupa bazi podataka
     */
    public List<T> findAll(Connection conn) throws DatabaseException {
        List<T> results = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(getSelectAllSql());
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) results.add(mapRow(rs));
            return results;
        } catch (SQLException e) {
            throw new DatabaseException("Greška pri dohvaćanju svih objekata " + type.getSimpleName(), e);
        }
    }

    /**
     * Vraća SQL izraz za umetanje entiteta u bazu podataka.
     *
     * @return SQL izraz za INSERT operaciju
     */
    protected abstract String getInsertSql();

    /**
     * Vraća SQL izraz za ažuriranje entiteta u bazi podataka.
     *
     * @return SQL izraz za UPDATE operaciju
     */
    protected abstract String getUpdateSql();

    /**
     * Vraća SQL izraz za brisanje entiteta iz baze podataka.
     *
     * @return SQL izraz za DELETE operaciju
     */
    protected abstract String getDeleteSql();

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
}
