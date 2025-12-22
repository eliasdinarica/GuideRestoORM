package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * @author cedric.baudet
 */
@Entity
@Table(name = "villes")
@NamedQuery(
        name = "City.findByZipAndName",
        query = """
            SELECT c FROM City c
            WHERE c.zipCode = :zip
              AND LOWER(c.cityName) = LOWER(:name)
            """
)
@NamedQuery(
        name = "City.findAll",
        query = "SELECT c FROM City c ORDER BY c.cityName"
)
@NamedQuery(
        name = "City.findByName",
        query = """
                SELECT c
                FROM City c
                WHERE LOWER(c.cityName) = LOWER(:name)
                """
)
@NamedQuery(
        name = "City.findByZipCode",
        query = """
                SELECT c
                FROM City c
                WHERE c.zipCode = :zip
                ORDER BY c.cityName
                """
)
public class City implements IBusinessObject {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "villes_seq")
    @SequenceGenerator(
            name = "villes_seq",
            sequenceName = "SEQ_VILLES",
            allocationSize = 1
    )
    @Column(name = "NUMERO")
    private Integer id;
    @Column(name = "CODE_POSTAL")
    private String zipCode;
    @Column(name = "NOM_VILLE")
    private String cityName;
    @OneToMany(mappedBy = "address.city")
    private Set<Restaurant> restaurants;

    public City() {
        this(null, null);
    }

    public City(String zipCode, String cityName) {
        this(null, zipCode, cityName);
    }

    public City(Integer id, String zipCode, String cityName) {
        this.id = id;
        this.zipCode = zipCode;
        this.cityName = cityName;
        this.restaurants = new HashSet();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String city) {
        this.cityName = city;
    }

    public Set<Restaurant> getRestaurants() {
        return restaurants;
    }

    public void setRestaurants(Set<Restaurant> restaurants) {
        this.restaurants = restaurants;
    }

}