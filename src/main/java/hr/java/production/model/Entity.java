package hr.java.production.model;

import java.io.Serializable;
import java.util.Objects;


/**
 * Apstraktna klasa koja predstavlja generički entitet s jedinstvenim identifikatorom.
 * Namijenjena je za nasljeđivanje, omogućujući dodatno definiranje atributa i ponašanja specifičnih entiteta.
 */
public abstract class Entity implements Serializable {
    private Long id;

    protected Entity() {}

    protected Entity(Long id) {
        this.id = id;
    }

    /**
     * Generička bazna klasa Buildera za entitete.
     *
     * @param <T> tip entiteta koji se gradi
     * @param <B> tip konkretne klase Buildera
     */
    //https://stackoverflow.com/questions/53189891/is-it-legal-to-make-a-type-parameter-recursive-for-an-already-generic-class
    public abstract static class Builder<T extends Entity, B extends Builder<T, B>> {
        protected Long id;

        /**
         * Postavlja ID entiteta koji se gradi.
         *
         * @param id jedinstveni identifikator
         * @return instanca Buildera
         */
        public B id(Long id) {
            this.id = id;
            return self();
        }

        /**
         * Vraća instancu Buildera odgovarajućeg tipa.
         * Implementira ga svaki konkretni Builder.
         *
         * @return instanca Buildera tipa B
         */
        protected abstract B self();

        /**
         * Stvara i vraća instancu entiteta tipa T.
         *
         * @return nova instanca entiteta
         */
        public abstract T build();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
