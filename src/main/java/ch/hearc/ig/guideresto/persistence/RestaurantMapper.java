package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.Restaurant;
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
 * Mapper pour la gestion des {@link Restaurant}.
 * Gère la persistance et la récupération des restaurants depuis la base,
 * tout en assurant la cohérence des instances via une Identity Map.
 */
public class RestaurantMapper extends AbstractMapper<Restaurant> {

    /** Cache local des restaurants déjà chargés. */
    protected static final Map<Integer, Restaurant> identityMap = new HashMap<>();
    @Override
    protected Map<Integer, Restaurant> getIdentityMap() {
        return identityMap;
    }

    /**
     * Recherche un restaurant par son identifiant.
     * Si le restaurant est déjà en cache, il est retourné directement.
     *
     * @param id identifiant unique du restaurant
     * @return le restaurant trouvé ou {@code null} si absent
     */
    @Override
    public Restaurant findById(int id) {
        if (!isCacheEmpty() && identityMap.containsKey(id)) {
            logger.debug("Restaurant {} trouvé dans le cache.", id);
            return identityMap.get(id);
        }

        String sql = """
            SELECT r.numero          AS r_id,
                   r.nom             AS r_nom,
                   r.description     AS r_desc,
                   r.site_web        AS r_site,
                   r.adresse         AS r_street,
                   v.numero          AS v_id,
                   v.nom_ville       AS v_name,
                   v.code_postal     AS v_zip,
                   t.numero          AS t_id,
                   t.libelle         AS t_label,
                   t.description     AS t_desc
            FROM   restaurants r
            JOIN   villes v ON v.numero = r.fk_vill
            JOIN   types_gastronomiques t ON t.numero = r.fk_type
            WHERE  r.numero = ?
        """;

        Connection connection = ConnectionUtils.getConnection();
        CityMapper cityMapper = new CityMapper();
        RestaurantTypeMapper typeMapper = new RestaurantTypeMapper();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;

                // ✅ Réutilisation de City via l’Identity Map
                int cityId = rs.getInt("v_id");
                City city = cityMapper.findById(cityId);
                if (city == null) {
                    city = new City(
                            cityId,
                            rs.getString("v_name"),
                            rs.getString("v_zip")
                    );
                    cityMapper.addToCache(city);
                }

                // ✅ Réutilisation de RestaurantType via son cache
                int typeId = rs.getInt("t_id");
                RestaurantType type = typeMapper.findById(typeId);
                if (type == null) {
                    type = new RestaurantType(
                            typeId,
                            rs.getString("t_label"),
                            rs.getString("t_desc")
                    );
                    typeMapper.addToCache(type);
                }

                // ✅ Construction du restaurant complet
                Restaurant rest = new Restaurant(
                        rs.getInt("r_id"),
                        rs.getString("r_nom"),
                        rs.getString("r_desc"),
                        rs.getString("r_site"),
                        rs.getString("r_street"),
                        city,
                        type
                );

                addToCache(rest);
                logger.debug("Restaurant {} ajouté au cache.", id);
                return rest;
            }

        } catch (SQLException ex) {
            logger.error("SQLException in findById({}): {}", id, ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Récupère tous les restaurants de la base avec leurs villes et types associés.
     * Les instances de villes et de types sont partagées via leurs Identity Maps respectives.
     *
     * @return un ensemble de restaurants
     */
    @Override
    public Set<Restaurant> findAll() {
        Set<Restaurant> restaurants = new HashSet<>();
        resetCache();

        String sql = """
            SELECT r.numero          AS r_id,
                   r.nom             AS r_nom,
                   r.description     AS r_desc,
                   r.site_web        AS r_site,
                   r.adresse         AS r_street,
                   v.numero          AS v_id,
                   v.nom_ville       AS v_name,
                   v.code_postal     AS v_zip,
                   t.numero          AS t_id,
                   t.libelle         AS t_label,
                   t.description     AS t_desc
            FROM   restaurants r
            JOIN   villes v ON v.numero = r.fk_vill
            JOIN   types_gastronomiques t ON t.numero = r.fk_type
        """;

        Connection connection = ConnectionUtils.getConnection();
        CityMapper cityMapper = new CityMapper();
        RestaurantTypeMapper typeMapper = new RestaurantTypeMapper();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // ✅ Réutilisation des entités liées
                int cityId = rs.getInt("v_id");
                City city = cityMapper.findById(cityId);
                if (city == null) {
                    city = new City(cityId, rs.getString("v_name"), rs.getString("v_zip"));
                    cityMapper.addToCache(city);
                }

                int typeId = rs.getInt("t_id");
                RestaurantType type = typeMapper.findById(typeId);
                if (type == null) {
                    type = new RestaurantType(typeId, rs.getString("t_label"), rs.getString("t_desc"));
                    typeMapper.addToCache(type);
                }

                // ✅ Création du restaurant
                Restaurant restaurant = new Restaurant(
                        rs.getInt("r_id"),
                        rs.getString("r_nom"),
                        rs.getString("r_desc"),
                        rs.getString("r_site"),
                        rs.getString("r_street"),
                        city,
                        type
                );

                addToCache(restaurant);
                restaurants.add(restaurant);
            }

            logger.debug("findAll() : {} restaurants chargés depuis la DB.", restaurants.size());

        } catch (SQLException ex) {
            logger.error("SQLException in findAll(): {}", ex.getMessage(), ex);
        }

        return restaurants;
    }

    /**
     * Crée un nouveau restaurant dans la base.
     *
     * @param object le restaurant à persister
     * @return le restaurant créé et ajouté au cache, ou {@code null} en cas d’erreur
     */
    @Override
    public Restaurant create(Restaurant object) {
        Connection c = ConnectionUtils.getConnection();

        try {
            int nextId = getSequenceValue();
            object.setId(nextId);

            String sql = """
                INSERT INTO RESTAURANTS (NUMERO, NOM, ADRESSE, DESCRIPTION, SITE_WEB, FK_TYPE, FK_VILL)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

            try (PreparedStatement s = c.prepareStatement(sql)) {
                s.setInt(1, object.getId());
                s.setString(2, object.getName());
                s.setString(3, object.getAddress().getStreet());
                s.setString(4, object.getDescription());
                s.setString(5, object.getWebsite());
                s.setInt(6, object.getType().getId());
                s.setInt(7, object.getAddress().getCity().getId());
                s.executeUpdate();
                c.commit();
            }

            addToCache(object);
            logger.debug("Restaurant {} ajouté au cache après création.", object.getId());
            return object;

        } catch (SQLException e) {
            logger.error("SQLException in create(): {}", e.getMessage());
            return null;
        }
    }

    /**
     * Met à jour un restaurant existant dans la base.
     *
     * @param object le restaurant à mettre à jour
     * @return {@code true} si la mise à jour a réussi
     */
    @Override
    public boolean update(Restaurant object) {
        Connection c = ConnectionUtils.getConnection();
        String sql = """
            UPDATE RESTAURANTS
            SET NOM = ?, ADRESSE = ?, DESCRIPTION = ?, SITE_WEB = ?, FK_TYPE = ?, FK_VILL = ?
            WHERE NUMERO = ?
        """;

        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, object.getName());
            s.setString(2, object.getAddress().getStreet());
            s.setString(3, object.getDescription());
            s.setString(4, object.getWebsite());
            s.setInt(5, object.getType().getId());
            s.setInt(6, object.getAddress().getCity().getId());
            s.setInt(7, object.getId());
            s.executeUpdate();
            c.commit();

            addToCache(object);
            logger.debug("Restaurant {} mis à jour dans le cache.", object.getId());
            return true;

        } catch (SQLException e) {
            logger.error("SQLException in update(): {}", e.getMessage());
            return false;
        }
    }

    /**
     * Supprime un restaurant de la base.
     *
     * @param object le restaurant à supprimer
     * @return {@code true} si la suppression a réussi
     */
    @Override
    public boolean delete(Restaurant object) {
        Connection c = ConnectionUtils.getConnection();
        String sql = "DELETE FROM RESTAURANTS WHERE NUMERO = ?";

        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, object.getId());
            s.executeUpdate();
            c.commit();

            removeFromCache(object.getId());
            logger.debug("Restaurant {} supprimé du cache et de la DB.", object.getId());
            return true;

        } catch (SQLException ex) {
            logger.error("SQLException in delete(): {}", ex.getMessage());
            return false;
        }
    }

    /**
     * Supprime un restaurant à partir de son identifiant.
     *
     * @param id identifiant du restaurant
     * @return {@code true} si la suppression a réussi
     */
    @Override
    public boolean deleteById(int id) {
        Restaurant rest = findById(id);
        if (rest == null) return false;
        return delete(rest);
    }

    @Override
    protected String getSequenceQuery() {
        return "SELECT SEQ_RESTAURANTS.NEXTVAL FROM DUAL";
    }

    @Override
    protected String getExistsQuery() {
        return "SELECT 1 FROM RESTAURANTS WHERE NUMERO = ?";
    }

    @Override
    protected String getCountQuery() {
        return "SELECT COUNT(*) FROM RESTAURANTS";
    }
}
