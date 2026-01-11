package ch.hearc.ig.guideresto.service;

import ch.hearc.ig.guideresto.business.*;
import ch.hearc.ig.guideresto.persistence.*;
import ch.hearc.ig.guideresto.persistence.jpa.JpaUtils;

import java.util.Date;
import java.util.Map;

public class EvaluationService {

    private final RestaurantMapper restaurantMapper = new RestaurantMapper();
    private final EvaluationMapper evaluationMapper = new EvaluationMapper();
    private final GradeMapper gradeMapper = new GradeMapper();
    private final EvaluationCriteriaMapper criteriaMapper = new EvaluationCriteriaMapper();

    public BasicEvaluation addBasicEvaluation(
            Integer restaurantId,
            Boolean likeRestaurant,
            String ipAddress
    ) {

        class Holder { BasicEvaluation value; }
        Holder h = new Holder();

        JpaUtils.inTransaction(em -> {

            Restaurant restaurant = restaurantMapper.findById(em, restaurantId);
            if (restaurant == null) {
                throw new IllegalArgumentException("Restaurant inexistant");
            }

            BasicEvaluation evaluation = new BasicEvaluation(
                    new Date(),
                    restaurant,
                    likeRestaurant,
                    ipAddress
            );

            evaluationMapper.save(em, evaluation);
            h.value = evaluation;
        });

        return h.value;
    }

    // =========================================================
    // COMPLETE EVALUATION + GRADES
    // =========================================================

    public CompleteEvaluation addCompleteEvaluation(
            Integer restaurantId,
            String username,
            String comment,
            Map<Integer, Integer> gradesByCriteriaId
    ) {

        class Holder { CompleteEvaluation value; }
        Holder h = new Holder();

        JpaUtils.inTransaction(em -> {

            // 1️. Restaurant existant
            Restaurant restaurant = restaurantMapper.findById(em, restaurantId);
            if (restaurant == null) {
                throw new IllegalArgumentException("Restaurant inexistant");
            }

            // 2. Création de l’évaluation complète
            CompleteEvaluation evaluation = new CompleteEvaluation(
                    new Date(),
                    restaurant,
                    comment,
                    username
            );

            evaluationMapper.save(em, evaluation);

            // 3. Création des notes
            for (Map.Entry<Integer, Integer> entry : gradesByCriteriaId.entrySet()) {

                Integer criteriaId = entry.getKey();
                Integer note = entry.getValue();

                if (note < 1 || note > 5) {
                    throw new IllegalArgumentException(
                            "La note doit être comprise entre 1 et 5"
                    );
                }

                EvaluationCriteria criteria =
                        criteriaMapper.findById(em, criteriaId);

                if (criteria == null) {
                    throw new IllegalArgumentException(
                            "Critère invalide (id=" + criteriaId + ")"
                    );
                }

                Grade grade = new Grade(note, evaluation, criteria);
                gradeMapper.save(em, grade);

                evaluation.getGrades().add(grade);
            }

            h.value = evaluation;
        });

        return h.value;
    }
}

