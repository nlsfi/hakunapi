package fi.nls.hakunapi.gpkg.extension;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public abstract class GPKGExtension {

    protected final String tableName;
    protected final String columnName;
    protected final String extensionName;
    protected final String definition;
    protected final String scope;

    public GPKGExtension(String extensionName, String definition, String scope) {
        this(null, null, extensionName, definition, scope);
    }

    public GPKGExtension(String tableName, String columnName, String extensionName, String definition, String scope) {
        this.tableName = tableName;
        this.columnName = columnName;
        this.extensionName = Objects.requireNonNull(extensionName);
        this.definition = Objects.requireNonNull(definition);
        this.scope = Objects.requireNonNull(scope);
    }

    public String getTableName() {
        return tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getExtensionName() {
        return extensionName;
    }

    public String getDefinition() {
        return definition;
    }

    public String getScope() {
        return scope;
    }

    public abstract void createExtension(Connection c) throws SQLException;

}
