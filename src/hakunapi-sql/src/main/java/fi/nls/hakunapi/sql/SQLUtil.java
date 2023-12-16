package fi.nls.hakunapi.sql;

import fi.nls.hakunapi.core.property.HakunaProperty;

public class SQLUtil {
    
    public static String toSQL(HakunaProperty prop) {
        return toSQL(prop.getTable(), prop.getColumn());
    }
    
    public static String toSQL(String table, String column) {
        StringBuilder sb = new StringBuilder();
        if (table != null && !table.isEmpty()) {
            sb.append('"').append(table).append('"').append('.');
        }
        if (column.contains("(")) {
            sb.append(column);
        } else {
            sb.append('"').append(column).append('"');
        }
        return sb.toString();
    }

}
