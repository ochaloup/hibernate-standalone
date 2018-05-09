package cz.chalda;

import java.util.EnumSet;

import javax.persistence.NoResultException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;

/**
 * Generate schema, persist and query with Hibernate API.
 */
public class Main {
    public static void main( String[] args ) {
        StandardServiceRegistryBuilder standardRegistryBuilder = new StandardServiceRegistryBuilder();
        standardRegistryBuilder
            .applySetting("hibernate.dialect", "org.hibernate.dialect.PostgreSQL94Dialect")
            .applySetting("hibernate.connection.driver_class", "org.postgresql.Driver")
            .applySetting("hibernate.connection.url", "jdbc:postgresql://localhost:5432/test")
            .applySetting("hibernate.connection.username", "test")
            .applySetting("hibernate.connection.password", "test");
        final ServiceRegistry standardRegistry = standardRegistryBuilder.build();

        MetadataSources sources = new MetadataSources(standardRegistry)
                .addAnnotatedClass(PersonNameDictionary.class);
        MetadataBuilder metadataBuilder = sources.getMetadataBuilder();
        metadataBuilder.applyPhysicalNamingStrategy(new PhysicalNamingStrategyStandardImpl() {
            private static final long serialVersionUID = 1L;

            @Override
            public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {
                return Identifier.toIdentifier(name.getCanonicalName() + "_goodguy");
            }
        });
        Metadata metadata = metadataBuilder.build();

        SchemaExport schemaExport = new SchemaExport();
        schemaExport.createOnly( EnumSet.of( TargetType.DATABASE ), metadata);
        System.out.println("exception: " + schemaExport.getExceptions());

        /*
        // https://stackoverflow.com/a/22278250/187035
        if(standardRegistry!= null) {
            StandardServiceRegistryBuilder.destroy(standardRegistry);
        }
        */

        SessionFactory sessionFactory = metadata.buildSessionFactory();
        Session session = sessionFactory.openSession();

        session.getTransaction().begin();
        PersonNameDictionary record = new PersonNameDictionary("franta", "recovery-franta");
        try {
            session.persist(record);
            session.getTransaction().commit();
        } catch (Exception e) {
            if(session.getTransaction() != null && session.getTransaction().getStatus() == TransactionStatus.ACTIVE)
                session.getTransaction().rollback();
            if(session.isOpen()) session.close();
            if(!sessionFactory.isClosed()) sessionFactory.close();
            throw new RuntimeException("can't persist record: [" + record + "]", e);
        }

        String queryPodname = "franta";
        try {
            System.out.printf("query returned: %s%n", Main.getRecord(session, queryPodname));
        } catch (NoResultException nre) {
            System.out.printf("No record for app pod name %s%n", queryPodname);
        } finally {
            session.close();
            sessionFactory.close();
        }
    }   

    public static PersonNameDictionary getRecord(Session session, String applicationPodName) {
        // the Criteria is deprecated in Hibernate 5.2 (see https://github.com/treehouse/giflib-hibernate/commit/f97a2828a466e849d8ae84884b5dce60a66cf412)
        return (PersonNameDictionary) session.createCriteria(PersonNameDictionary.class)
          .add(Restrictions.eq("id.firstName", applicationPodName))
          .uniqueResult();
      } 
}
