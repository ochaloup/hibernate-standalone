package org.jboss.openshift;

import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

public class Main {
    public static void main( String[] args ) {
        Properties generateSchemaProperties = new Properties();
        generateSchemaProperties.setProperty("javax.persistence.schema-generation.database.action", "drop-and-create");
        Persistence.generateSchema("hibernate-executor", generateSchemaProperties);

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hibernate-executor");
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        try {
            RecoveryInProgressRecord record = new RecoveryInProgressRecord("franta", "recovery-franta");
            em.persist(record);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("can't persist", e);
        }

        String queryPodname = "franta";
        try {
            System.out.println(Main.getRecord(em, queryPodname));
        } catch (NoResultException nre) {
            System.out.printf("No record for app pod name %s%n", queryPodname);
        }
    }

    public static RecoveryInProgressRecord getRecord(EntityManager em, String applicationPodName) {
        TypedQuery<RecoveryInProgressRecord> query = em.createQuery(
            "SELECT r FROM RecoveryInProgressRecord r WHERE r.id.applicationPodName = :podName", RecoveryInProgressRecord.class);
        return query.setParameter("podName", applicationPodName).getSingleResult();
      } 
}
