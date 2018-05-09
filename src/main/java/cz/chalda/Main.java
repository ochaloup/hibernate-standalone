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

    private static final String PERSISTENCE_UNIT_NAME = "hibernate-executor";

    public static void main( String[] args ) {
        Properties generateSchemaProperties = new Properties();
        generateSchemaProperties.setProperty(AvailableSettings.HBM2DDL_DATABASE_ACTION, "create");
        Persistence.generateSchema(PERSISTENCE_UNIT_NAME, generateSchemaProperties);

        EntityManagerFactory emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        PersonNameDictionary record = new PersonNameDictionary("franta", "recovery-franta");
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

        String queryPodname = "franta";
        try {
            log.infof("query returned: [%s]%n", Main.getRecord(em, queryPodname));
        } catch (NoResultException nre) {
            log.errorf(nre, "No record for app pod name: %s%n", queryPodname);
        } finally {
            log.debugf("Closing em [%s] and emf [%s]", em, emf);
            if(em.isOpen()) em.close();
            if(emf.isOpen()) emf.close();
        }
    }

    public static PersonNameDictionary getRecord(EntityManager em, String firstName) {
        TypedQuery<PersonNameDictionary> query = em.createQuery(
            "SELECT r FROM PersonNameDictionary r WHERE r.id.firstName = :name", PersonNameDictionary.class);
        return query.setParameter("name", firstName).getSingleResult();
      } 
}
