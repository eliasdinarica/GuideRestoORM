package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

@Entity
@Table(name = "NOTES")
public class Grade implements IBusinessObject {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_notes")
    @SequenceGenerator(name = "seq_notes", sequenceName = "SEQ_NOTES", allocationSize = 1)
    @Column(name = "NUMERO")
    private Integer id;

    @Column(name = "NOTE", nullable = false)
    private Integer grade;

    @ManyToOne
    @JoinColumn(name = "FK_COMM", nullable = false)
    private CompleteEvaluation evaluation;

    @ManyToOne
    @JoinColumn(name = "FK_CRIT", nullable = false)
    private EvaluationCriteria criteria;

    public Grade() {
    }

    public Grade(Integer grade, CompleteEvaluation evaluation, EvaluationCriteria criteria) {
        this.grade = grade;
        this.evaluation = evaluation;
        this.criteria = criteria;
    }

    public Grade(Integer id, Integer grade, CompleteEvaluation evaluation, EvaluationCriteria criteria) {
        this.id = id;
        this.grade = grade;
        this.evaluation = evaluation;
        this.criteria = criteria;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public CompleteEvaluation getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(CompleteEvaluation evaluation) {
        this.evaluation = evaluation;
    }

    public EvaluationCriteria getCriteria() {
        return criteria;
    }

    public void setCriteria(EvaluationCriteria criteria) {
        this.criteria = criteria;
    }
}
