package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.CompleteEvaluation;
import ch.hearc.ig.guideresto.business.EvaluationCriteria;
import ch.hearc.ig.guideresto.business.Grade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Mapper pour la gestion des notes ({@link Grade}).
 * Assure la correspondance entre la table NOTES et les objets métier.
 *
 * Implémente une Identity Map pour garantir l’unicité des instances.
 */
public class GradeMapper extends AbstractMapper<Grade> {

    /** Cache local des notes déjà chargées. */
    protected static final Map<Integer, Grade> identityMap = new HashMap<>();
    @Override
    protected Map<Integer, Grade> getIdentityMap() {
        return identityMap;
    }

    /**
     * Recherche une note (grade) par son identifiant.
     * Si elle existe déjà dans le cache, elle est renvoyée directement.
     *
     * @param id identifiant unique de la note
     * @return la note correspondante, ou {@code null} si absente
     */
    @Override
    public Grade findById(int id) {
        if (!isCacheEmpty() && identityMap.containsKey(id)) {
            logger.debug("Grade {} trouvé dans le cache.", id);
            return identityMap.get(id);
        }

        String sql = "SELECT * FROM NOTES WHERE NUMERO = ?";
        Connection c = ConnectionUtils.getConnection();

        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, id);

            try (ResultSet rs = s.executeQuery()) {
                if (!rs.next()) return null;

                Grade grade = new Grade();
                grade.setId(rs.getInt("NUMERO"));
                grade.setGrade(rs.getInt("NOTE"));

                // 🔹 Création de proxies légers pour lazy loading
                CompleteEvaluation evaluation = new CompleteEvaluation();
                evaluation.setId(rs.getInt("FK_COMM"));
                grade.setEvaluation(evaluation);

                EvaluationCriteria criteria = new EvaluationCriteria();
                criteria.setId(rs.getInt("FK_CRIT"));
                grade.setCriteria(criteria);

                addToCache(grade);
                logger.debug("Grade {} ajouté au cache.", id);
                return grade;
            }

        } catch (SQLException ex) {
            logger.error("SQLException in findById({}): {}", id, ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Récupère toutes les notes enregistrées dans la base.
     *
     * @return un ensemble de toutes les notes
     */
    @Override
    public Set<Grade> findAll() {
        Set<Grade> grades = new HashSet<>();
        resetCache();

        String sql = "SELECT * FROM NOTES";
        Connection c = ConnectionUtils.getConnection();

        try (PreparedStatement s = c.prepareStatement(sql);
             ResultSet rs = s.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("NUMERO");

                // ✅ Réutilisation du cache si déjà présent
                Grade grade = identityMap.get(id);
                if (grade == null) {
                    grade = new Grade();
                    grade.setId(id);
                    grade.setGrade(rs.getInt("NOTE"));

                    CompleteEvaluation evaluation = new CompleteEvaluation();
                    evaluation.setId(rs.getInt("FK_COMM"));
                    grade.setEvaluation(evaluation);

                    EvaluationCriteria criteria = new EvaluationCriteria();
                    criteria.setId(rs.getInt("FK_CRIT"));
                    grade.setCriteria(criteria);

                    addToCache(grade);
                }

                grades.add(grade);
            }

            logger.debug("findAll() : {} Grades chargés depuis la DB.", grades.size());

        } catch (SQLException ex) {
            logger.error("SQLException in findAll(): {}", ex.getMessage(), ex);
        }

        return grades;
    }

    /**
     * Crée une nouvelle note et la persiste dans la base.
     *
     * @param object la note à insérer
     * @return la note créée et ajoutée au cache, ou {@code null} en cas d’erreur
     */
    @Override
    public Grade create(Grade object) {
        Connection c = ConnectionUtils.getConnection();

        try {
            int nextId = getSequenceValue();
            object.setId(nextId);

            String sql = "INSERT INTO NOTES (NUMERO, NOTE, FK_COMM, FK_CRIT) VALUES (?, ?, ?, ?)";

            try (PreparedStatement s = c.prepareStatement(sql)) {
                s.setInt(1, object.getId());
                s.setInt(2, object.getGrade());
                s.setInt(3, object.getEvaluation().getId());
                s.setInt(4, object.getCriteria().getId());
                s.executeUpdate();
                c.commit();
            }

            addToCache(object);
            logger.debug("Grade {} ajouté au cache après création.", object.getId());
            return object;

        } catch (SQLException e) {
            logger.error("SQLException in create(): {}", e.getMessage());
            return null;
        }
    }

    /**
     * Met à jour une note existante.
     *
     * @param object la note à mettre à jour
     * @return {@code true} si la mise à jour a réussi
     */
    @Override
    public boolean update(Grade object) {
        Connection c = ConnectionUtils.getConnection();
        String sql = """
                UPDATE NOTES
                SET NOTE = ?, FK_COMM = ?, FK_CRIT = ?
                WHERE NUMERO = ?
                """;

        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, object.getGrade());
            s.setInt(2, object.getEvaluation().getId());
            s.setInt(3, object.getCriteria().getId());
            s.setInt(4, object.getId());
            s.executeUpdate();
            c.commit();

            addToCache(object);
            logger.debug("Grade {} mis à jour dans le cache.", object.getId());
            return true;

        } catch (SQLException e) {
            logger.error("SQLException in update(): {}", e.getMessage());
            return false;
        }
    }

    /**
     * Supprime une note de la base.
     *
     * @param object la note à supprimer
     * @return {@code true} si la suppression a réussi
     */
    @Override
    public boolean delete(Grade object) {
        Connection c = ConnectionUtils.getConnection();
        String sql = "DELETE FROM NOTES WHERE NUMERO = ?";

        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, object.getId());
            s.executeUpdate();
            c.commit();

            removeFromCache(object.getId());
            logger.debug("Grade {} supprimé du cache et de la DB.", object.getId());
            return true;

        } catch (SQLException e) {
            logger.error("SQLException in delete(): {}", e.getMessage());
            return false;
        }
    }

    /**
     * Supprime une note par son identifiant.
     *
     * @param id identifiant de la note
     * @return {@code true} si la suppression a réussi
     */
    @Override
    public boolean deleteById(int id) {
        Grade grade = findById(id);
        if (grade == null) return false;
        return delete(grade);
    }

    @Override
    protected String getSequenceQuery() {
        return "SELECT SEQ_NOTES.NEXTVAL FROM DUAL";
    }

    @Override
    protected String getExistsQuery() {
        return "SELECT 1 FROM NOTES WHERE NUMERO = ?";
    }

    @Override
    protected String getCountQuery() {
        return "SELECT COUNT(*) FROM NOTES";
    }

    /**
     * Recherche toutes les notes associées à une évaluation complète.
     *
     * @param evaluation l’évaluation concernée
     * @return un ensemble de notes associées
     */
    public Set<Grade> findByEvaluation(CompleteEvaluation evaluation) {
        Set<Grade> grades = new HashSet<>();
        String sql = "SELECT * FROM NOTES WHERE FK_COMM = ?";
        Connection c = ConnectionUtils.getConnection();

        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, evaluation.getId());

            try (ResultSet rs = s.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("NUMERO");

                    Grade grade = identityMap.get(id);
                    if (grade == null) {
                        grade = new Grade();
                        grade.setId(id);
                        grade.setGrade(rs.getInt("NOTE"));
                        grade.setEvaluation(evaluation);

                        EvaluationCriteria criteria = new EvaluationCriteria();
                        criteria.setId(rs.getInt("FK_CRIT"));
                        grade.setCriteria(criteria);

                        addToCache(grade);
                    }

                    grades.add(grade);
                }
            }

        } catch (SQLException e) {
            logger.error("SQLException in findByEvaluation(): {}", e.getMessage());
        }

        return grades;
    }

    /**
     * Recherche toutes les notes liées à un critère d’évaluation.
     *
     * @param criteria le critère concerné
     * @return un ensemble de notes liées à ce critère
     */
    public Set<Grade> findByCriteria(EvaluationCriteria criteria) {
        Set<Grade> grades = new HashSet<>();
        String sql = "SELECT * FROM NOTES WHERE FK_CRIT = ?";
        Connection c = ConnectionUtils.getConnection();

        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, criteria.getId());

            try (ResultSet rs = s.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("NUMERO");

                    Grade grade = identityMap.get(id);
                    if (grade == null) {
                        grade = new Grade();
                        grade.setId(id);
                        grade.setGrade(rs.getInt("NOTE"));

                        // 🔹 Chargement minimal de l’évaluation associée
                        int evalId = rs.getInt("FK_COMM");
                        CompleteEvaluation eval = CompleteEvaluationMapper.identityMap.get(evalId);
                        if (eval == null) {
                            eval = new CompleteEvaluation();
                            eval.setId(evalId);
                        }

                        grade.setEvaluation(eval);
                        grade.setCriteria(criteria);
                        addToCache(grade);
                    }

                    grades.add(grade);
                }
            }

        } catch (SQLException e) {
            logger.error("SQLException in findByCriteria(): {}", e.getMessage());
        }

        return grades;
    }
}
