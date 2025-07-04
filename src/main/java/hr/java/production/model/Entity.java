package hr.java.production.model;

import java.io.Serializable;

public abstract class Entity implements Serializable {
    private Long id;

    public Entity() {}

    public Entity(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
