package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.City;
import jakarta.persistence.EntityManager;

import java.util.List;

public class CityMapper extends AbstractMapper<City> {

    public CityMapper() {
        super(City.class, "City.findAll");
    }

    // ========= CRUD ========= //

    public City findById(EntityManager em, Integer id) {
        return em.find(City.class, id);
    }

    public void save(EntityManager em, City city) {
        if (city.getId() == null) {
            em.persist(city);
        } else {
            em.merge(city);
        }
    }



    // ========= Recherches ========= //

    public City findByName(EntityManager em, String name) {
        return em.createNamedQuery("City.findByName", City.class)
                .setParameter("name", name)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public List<City> findByZipCode(EntityManager em, String zipCode) {
        return em.createNamedQuery("City.findByZipCode", City.class)
                .setParameter("zip", zipCode)
                .getResultList();
    }

    public City findByZipAndName(EntityManager em, String zip, String name) {
        return em.createNamedQuery("City.findByZipAndName", City.class)
                .setParameter("zip", zip)
                .setParameter("name", name)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }
}

