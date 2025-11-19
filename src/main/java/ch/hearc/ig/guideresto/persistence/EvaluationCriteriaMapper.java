package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.EvaluationCriteria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Mapper pour la gestion des {@link EvaluationCriteria}.
 * Assure la correspondance entre la table CRITERES_EVALUATION
 * et les objets métier, avec une gestion du cache (Identity Map).
 */
public class EvaluationCriteriaMapper extends AbstractMapper<EvaluationCriteria> {

    /** Cache local des critères déjà chargés (Identity Map). */
    protected static final Map<Integer, EvaluationCriteria> identityMap = new HashMap<>();
    @Override
    protected Map<Integer, EvaluationCriteria> getIdentityMap() {
        return identityMap;
    }

    /**
     * Recherche un critère d’évaluation par son identifiant.
     * Si le critère est déjà présent dans le cache, il est retourné directement.
     *
     * @param id identifiant unique du critère
     * @return le critère correspondant, ou {@code null} si absent
     */
    @Override
    public EvaluationCriteria findById(int id) {
        if (!isCacheEmpty() && identityMap.containsKey(id)) {
            logger.debug("EvaluationCriteria {} trouvé dans le cache.", id);
            return identityMap.get(id);
        }

        String sql = "SELECT * FROM CRITERES_EVALUATION WHERE NUMERO = ?";
        Connection c = ConnectionUtils.getConnection();

        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, id);

            try (ResultSet rs = s.executeQuery()) {
                if (!rs.next()) return null;

                EvaluationCriteria criteria = new EvaluationCriteria();
                criteria.setId(rs.getInt("NUMERO"));
                criteria.setName(rs.getString("NOM"));
                criteria.setDescription(rs.getString("DESCRIPTION"));

                addToCache(criteria);
                logger.debug("EvaluationCriteria {} ajouté au cache.", id);
                return criteria;
            }

        } catch (SQLException ex) {
            logger.error("SQLException in findById({}): {}", id, ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Récupère tous les critères d’évaluation existants.
     * Réutilise les instances déjà présentes dans l’Identity Map.
     *
     * @return un ensemble de critères d’évaluation
     */
    @Override
    public Set<EvaluationCriteria> findAll() {
        Set<EvaluationCriteria> criterias = new HashSet<>();
        resetCache();

        String sql = "SELECT * FROM CRITERES_EVALUATION";
        Connection c = ConnectionUtils.getConnection();

        try (PreparedStatement s = c.prepareStatement(sql);
             ResultSet rs = s.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("NUMERO");

                // ✅ Réutilisation du cache si possible
                EvaluationCriteria criteria = identityMap.get(id);
                if (criteria == null) {
                    criteria = new EvaluationCriteria();
                    criteria.setId(id);
                    criteria.setName(rs.getString("NOM"));
                    criteria.setDescription(rs.getString("DESCRIPTION"));
                    addToCache(criteria);
                }

                criterias.add(criteria);
            }

            logger.debug("findAll() : {} EvaluationCriteria chargés depuis la DB.", criterias.size());

        } catch (SQLException ex) {
            logger.error("SQLException in findAll(): {}", ex.getMessage(), ex);
        }

        return criterias;
    }

    /**
     * Crée un nouveau critère d’évaluation dans la base.
     *
     * @param object le critère à persister
     * @return le critère créé et ajouté au cache, ou {@code null} en cas d’erreur
     */
    @Override
    public EvaluationCriteria create(EvaluationCriteria object) {
        Connection c = ConnectionUtils.getConnection();

        try {
            int nextId = getSequenceValue();
            object.setId(nextId);

            String sql = "INSERT INTO CRITERES_EVALUATION (NUMERO, NOM, DESCRIPTION) VALUES (?, ?, ?)";

            try (PreparedStatement s = c.prepareStatement(sql)) {
                s.setInt(1, object.getId());
                s.setString(2, object.getName());
                s.setString(3, object.getDescription());
                s.executeUpdate();
                c.commit();
            }

            addToCache(object);
            logger.debug("EvaluationCriteria {} ajouté au cache après création.", object.getId());
            return object;

        } catch (SQLException e) {
            logger.error("SQLException in create(): {}", e.getMessage());
            return null;
        }
    }

    /**
     * Met à jour un critère d’évaluation existant.
     *
     * @param object le critère à mettre à jour
     * @return {@code true} si la mise à jour a réussi, {@code false} sinon
     */
    @Override
    public boolean update(EvaluationCriteria object) {
        Connection c = ConnectionUtils.getConnection();
        String sql = """
                UPDATE CRITERES_EVALUATION
                SET NOM = ?, DESCRIPTION = ?
                WHERE NUMERO = ?
                """;

        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, object.getName());
            s.setString(2, object.getDescription());
            s.setInt(3, object.getId());
            s.executeUpdate();
            c.commit();

            addToCache(object);
            logger.debug("EvaluationCriteria {} mis à jour dans le cache.", object.getId());
            return true;

        } catch (SQLException e) {
            logger.error("SQLException in update(): {}", e.getMessage());
            return false;
        }
    }

    /**
     * Supprime un critère d’évaluation de la base.
     *
     * @param object le critère à supprimer
     * @return {@code true} si la suppression a réussi
     */
    @Override
    public boolean delete(EvaluationCriteria object) {
        Connection c = ConnectionUtils.getConnection();
        String sql = "DELETE FROM CRITERES_EVALUATION WHERE NUMERO = ?";

        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, object.getId());
            s.executeUpdate();
            c.commit();

            removeFromCache(object.getId());
            logger.debug("EvaluationCriteria {} supprimé du cache et de la DB.", object.getId());
            return true;

        } catch (SQLException e) {
            logger.error("SQLException in delete(): {}", e.getMessage());
            return false;
        }
    }

    /**
     * Supprime un critère d’évaluation à partir de son identifiant.
     *
     * @param id identifiant du critère
     * @return {@code true} si la suppression a réussi
     */
    @Override
    public boolean deleteById(int id) {
        EvaluationCriteria crit = findById(id);
        if (crit == null) return false;
        return delete(crit);
    }

    @Override
    protected String getSequenceQuery() {
        return "SELECT SEQ_CRITERES_EVALUATION.NEXTVAL FROM DUAL";
    }

    @Override
    protected String getExistsQuery() {
        return "SELECT 1 FROM CRITERES_EVALUATION WHERE NUMERO = ?";
    }

    @Override
    protected String getCountQuery() {
        return "SELECT COUNT(*) FROM CRITERES_EVALUATION";
    }
}
