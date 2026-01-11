package ch.hearc.ig.guideresto.service;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.Localisation;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.business.RestaurantType;
import ch.hearc.ig.guideresto.persistence.CityMapper;
import ch.hearc.ig.guideresto.persistence.RestaurantMapper;
import ch.hearc.ig.guideresto.persistence.RestaurantTypeMapper;
import ch.hearc.ig.guideresto.persistence.jpa.JpaUtils;
import jakarta.persistence.LockModeType;
import jakarta.persistence.OptimisticLockException;

import java.util.Set;

public class RestaurantService {

    private final RestaurantMapper restaurantMapper = new RestaurantMapper();
    private final CityMapper cityMapper = new CityMapper();
    private final RestaurantTypeMapper typeMapper = new RestaurantTypeMapper();

    public Restaurant create(Restaurant restaurant) {

        class Holder { Restaurant value; }
        Holder h = new Holder();

        JpaUtils.inTransaction(em -> {


            if (restaurant.getId() != null) {
                throw new IllegalArgumentException(
                        "Un restaurant à créer ne doit pas avoir d'id"
                );
            }


            City city = restaurant.getAddress().getCity();

            city = cityMapper.findByZipAndName(
                    em,
                    city.getZipCode(),
                    city.getCityName()
            );

            if (city == null) {
                city = restaurant.getAddress().getCity();
                cityMapper.save(em, city);
            }

            restaurant.getAddress().setCity(city);

            RestaurantType type = typeMapper.findById(
                    em,
                    restaurant.getType().getId()
            );

            if (type == null) {
                throw new IllegalArgumentException("Type invalide");
            }

            restaurant.setType(type);


            restaurantMapper.save(em, restaurant);
            h.value = restaurant;
        });

        return h.value;
    }
    public void update(Restaurant restaurant) {

        JpaUtils.inTransaction(em -> {
            try {
                Restaurant managed = em.find(Restaurant.class, restaurant.getId());
                if (managed == null) {
                    throw new IllegalArgumentException("Restaurant inexistant");
                }

                managed.setName(restaurant.getName());
                managed.setDescription(restaurant.getDescription());
                managed.setWebsite(restaurant.getWebsite());
                managed.setAddress(restaurant.getAddress());
                managed.setType(restaurant.getType());

            } catch (OptimisticLockException e) {
                throw new IllegalStateException(
                        "Modification concurrente détectée sur le restaurant"
                );
            }
        });
    }
    public Restaurant getById(Integer id) {

        class Holder { Restaurant value; }
        Holder h = new Holder();

        JpaUtils.inTransaction(em -> {
            h.value = restaurantMapper.findById(em, id);
        });

        return h.value;
    }
    public Set<Restaurant> getAll() {

        class Holder { Set<Restaurant> value; }
        Holder h = new Holder();

        JpaUtils.inTransaction(em -> {
            h.value = restaurantMapper.findAll(em);
        });

        return h.value;
    }
    public Set<Restaurant> getByCity(Integer cityId) {

        class Holder { Set<Restaurant> value; }
        Holder h = new Holder();

        JpaUtils.inTransaction(em -> {
            h.value = restaurantMapper.findByCity(em, cityId);
        });

        return h.value;
    }
    public Set<Restaurant> getByType(Integer typeId) {

        class Holder { Set<Restaurant> value; }
        Holder h = new Holder();

        JpaUtils.inTransaction(em -> {
            h.value = restaurantMapper.findByType(em, typeId);
        });

        return h.value;
    }
    public void delete(Integer restaurantId) {
        JpaUtils.inTransaction(em -> {
            Restaurant restaurant = restaurantMapper.findById(em, restaurantId);
            if (restaurant == null) {
                throw new IllegalArgumentException("Restaurant inexistant");
            }
            restaurantMapper.delete(em, restaurant);
        });
    }







}