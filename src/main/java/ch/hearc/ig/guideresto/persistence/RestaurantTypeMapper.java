package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.RestaurantType;
import jakarta.persistence.EntityManager;
import java.util.List;

public class RestaurantTypeMapper {

    // ========= CRUD ========= //

    public RestaurantType findById(EntityManager em, Integer id) {
        return em.find(RestaurantType.class, id);
    }

    public List<RestaurantType> findAll(EntityManager em) {
        return em.createNamedQuery("RestaurantType.findAll", RestaurantType.class)
                .getResultList();
    }

    public void save(EntityManager em, RestaurantType type) {
        if (type.getId() == null) {
            em.persist(type);
        } else {
            em.merge(type);
        }
    }

    public void delete(EntityManager em, RestaurantType type) {
        em.remove(em.merge(type));
    }


    // ========= Fonctions de recherche pratiques ========= //

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
