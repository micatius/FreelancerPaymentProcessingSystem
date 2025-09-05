package hr.java.production.log;

import hr.java.production.model.Entity;

import java.io.Serializable;
import java.util.List;

/**
 * Sučelje za zapisivanje i čitanje promjena nad entitetima.
 * Implementacije definiraju konkretan način spremanja (binarno, JSON, baza...).
 */
public sealed interface ChangeLogger permits BinaryChangeLogger {

    /**
     * Zapiše CREATE promjenu entiteta.
     *
     * @param newValue nova vrijednost entiteta
     * @param <T>      tip entiteta
     */
    <T extends Entity & Serializable> void logCreate(T newValue);

    /**
     * Zapiše UPDATE promjenu entiteta.
     *
     * @param oldValue prethodna vrijednost
     * @param newValue nova vrijednost
     * @param <T>      tip entiteta
     */
    <T extends Entity & Serializable> void logUpdate(T oldValue, T newValue);

    /**
     * Zapiše DELETE promjenu entiteta.
     *
     * @param oldValue obrisana vrijednost
     * @param <T>      tip entiteta
     */
    <T extends Entity & Serializable> void logDelete(T oldValue);

    /**
     * Vraća sve zapise određenog tipa.
     *
     * @param type tip entiteta (npr. Invoice.class)
     * @param <T>  tip entiteta
     * @return lista zapisa tog tipa
     */
    <T extends Entity & Serializable> List<ChangeLog<T>> readAll(Class<T> type);
}
