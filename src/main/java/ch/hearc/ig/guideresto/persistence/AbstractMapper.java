package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.IBusinessObject;
import jakarta.persistence.EntityManager;

import java.util.Set;

import static java.util.stream.Collectors.toUnmodifiableSet;

public abstract class AbstractMapper<T extends IBusinessObject> {

    private final Class<T> entityClass;
    private final String findAllQueryName;

    protected AbstractMapper(Class<T> entityClass, String findAllQueryName) {
        this.entityClass = entityClass;
        this.findAllQueryName = findAllQueryName;
    }

    // ========= FIND ALL ========= //

    public Set<T> findAll(EntityManager em) {
        return em
                .createNamedQuery(findAllQueryName, entityClass)
                .getResultStream()
                .collect(toUnmodifiableSet());
    }

    // ========= DELETE ========= //

    public void delete(EntityManager em, T entity) {
        if (!em.contains(entity)) {
            entity = em.merge(entity);
        }
        em.remove(entity);
    }

}
