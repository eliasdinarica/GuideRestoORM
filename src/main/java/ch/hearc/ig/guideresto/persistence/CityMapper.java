package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.City;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Mapper pour la gestion des entités {@link City}.
 * Assure la correspondance entre la table VILLES et les objets métier.
 *
 * Utilise une Identity Map pour éviter la duplication d’instances en mémoire.
 */
public class CityMapper extends AbstractMapper<City> {

    /** Cache local des instances chargées (Identity Map). */
    protected static final Map<Integer, City> identityMap = new HashMap<>();

    @Override
    protected Map<Integer, City> getIdentityMap() {
        return identityMap;
    }

    /**
     * Recherche une ville par son identifiant.
     * Si elle est déjà en cache, elle est retournée directement.
     *
     * @param id identifiant unique de la ville
     * @return la ville correspondante, ou {@code null} si absente
     */
    @Override
    public City findById(int id) {
        if (!isCacheEmpty() && identityMap.containsKey(id)) {
            logger.debug("City {} trouvée dans le cache.", id);
            return identityMap.get(id);
        }

        String sql = "SELECT * FROM VILLES WHERE NUMERO = ?";
        Connection c = ConnectionUtils.getConnection();

        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, id);

            try (ResultSet rs = s.executeQuery()) {
                if (!rs.next()) return null;

                City city = new City();
                city.setId(rs.getInt("NUMERO"));
                city.setZipCode(rs.getString("CODE_POSTAL"));
                city.setCityName(rs.getString("NOM_VILLE"));

                addToCache(city);
                logger.debug("City {} ajoutée au cache.", id);
                return city;
            }

        } catch (SQLException e) {
            logger.error("SQLException in findById({}): {}", id, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Récupère toutes les villes présentes dans la base.
     *
     * @return un ensemble de toutes les villes connues
     */
    @Override
    public Set<City> findAll() {
        Set<City> cities = new HashSet<>();
        resetCache();

        String sql = "SELECT * FROM VILLES";
        Connection c = ConnectionUtils.getConnection();

        try (PreparedStatement s = c.prepareStatement(sql);
             ResultSet rs = s.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("NUMERO");

                // ✅ Réutilisation de l’instance si déjà présente dans le cache
                City city = identityMap.get(id);
                if (city == null) {
                    city = new City();
                    city.setId(id);
                    city.setZipCode(rs.getString("CODE_POSTAL"));
                    city.setCityName(rs.getString("NOM_VILLE"));
                    addToCache(city);
                }

                cities.add(city);
            }

            logger.debug("findAll() : {} villes chargées depuis la DB.", cities.size());

        } catch (SQLException e) {
            logger.error("SQLException in findAll(): {}", e.getMessage());
        }

        return cities;
    }

    /**
     * Crée une nouvelle ville dans la base.
     *
     * @param object l’objet {@link City} à insérer
     * @return la ville créée et persistée, ou {@code null} en cas d’erreur
     */
    @Override
    public City create(City object) {
        Connection c = ConnectionUtils.getConnection();

        try {
            int nextId = getSequenceValue();
            object.setId(nextId);

            String sql = "INSERT INTO VILLES (NUMERO, CODE_POSTAL, NOM_VILLE) VALUES (?, ?, ?)";

            try (PreparedStatement s = c.prepareStatement(sql)) {
                s.setInt(1, object.getId());
                s.setString(2, object.getZipCode());
                s.setString(3, object.getCityName());

                s.executeUpdate();
                c.commit();
            }

            addToCache(object);
            logger.debug("City {} ajoutée au cache après création.", object.getId());
            return object;

        } catch (SQLException e) {
            logger.error("SQLException in create(): {}", e.getMessage());
            return null;
        }
    }

    /**
     * Met à jour une ville existante.
     *
     * @param object la ville à mettre à jour
     * @return {@code true} si la mise à jour a réussi, {@code false} sinon
     */
    @Override
    public boolean update(City object) {
        Connection c = ConnectionUtils.getConnection();
        String sql = "UPDATE VILLES SET CODE_POSTAL = ?, NOM_VILLE = ? WHERE NUMERO = ?";

        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, object.getZipCode());
            s.setString(2, object.getCityName());
            s.setInt(3, object.getId());

            s.executeUpdate();
            c.commit();

            addToCache(object);
            logger.debug("City {} mise à jour dans le cache.", object.getId());
            return true;

        } catch (SQLException e) {
            logger.error("SQLException in update(): {}", e.getMessage());
            return false;
        }
    }

    /**
     * Supprime une ville de la base.
     *
     * @param object la ville à supprimer
     * @return {@code true} si la suppression a réussi
     */
    @Override
    public boolean delete(City object) {
        Connection c = ConnectionUtils.getConnection();
        String sql = "DELETE FROM VILLES WHERE NUMERO = ?";

        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, object.getId());
            s.executeUpdate();
            c.commit();

            removeFromCache(object.getId());
            logger.debug("City {} supprimée du cache et de la DB.", object.getId());
            return true;

        } catch (SQLException e) {
            logger.error("SQLException in delete(): {}", e.getMessage());
            return false;
        }
    }

    /**
     * Supprime une ville à partir de son identifiant.
     *
     * @param id identifiant unique de la ville
     * @return {@code true} si la suppression a réussi
     */
    @Override
    public boolean deleteById(int id) {
        City city = findById(id);
        if (city == null) return false;
        return delete(city);
    }

    @Override
    protected String getSequenceQuery() {
        return "SELECT SEQ_VILLES.NEXTVAL FROM DUAL";
    }

    @Override
    protected String getExistsQuery() {
        return "SELECT 1 FROM VILLES WHERE NUMERO = ?";
    }

    @Override
    protected String getCountQuery() {
        return "SELECT COUNT(*) FROM VILLES";
    }
}
