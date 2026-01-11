package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.Restaurant;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toUnmodifiableSet;

public class RestaurantMapper extends AbstractMapper<Restaurant> {

    public RestaurantMapper() {
        super(Restaurant.class, "Restaurant.findAll");
    }


    // ========= CRUD ========= //
    public void save(EntityManager em, Restaurant restaurant) {
        if (restaurant.getId() == null) {
            em.persist(restaurant);
        } else {
            em.merge(restaurant);
        }
    }

    // ========= Recherches ========= //

    public Restaurant findById(EntityManager em, Integer id) {
        return em.find(Restaurant.class, id);
    }
    public Set<Restaurant> findByCity(EntityManager em, Integer cityId) {
        return em.createNamedQuery("Restaurant.findByCity", Restaurant.class)
                .setParameter("cityId", cityId)
                .getResultStream()
                .collect(toUnmodifiableSet());
    }

    public Set<Restaurant> findByType(EntityManager em, Integer typeId) {
        return em.createNamedQuery("Restaurant.findByType", Restaurant.class)
                .setParameter("typeId", typeId)
                .getResultStream()
                .collect(toUnmodifiableSet());
    }
}
