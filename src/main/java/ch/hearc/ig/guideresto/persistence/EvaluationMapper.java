package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.Evaluation;
import ch.hearc.ig.guideresto.business.CompleteEvaluation;
import jakarta.persistence.EntityManager;

import java.util.Set;

import static java.util.stream.Collectors.toUnmodifiableSet;

public class EvaluationMapper extends AbstractMapper<Evaluation> {

    public EvaluationMapper() {
        super(Evaluation.class, "Evaluation.findAll"); // optionnel si tu ajoutes findAll
    }

    // ========= CRUD ========= //

    public void save(EntityManager em, Evaluation evaluation) {
        if (evaluation.getId() == null) {
            em.persist(evaluation);
        } else {
            em.merge(evaluation);
        }
    }


    // ========= Recherches ========= //

    public Evaluation findById(EntityManager em, Integer id) {
        return em.find(Evaluation.class, id);
    }

    public Set<Evaluation> findByRestaurant(EntityManager em, Integer restaurantId) {
        return em.createNamedQuery("Evaluation.findByRestaurant", Evaluation.class)
                .setParameter("restaurantId", restaurantId)
                .getResultStream()
                .collect(toUnmodifiableSet());
    }

    public Set<CompleteEvaluation> findCompleteByRestaurant(EntityManager em, Integer restaurantId) {
        return em.createNamedQuery(
                        "CompleteEvaluation.findByRestaurant",
                        CompleteEvaluation.class
                )
                .setParameter("restaurantId", restaurantId)
                .getResultStream()
                .collect(toUnmodifiableSet());
    }
}