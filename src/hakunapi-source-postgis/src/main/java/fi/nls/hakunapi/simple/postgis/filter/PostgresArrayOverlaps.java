package fi.nls.hakunapi.simple.postgis.filter;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyArray;

public class PostgresArrayOverlaps implements SQLFilter {

    @Override
    public String toSQL(Filter filter) {
        HakunaProperty prop = filter.getProp();
        return String.format("\"%s\".\"%s\" && ?", prop.getTable(), prop.getColumn());
    }

    @Override
    public int bind(Filter filter, Connection c, PreparedStatement ps, int i) throws SQLException {
        HakunaPropertyArray prop = (HakunaPropertyArray) filter.getProp();
        List<Object> arr = (List<Object>) filter.getValue();
        Array array = toSQLArray(c, prop, arr);
        ps.setArray(i++, array);
        return i;
    }

    private Array toSQLArray(Connection c, HakunaPropertyArray prop, List<Object> arr) throws SQLException {
        switch (prop.getComponentType()) {
        case BOOLEAN:
            return c.createArrayOf("bool", arr.toArray());
        case INT:
            return c.createArrayOf("int4", arr.toArray());
        case LONG:
            return c.createArrayOf("int8", arr.toArray());
        case FLOAT:
            return c.createArrayOf("float4", arr.toArray());
        case DOUBLE:
            return c.createArrayOf("float8", arr.toArray());
        case STRING:
            return c.createArrayOf("text", arr.toArray());
        default:
            throw new IllegalArgumentException("No handler for array of type " + prop.getComponentType());
        }
    }

}
