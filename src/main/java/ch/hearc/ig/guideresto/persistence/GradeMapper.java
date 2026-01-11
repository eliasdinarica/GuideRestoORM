package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.Grade;
import jakarta.persistence.EntityManager;

import java.util.Set;

import static java.util.stream.Collectors.toUnmodifiableSet;

public class GradeMapper extends AbstractMapper<Grade> {

    public GradeMapper() {
        super(Grade.class, "Grade.findAll"); // optionnel si tu ajoutes findAll
    }

    // ========= CRUD ========= //

    public void save(EntityManager em, Grade grade) {
        if (grade.getId() == null) {
            em.persist(grade);
        } else {
            em.merge(grade);
        }
    }


    // ========= Recherches ========= //

    public Grade findById(EntityManager em, Integer id) {
        return em.find(Grade.class, id);
    }

    public Set<Grade> findByEvaluation(EntityManager em, Integer evaluationId) {
        return em.createNamedQuery("Grade.findByEvaluation", Grade.class)
                .setParameter("evaluationId", evaluationId)
                .getResultStream()
                .collect(toUnmodifiableSet());
    }

    public Set<Grade> findByCriteria(EntityManager em, Integer criteriaId) {
        return em.createNamedQuery("Grade.findByCriteria", Grade.class)
                .setParameter("criteriaId", criteriaId)
                .getResultStream()
                .collect(toUnmodifiableSet());
    }
}

