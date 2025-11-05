package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

/**
 * @author cedric.baudet
 */
@Entity
@Table(name = "criteres_evaluation")
public class EvaluationCriteria implements IBusinessObject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NUMERO")
    private Integer id;
    @Column(name = "NOM")
    private String name;
    @Column(name = "DESCRIPTION")
    private String description;

    public EvaluationCriteria() {
        this(null, null);
    }

    public EvaluationCriteria(String name, String description) {
        this(null, name, description);
    }

    public EvaluationCriteria(Integer id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}