package cz.chalda;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

/**
 * Defining naming strategy changes table name based on the system property.
 */
public class TableNamingStrategy extends PhysicalNamingStrategyStandardImpl {
    private static final long serialVersionUID = 1L;

    private static final String tableNameSuffix = System.getProperty("table.name.suffix", "_BADBOY");

    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        return Identifier.toIdentifier(name.getText() + tableNameSuffix);
    }
}
