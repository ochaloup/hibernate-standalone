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
    	// definition of database connection
        StandardServiceRegistryBuilder standardRegistryBuilder = new StandardServiceRegistryBuilder();
        standardRegistryBuilder
            .applySetting("hibernate.dialect", "org.hibernate.dialect.PostgreSQL94Dialect")
            .applySetting("hibernate.connection.driver_class", "org.postgresql.Driver")
            .applySetting("hibernate.connection.url", "jdbc:postgresql://localhost:5432/test")
            .applySetting("hibernate.connection.username", "test")
            .applySetting("hibernate.connection.password", "test");
        final ServiceRegistry standardRegistry = standardRegistryBuilder.build();

        // let's define what's the class to be scanned as entity
        MetadataSources sources = new MetadataSources(standardRegistry)
                .addAnnotatedClass(PersonNameDictionary.class);
        MetadataBuilder metadataBuilder = sources.getMetadataBuilder();
        // MetadataBuilder gives us chance to change database table which will be created for the entity 
        metadataBuilder.applyPhysicalNamingStrategy(new PhysicalNamingStrategyStandardImpl() {
            private static final long serialVersionUID = 1L;

            @Override
            public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {
                return Identifier.toIdentifier(name.getCanonicalName() + "_goodguy");
            }
        });
        Metadata metadata = metadataBuilder.build();

        // based on the metadata we are creating the database schema
        SchemaExport schemaExport = new SchemaExport();
        schemaExport.createOnly( EnumSet.of( TargetType.DATABASE ), metadata);
        if(schemaExport.getExceptions() != null && !schemaExport.getExceptions().isEmpty()) {
        	System.err.println("[ERROR] exception during schema creation: " + schemaExport.getExceptions());
        }

        /*
        // during working on the example I hit an issue on the program is not smoothly stopped
        // as there was hanging the registry. This was workaround to get the Main stopped.
        // for some reason it's not needed anymore for my test app now
        // https://stackoverflow.com/a/22278250/187035
        if(standardRegistry!= null) {
            StandardServiceRegistryBuilder.destroy(standardRegistry);
        }
        */

        // using hibernate API to get session (entity manager in JPA words)
        SessionFactory sessionFactory = metadata.buildSessionFactory();
        Session session = sessionFactory.openSession();

        String personName = "Bilbo";

        // starting transaction and persisting the person entity
        session.getTransaction().begin();
        PersonNameDictionary record = new PersonNameDictionary(personName, "Baggins");
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

        // querying what was just saved in the database
        try {
            System.out.printf("query returned: %s%n", Main.getPersonRecord(session, personName));
        } catch (NoResultException nre) {
            System.out.printf("No record for the person name '%s' found%n", personName);
        } finally {
            session.close();
            sessionFactory.close();
        }
    }   

    public static PersonNameDictionary getPersonRecord(Session session, String applicationPodName) {
        // the Criteria is deprecated in Hibernate 5.2 (see https://github.com/treehouse/giflib-hibernate/commit/f97a2828a466e849d8ae84884b5dce60a66cf412)
        return (PersonNameDictionary) session.createCriteria(PersonNameDictionary.class)
          .add(Restrictions.eq("id.firstName", applicationPodName))
          .uniqueResult();
      } 
}
