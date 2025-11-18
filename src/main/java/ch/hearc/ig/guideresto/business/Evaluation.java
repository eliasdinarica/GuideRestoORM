package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Evaluation implements IBusinessObject {

    @Id
    @Column(name = "NUMERO")
    private Integer id;

    @Temporal(TemporalType.DATE)
    @Column(name = "DATE_EVAL", nullable = false)
    private Date visitDate;

    @ManyToOne
    @JoinColumn(name = "FK_REST", nullable = false)
    private Restaurant restaurant;

    public Evaluation() {
    }

    public Evaluation(Integer id, Date visitDate, Restaurant restaurant) {
        this.id = id;
        this.visitDate = visitDate;
        this.restaurant = restaurant;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(Date visitDate) {
        this.visitDate = visitDate;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }
}
