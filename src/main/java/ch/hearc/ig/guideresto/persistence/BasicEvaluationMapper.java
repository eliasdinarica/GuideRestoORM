package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.BasicEvaluation;
import ch.hearc.ig.guideresto.business.Restaurant;

import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Mapper pour la gestion des évaluations basiques (likes/dislikes).
 * Fait le lien entre la couche de persistance (table LIKES)
 * et les objets métier {@link BasicEvaluation}.
 *
 * Utilise une Identity Map pour garantir l’unicité des instances en mémoire.
 */
public class BasicEvaluationMapper extends AbstractMapper<BasicEvaluation> {

    /** Cache local pour les instances déjà chargées (Identity Map). */
    protected static final Map<Integer, BasicEvaluation> identityMap = new HashMap<>();

    @Override
    protected Map<Integer, BasicEvaluation> getIdentityMap() {
        return identityMap;
    }

    /**
     * Recherche une évaluation basique par son identifiant.
     * Si elle est présente dans le cache, elle est retournée directement.
     */
    @Override
    public BasicEvaluation findById(int id) {
        if (!isCacheEmpty() && identityMap.containsKey(id)) {
            logger.debug("BasicEvaluation {} trouvée dans le cache.", id);
            return identityMap.get(id);
        }

        String sql = "SELECT * FROM LIKES WHERE NUMERO = ?";
        Connection c = ConnectionUtils.getConnection();

        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, id);

            try (ResultSet rs = s.executeQuery()) {
                if (!rs.next()) return null;

                BasicEvaluation evaluation = new BasicEvaluation();
                evaluation.setId(rs.getInt("NUMERO"));
                evaluation.setLikeRestaurant("T".equalsIgnoreCase(rs.getString("APPRECIATION")));
                evaluation.setVisitDate(rs.getDate("DATE_EVAL"));
                evaluation.setIpAddress(rs.getString("ADRESSE_IP"));

                // 🔹 Création d’un proxy minimal du restaurant (chargement différé)
                Restaurant restaurant = new Restaurant();
                restaurant.setId(rs.getInt("FK_REST"));
                evaluation.setRestaurant(restaurant);

                addToCache(evaluation);
                logger.debug("BasicEvaluation {} ajoutée au cache.", id);
                return evaluation;
            }

        } catch (SQLException e) {
            logger.error("SQLException in findById({}): {}", id, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Récupère toutes les évaluations basiques présentes dans la base.
     */
    @Override
    public Set<BasicEvaluation> findAll() {
        Set<BasicEvaluation> evaluations = new HashSet<>();
        resetCache();

        String sql = "SELECT * FROM LIKES";
        Connection c = ConnectionUtils.getConnection();

        try (PreparedStatement s = c.prepareStatement(sql);
             ResultSet rs = s.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("NUMERO");

                // ✅ Réutilisation si déjà présent dans l’Identity Map
                BasicEvaluation evaluation = identityMap.get(id);
                if (evaluation == null) {
                    evaluation = new BasicEvaluation();
                    evaluation.setId(id);
                    evaluation.setLikeRestaurant("T".equalsIgnoreCase(rs.getString("APPRECIATION")));
                    evaluation.setVisitDate(rs.getDate("DATE_EVAL"));
                    evaluation.setIpAddress(rs.getString("ADRESSE_IP"));

                    Restaurant restaurant = new Restaurant();
                    restaurant.setId(rs.getInt("FK_REST"));
                    evaluation.setRestaurant(restaurant);

                    addToCache(evaluation);
                }

                evaluations.add(evaluation);
            }

            logger.debug("findAll() : {} BasicEvaluations chargées depuis la DB.", evaluations.size());

        } catch (SQLException e) {
            logger.error("SQLException in findAll(): {}", e.getMessage());
        }

        return evaluations;
    }

    /**
     * Insère une nouvelle évaluation basique dans la base.
     */
    @Override
    public BasicEvaluation create(BasicEvaluation object) {
        Connection c = ConnectionUtils.getConnection();

        try {
            int nextId = getSequenceValue();
            object.setId(nextId);

            String sql = """
                    INSERT INTO LIKES (NUMERO, APPRECIATION, DATE_EVAL, ADRESSE_IP, FK_REST)
                    VALUES (?, ?, ?, ?, ?)
                    """;

            try (PreparedStatement s = c.prepareStatement(sql)) {
                s.setInt(1, object.getId());
                s.setString(2, object.getLikeRestaurant() ? "T" : "F");
                s.setDate(3, new Date(object.getVisitDate().getTime()));
                s.setString(4, object.getIpAddress());
                s.setInt(5, object.getRestaurant().getId());
                s.executeUpdate();
                c.commit();
            }

            addToCache(object);
            logger.debug("BasicEvaluation {} ajoutée au cache après création.", object.getId());
            return object;

        } catch (SQLException e) {
            logger.error("SQLException in create(): {}", e.getMessage());
            return null;
        }
    }

    /**
     * Met à jour une évaluation basique existante.
     */
    @Override
    public boolean update(BasicEvaluation object) {
        Connection c = ConnectionUtils.getConnection();
        String sql = """
                UPDATE LIKES
                SET APPRECIATION = ?, DATE_EVAL = ?, ADRESSE_IP = ?, FK_REST = ?
                WHERE NUMERO = ?
                """;

        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, object.getLikeRestaurant() ? "T" : "F");
            s.setDate(2, new Date(object.getVisitDate().getTime()));
            s.setString(3, object.getIpAddress());
            s.setInt(4, object.getRestaurant().getId());
            s.setInt(5, object.getId());

            s.executeUpdate();
            c.commit();

            addToCache(object);
            logger.debug("BasicEvaluation {} mise à jour dans le cache.", object.getId());
            return true;

        } catch (SQLException e) {
            logger.error("SQLException in update(): {}", e.getMessage());
            return false;
        }
    }

    /**
     * Supprime une évaluation basique.
     */
    @Override
    public boolean delete(BasicEvaluation object) {
        Connection c = ConnectionUtils.getConnection();
        String sql = "DELETE FROM LIKES WHERE NUMERO = ?";

        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, object.getId());
            s.executeUpdate();
            c.commit();

            removeFromCache(object.getId());
            logger.debug("BasicEvaluation {} supprimée du cache et de la DB.", object.getId());
            return true;

        } catch (SQLException e) {
            logger.error("SQLException in delete(): {}", e.getMessage());
            return false;
        }
    }

    /**
     * Supprime une évaluation basique à partir de son identifiant.
     */
    @Override
    public boolean deleteById(int id) {
        BasicEvaluation eval = findById(id);
        if (eval == null) return false;
        return delete(eval);
    }

    @Override
    protected String getSequenceQuery() {
        return "SELECT SEQ_EVAL.NEXTVAL FROM DUAL";
    }

    @Override
    protected String getExistsQuery() {
        return "SELECT 1 FROM LIKES WHERE NUMERO = ?";
    }

    @Override
    protected String getCountQuery() {
        return "SELECT COUNT(*) FROM LIKES";
    }

    /**
     * Récupère toutes les évaluations basiques d’un restaurant donné.
     */
    public Set<BasicEvaluation> findByRestaurant(Restaurant restaurant) {
        Set<BasicEvaluation> evaluations = new HashSet<>();
        String sql = "SELECT * FROM LIKES WHERE FK_REST = ?";
        Connection c = ConnectionUtils.getConnection();

        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, restaurant.getId());

            try (ResultSet rs = s.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("NUMERO");

                    BasicEvaluation evaluation = identityMap.get(id);
                    if (evaluation == null) {
                        evaluation = new BasicEvaluation();
                        evaluation.setId(id);
                        evaluation.setLikeRestaurant("T".equalsIgnoreCase(rs.getString("APPRECIATION")));
                        evaluation.setVisitDate(rs.getDate("DATE_EVAL"));
                        evaluation.setIpAddress(rs.getString("ADRESSE_IP"));
                        evaluation.setRestaurant(restaurant);
                        addToCache(evaluation);
                    }

                    evaluations.add(evaluation);
                }
            }

        } catch (SQLException e) {
            logger.error("SQLException in findByRestaurant(): {}", e.getMessage());
        }

        return evaluations;
    }
}
