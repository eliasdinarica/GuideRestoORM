package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.RestaurantType;
import jakarta.persistence.EntityManager;

import java.util.List;

public class RestaurantTypeMapper extends AbstractMapper<RestaurantType> {

    public RestaurantTypeMapper() {
        super(RestaurantType.class, "RestaurantType.findAll");
    }

    // ========= CRUD ========= //

    public void save(EntityManager em, RestaurantType type) {
        if (type.getId() == null) {
            em.persist(type);
        } else {
            em.merge(type);
        }
    }


    // ========= Recherches ========= //

    public RestaurantType findById(EntityManager em, Integer id) {
        return em.find(RestaurantType.class, id);
    }

    public List<RestaurantType> findByLabel(EntityManager em, String label) {
        return em.createNamedQuery("RestaurantType.findByLabel", RestaurantType.class)
                .setParameter("label", label)
                .getResultList();
    }

    public List<RestaurantType> searchByLabel(EntityManager em, String text) {
        return em.createNamedQuery("RestaurantType.searchByLabel", RestaurantType.class)
                .setParameter("text", "%" + text + "%")
                .getResultList();
    }
}
