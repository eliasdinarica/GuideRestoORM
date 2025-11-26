package ch.hearc.ig.guideresto.service;

import ch.hearc.ig.guideresto.business.RestaurantType;
import ch.hearc.ig.guideresto.persistence.RestaurantTypeMapper;
import ch.hearc.ig.guideresto.persistence.jpa.JpaUtils;

import java.util.List;

public class RestaurantTypeService {

    private final RestaurantTypeMapper mapper = new RestaurantTypeMapper();

    // ========= READ ========= //

    public RestaurantType getById(Integer id) {

        class Holder { RestaurantType value; }
        Holder h = new Holder();

        JpaUtils.inTransaction(em -> {
            h.value = mapper.findById(em, id);
        });

        return h.value;
    }


    public List<RestaurantType> getAll() {

        class Holder { List<RestaurantType> value; }
        Holder h = new Holder();

        JpaUtils.inTransaction(em -> {
            h.value = mapper.findAll(em);
        });

        return h.value;
    }

}
