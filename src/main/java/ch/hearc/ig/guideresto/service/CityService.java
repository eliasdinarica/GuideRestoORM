package ch.hearc.ig.guideresto.service;
import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.persistence.CityMapper;
import ch.hearc.ig.guideresto.persistence.jpa.JpaUtils;

import java.util.Set;

public class CityService {

    private final CityMapper mapper = new CityMapper();

    // =====================================================
    // LECTURE
    // =====================================================

    public City getById(Integer id) {

        class Holder { City value; }
        Holder h = new Holder();

        JpaUtils.inTransaction(em -> {
            h.value = mapper.findById(em, id);
        });

        return h.value;
    }

    public Set<City> getAll() {

        class Holder { Set<City> value; }
        Holder h = new Holder();

        JpaUtils.inTransaction(em -> {
            h.value = mapper.findAll(em);
        });

        return h.value;
    }

    public City getByZipAndName(String zip, String name) {

        class Holder { City value; }
        Holder h = new Holder();

        JpaUtils.inTransaction(em -> {
            h.value = mapper.findByZipAndName(em, zip, name);
        });

        return h.value;
    }

    public Set<City> getByZipCode(String zipCode) {

        class Holder { Set<City> value; }
        Holder h = new Holder();

        JpaUtils.inTransaction(em -> {
            h.value = Set.copyOf(mapper.findByZipCode(em, zipCode));
        });

        return h.value;
    }
}