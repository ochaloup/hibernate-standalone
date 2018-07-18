package cz.chalda;

import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import org.hibernate.cfg.AvailableSettings;
import org.jboss.logging.Logger;

/**
 * Generating database schema, persisting record and querying it with using JPA API.
 */
public class Main {
    private static final Logger log = Logger.getLogger(Main.class);

    private static final String PERSISTENCE_UNIT_NAME = "person-dictionary";

    public static void main( String[] args ) {
    	// programmatically calling schema generation 
        Properties generateSchemaProperties = new Properties();
        generateSchemaProperties.setProperty(AvailableSettings.HBM2DDL_DATABASE_ACTION, "create");
        Persistence.generateSchema(PERSISTENCE_UNIT_NAME, generateSchemaProperties);

        // getting entity manager for the defined persistence unit
        EntityManagerFactory emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        EntityManager em = emf.createEntityManager();

        // having transaction started and persisting a entity
        em.getTransaction().begin();
        PersonNameDictionary record = new PersonNameDictionary("Bilbo", "Baggins");
        try {
            em.persist(record);
            em.getTransaction().commit();
        } catch (Exception e) {
            if(em.getTransaction() != null && em.getTransaction().isActive())
                em.getTransaction().rollback();
            if(em.isOpen()) em.close();
            if(emf.isOpen()) emf.close();
            throw new RuntimeException("Can't persist record: [" + record + "]", e);
        }

        String personName = "Bilbo";
        try {
            log.infof("query returned: [%s]%n", Main.getDictionaryRecord(em, personName));
        } catch (NoResultException nre) {
            log.errorf(nre, "No surname for person name: %s%n", personName);
        } finally {
            log.debugf("Closing em [%s] and emf [%s]", em, emf);
            if(em.isOpen()) em.close();
            if(emf.isOpen()) emf.close();
        }
    }

    public static PersonNameDictionary getDictionaryRecord(EntityManager em, String firstName) {
        TypedQuery<PersonNameDictionary> query = em.createQuery(
            "SELECT r FROM PersonNameDictionary r WHERE r.id.firstName = :name", PersonNameDictionary.class);
        return query.setParameter("name", firstName).getSingleResult();
      } 
}
