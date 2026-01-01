package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.EvaluationCriteria;
import jakarta.persistence.EntityManager;

import java.util.Set;

import static java.util.stream.Collectors.toUnmodifiableSet;

public class EvaluationCriteriaMapper extends AbstractMapper<EvaluationCriteria> {

    public EvaluationCriteriaMapper() {
        super(EvaluationCriteria.class, "EvaluationCriteria.findAll");
    }

    public EvaluationCriteria findById(EntityManager em, Integer id) {
        return em.find(EvaluationCriteria.class, id);
    }
}

