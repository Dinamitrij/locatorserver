package lv.div.locator.dao;

import lv.div.locator.utils.LocatorServiceException;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Stateless
public class GenericDao {

    @PersistenceContext(unitName = "persistenceUnit")
    protected EntityManager entityManager;

    /**
     * Logger.
     */
    protected static final Logger log = Logger.getLogger(GenericDao.class.getName());

    /**
     * Returns entity manager.
     *
     * @return entity manager
     */
    protected EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * Finds object by the given identifier.
     *
     * @param <T>         object type
     * @param objectClass object class
     * @param objectId    object identifier
     *
     * @return object
     */
    public <T> T findById(Class<T> objectClass, Object objectId) {
        try {
            return getEntityManager().find(objectClass, objectId);
        } catch (PersistenceException pe) {
            throw new LocatorServiceException("Can't find " + objectClass + " by ID = " + objectId + ".", pe);
        }
    }

    /**
     * Finds all objects of the given type.
     *
     * @param <T>         objects type
     * @param objectClass objects class
     *
     * @return objects
     */
    public <T> List<T> findAll(Class<T> objectClass) {
        TypedQuery<T> query =
            entityManager.createQuery("SELECT t FROM " + objectClass.getSimpleName() + " t", objectClass);
        return query.getResultList();
    }

    /**
     * Saves object in database.
     *
     * @param <T>    object type
     * @param object object to save
     */
    public <T> void save(T object) {
        try {
            getEntityManager().persist(object);
            getEntityManager().flush();
        } catch (PersistenceException pe) {
            log.severe("Exception in save() method");
            throw new LocatorServiceException("Can't save object " + object, pe);
        }
    }

    /**
     * Saves objects in database.
     *
     * @param <T>     objects type
     * @param objects objects to save
     */
    public <T> void save(List<T> objects) {
        if (objects == null || objects.isEmpty()) {
            return;
        }

        try {
            for (T object : objects) {
                getEntityManager().persist(object);
            }
            getEntityManager().flush();
        } catch (PersistenceException pe) {
            throw new LocatorServiceException("Can't save objects.", pe);
        }
    }

    /**
     * Updates object in database.
     *
     * @param <T>    object type
     * @param object object to update
     *
     * @return updated object
     */
    public <T> T update(T object) {
        try {
            T mergedObject = getEntityManager().merge(object);
            getEntityManager().flush();

            return mergedObject;
        } catch (PersistenceException pe) {
            throw new LocatorServiceException("Can't update object " + object, pe);
        }
    }

    /**
     * Updates objects in database.
     *
     * @param <T>     objects type
     * @param objects objects to update
     *
     * @return updated objects
     */
    public <T> List<T> update(List<T> objects) {
        if (objects == null || objects.isEmpty()) {
            return objects;
        }

        List<T> mergedObjects = new ArrayList<T>(objects.size());

        try {
            for (T object : objects) {
                T mergedObject = getEntityManager().merge(object);
                mergedObjects.add(mergedObject);
            }
            getEntityManager().flush();

            return mergedObjects;
        } catch (PersistenceException pe) {
            throw new LocatorServiceException("Can't update objects.", pe);
        }
    }

    /**
     * Removes object in database.
     *
     * @param <T>    object type
     * @param object object to remove
     */
    public <T> void delete(T object) {
        try {
            T mergedObject = getEntityManager().merge(object);
            getEntityManager().remove(mergedObject);
            getEntityManager().flush();
        } catch (PersistenceException pe) {
            throw new LocatorServiceException("Can't delete object " + object, pe);
        }
    }

    /**
     * Removes objects in database.
     *
     * @param <T>     objects type
     * @param objects objects to remove
     */
    public <T> void delete(List<T> objects) {
        if (objects == null || objects.isEmpty()) {
            return;
        }

        try {
            for (T object : objects) {
                T mergedObject = getEntityManager().merge(object);
                getEntityManager().remove(mergedObject);
            }
            getEntityManager().flush();
        } catch (PersistenceException pe) {
            throw new LocatorServiceException("Can't delete objects.", pe);
        }
    }

}
