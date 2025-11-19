package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.RestaurantType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Mapper pour la gestion des {@link RestaurantType}.
 * Permet la persistance et la récupération des types gastronomiques
 * (ex. : Italien, Japonais, Français), tout en gérant le cache via une Identity Map.
 */
public class RestaurantTypeMapper extends AbstractMapper<RestaurantType> {

    /** Cache local des types de restaurants déjà chargés. */
    protected static final Map<Integer, RestaurantType> identityMap = new HashMap<>();

    @Override
    protected Map<Integer, RestaurantType> getIdentityMap() {
        return identityMap;
    }

    /**
     * Recherche un type de restaurant par son identifiant.
     * Si le type est déjà présent dans le cache, il est renvoyé directement.
     *
     * @param id identifiant unique du type gastronomique
     * @return le type correspondant, ou {@code null} si absent
     */
    @Override
    public RestaurantType findById(int id) {
        if (!isCacheEmpty() && identityMap.containsKey(id)) {
            logger.debug("RestaurantType {} trouvé dans le cache.", id);
            return identityMap.get(id);
        }

        String sql = "SELECT * FROM TYPES_GASTRONOMIQUES WHERE NUMERO = ?";
        Connection c = ConnectionUtils.getConnection();

        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, id);

            try (ResultSet rs = s.executeQuery()) {
                if (!rs.next()) return null;

                RestaurantType type = new RestaurantType();
                type.setId(rs.getInt("NUMERO"));
                type.setLabel(rs.getString("LIBELLE"));
                type.setDescription(rs.getString("DESCRIPTION"));

                addToCache(type);
                logger.debug("RestaurantType {} ajouté au cache.", id);
                return type;
            }

        } catch (SQLException ex) {
            logger.error("SQLException in findById({}): {}", id, ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Récupère tous les types de restaurants depuis la base.
     * Réutilise les instances déjà présentes dans le cache.
     *
     * @return un ensemble de types gastronomiques
     */
    @Override
    public Set<RestaurantType> findAll() {
        Set<RestaurantType> types = new HashSet<>();
        resetCache();

        String sql = "SELECT * FROM TYPES_GASTRONOMIQUES";
        Connection c = ConnectionUtils.getConnection();

        try (PreparedStatement s = c.prepareStatement(sql);
             ResultSet rs = s.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("NUMERO");

                // ✅ Réutilisation du cache si déjà présent
                RestaurantType type = identityMap.get(id);
                if (type == null) {
                    type = new RestaurantType();
                    type.setId(id);
                    type.setLabel(rs.getString("LIBELLE"));
                    type.setDescription(rs.getString("DESCRIPTION"));
                    addToCache(type);
                }

                types.add(type);
            }

            logger.debug("findAll() : {} RestaurantTypes chargés depuis la DB.", types.size());

        } catch (SQLException ex) {
            logger.error("SQLException in findAll(): {}", ex.getMessage(), ex);
        }

        return types;
    }

    /**
     * Crée un nouveau type gastronomique et le persiste dans la base.
     *
     * @param object le type de restaurant à créer
     * @return le type créé et ajouté au cache, ou {@code null} en cas d’erreur
     */
    @Override
    public RestaurantType create(RestaurantType object) {
        Connection c = ConnectionUtils.getConnection();

        try {
            int nextId = getSequenceValue();
            object.setId(nextId);

            String sql = "INSERT INTO TYPES_GASTRONOMIQUES (NUMERO, LIBELLE, DESCRIPTION) VALUES (?, ?, ?)";

            try (PreparedStatement s = c.prepareStatement(sql)) {
                s.setInt(1, object.getId());
                s.setString(2, object.getLabel());
                s.setString(3, object.getDescription());
                s.executeUpdate();
                c.commit();
            }

            addToCache(object);
            logger.debug("RestaurantType {} ajouté au cache après création.", object.getId());
            return object;

        } catch (SQLException e) {
            logger.error("SQLException in create(): {}", e.getMessage());
            return null;
        }
    }

    /**
     * Met à jour un type gastronomique existant dans la base.
     *
     * @param object le type à mettre à jour
     * @return {@code true} si la mise à jour a réussi
     */
    @Override
    public boolean update(RestaurantType object) {
        Connection c = ConnectionUtils.getConnection();
        String sql = """
            UPDATE TYPES_GASTRONOMIQUES
            SET LIBELLE = ?, DESCRIPTION = ?
            WHERE NUMERO = ?
        """;

        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, object.getLabel());
            s.setString(2, object.getDescription());
            s.setInt(3, object.getId());
            s.executeUpdate();
            c.commit();

            addToCache(object);
            logger.debug("RestaurantType {} mis à jour dans le cache.", object.getId());
            return true;

        } catch (SQLException e) {
            logger.error("SQLException in update(): {}", e.getMessage());
            return false;
        }
    }

    /**
     * Supprime un type gastronomique de la base.
     *
     * @param object le type à supprimer
     * @return {@code true} si la suppression a réussi
     */
    @Override
    public boolean delete(RestaurantType object) {
        Connection c = ConnectionUtils.getConnection();
        String sql = "DELETE FROM TYPES_GASTRONOMIQUES WHERE NUMERO = ?";

        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, object.getId());
            s.executeUpdate();
            c.commit();

            removeFromCache(object.getId());
            logger.debug("RestaurantType {} supprimé du cache et de la DB.", object.getId());
            return true;

        } catch (SQLException e) {
            logger.error("SQLException in delete(): {}", e.getMessage());
            return false;
        }
    }

    /**
     * Supprime un type gastronomique à partir de son identifiant.
     *
     * @param id identifiant du type
     * @return {@code true} si la suppression a réussi
     */
    @Override
    public boolean deleteById(int id) {
        RestaurantType type = findById(id);
        if (type == null) return false;
        return delete(type);
    }

    @Override
    protected String getSequenceQuery() {
        return "SELECT SEQ_TYPES_GASTRONOMIQUES.NEXTVAL FROM DUAL";
    }

    @Override
    protected String getExistsQuery() {
        return "SELECT 1 FROM TYPES_GASTRONOMIQUES WHERE NUMERO = ?";
    }

    @Override
    protected String getCountQuery() {
        return "SELECT COUNT(*) FROM TYPES_GASTRONOMIQUES";
    }
}
