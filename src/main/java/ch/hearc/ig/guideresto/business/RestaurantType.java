package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * @author cedric.baudet
 */
@Entity
@Table(name = "types_gastronomiques")
@NamedQueries({
        @NamedQuery(
                name = "RestaurantType.findAll",
                query = "SELECT t FROM RestaurantType t ORDER BY t.label"
        ),
        @NamedQuery(
                name = "RestaurantType.findByLabel",
                query = "SELECT t FROM RestaurantType t WHERE t.label = :label"
        ),
        @NamedQuery(
                name = "RestaurantType.searchByLabel",
                query = "SELECT t FROM RestaurantType t WHERE LOWER(t.label) LIKE LOWER(:text)"
        )
})
public class RestaurantType implements IBusinessObject {
    @Id
    @Column(name = "NUMERO")
    private Integer id;
    @Column(name = "LIBELLE")
    private String label;
    @Column(name = "DESCRIPTION")
    private String description;
    @OneToMany(mappedBy = "type")
    private Set<Restaurant> restaurants;

    public RestaurantType() {
        this(null, null);
    }

    public RestaurantType(String label, String description) {
        this(null, label, description);
    }

    public RestaurantType(Integer id, String label, String description) {
        this.restaurants = new HashSet();
        this.id = id;
        this.label = label;
        this.description = description;
    }

    @Override
    public String toString() {
        return label;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Restaurant> getRestaurants() {
        return restaurants;
    }

    public void setRestaurants(Set<Restaurant> restaurants) {
        this.restaurants = restaurants;
    }

}