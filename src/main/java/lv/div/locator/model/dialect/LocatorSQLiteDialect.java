package lv.div.locator.model.dialect;

import java.sql.Types;

/**
 * SQLite dialect supporting NULL values
 */
public class LocatorSQLiteDialect extends org.hibernate.dialect.SQLiteDialect {

    public LocatorSQLiteDialect() {
        super();
        registerColumnType(Types.NULL, "null");
        registerHibernateType(Types.NULL, "null");
    }
}
