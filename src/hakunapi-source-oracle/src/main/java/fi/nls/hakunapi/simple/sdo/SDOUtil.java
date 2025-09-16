package fi.nls.hakunapi.simple.sdo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.nls.hakunapi.core.OrderBy;
import fi.nls.hakunapi.core.QueryContext;
import fi.nls.hakunapi.core.ValueMapper;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.filter.FilterOp;
import fi.nls.hakunapi.core.join.Join;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyComposite;
import fi.nls.hakunapi.core.util.StringPair;
import fi.nls.hakunapi.simple.sdo.filter.SDOContains;
import fi.nls.hakunapi.simple.sdo.filter.SDOCrosses;
import fi.nls.hakunapi.simple.sdo.filter.SDODisjoint;
import fi.nls.hakunapi.simple.sdo.filter.SDOEquals;
import fi.nls.hakunapi.simple.sdo.filter.SDOIntersects;
import fi.nls.hakunapi.simple.sdo.filter.SDOIntersectsIndex;
import fi.nls.hakunapi.simple.sdo.filter.SDOOverlaps;
import fi.nls.hakunapi.simple.sdo.filter.SDOTouches;
import fi.nls.hakunapi.simple.sdo.filter.SDOWithin;
import fi.nls.hakunapi.simple.sdo.sql.filter.SQLAnd;
import fi.nls.hakunapi.simple.sdo.sql.filter.SQLEqualTo;
import fi.nls.hakunapi.simple.sdo.sql.filter.SQLFilter;
import fi.nls.hakunapi.simple.sdo.sql.filter.SQLGreaterThan;
import fi.nls.hakunapi.simple.sdo.sql.filter.SQLGreaterThanOrEqualTo;
import fi.nls.hakunapi.simple.sdo.sql.filter.SQLIsNotNull;
import fi.nls.hakunapi.simple.sdo.sql.filter.SQLIsNull;
import fi.nls.hakunapi.simple.sdo.sql.filter.SQLLessThan;
import fi.nls.hakunapi.simple.sdo.sql.filter.SQLLessThanOrEqualTo;
import fi.nls.hakunapi.simple.sdo.sql.filter.SQLLike;
import fi.nls.hakunapi.simple.sdo.sql.filter.SQLNot;
import fi.nls.hakunapi.simple.sdo.sql.filter.SQLNotEqualTo;
import fi.nls.hakunapi.simple.sdo.sql.filter.SQLOr;

public class SDOUtil {

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

    protected static final EnumMap<FilterOp, SQLFilter> FILTERS;
    static {
        FILTERS = new EnumMap<>(FilterOp.class);
        FILTERS.put(FilterOp.EQUAL_TO, new SQLEqualTo());
        FILTERS.put(FilterOp.NOT_EQUAL_TO, new SQLNotEqualTo());
        FILTERS.put(FilterOp.GREATER_THAN, new SQLGreaterThan());
        FILTERS.put(FilterOp.GREATER_THAN_OR_EQUAL_TO, new SQLGreaterThanOrEqualTo());
        FILTERS.put(FilterOp.LESS_THAN, new SQLLessThan());
        FILTERS.put(FilterOp.LESS_THAN_OR_EQUAL_TO, new SQLLessThanOrEqualTo());

        FILTERS.put(FilterOp.LIKE, new SQLLike());

        FILTERS.put(FilterOp.NULL, new SQLIsNull());
        FILTERS.put(FilterOp.NOT_NULL, new SQLIsNotNull());

        FILTERS.put(FilterOp.OR, new SQLOr(FILTERS));
        FILTERS.put(FilterOp.AND, new SQLAnd(FILTERS));
        FILTERS.put(FilterOp.NOT, new SQLNot(FILTERS));

        // SDO
        FILTERS.put(FilterOp.INTERSECTS_INDEX, new SDOIntersectsIndex());
        FILTERS.put(FilterOp.INTERSECTS, new SDOIntersects());
        FILTERS.put(FilterOp.EQUALS, new SDOEquals());
        FILTERS.put(FilterOp.DISJOINT, new SDODisjoint());
        FILTERS.put(FilterOp.TOUCHES, new SDOTouches());
        FILTERS.put(FilterOp.WITHIN, new SDOWithin());
        FILTERS.put(FilterOp.OVERLAPS, new SDOOverlaps());
        FILTERS.put(FilterOp.CROSSES, new SDOCrosses());
        FILTERS.put(FilterOp.CONTAINS, new SDOContains());
    }

    public static void bind(Connection c, PreparedStatement ps, List<Filter> filters) throws SQLException {
        int i = 1;
        for (Filter filter : filters) {
            i = FILTERS.get(filter.getOp()).bind(filter, c, ps, i);
        }
    }

    public static void prepareConnection(Connection c) {
        // TODO Auto-generated method stub
        /*
         * if (c.isWrapperFor(Connection.class)) { try { Connection pgConn =
         * c.unwrap(PgConnection.class); pgConn.setForceBinary(true);
         * LOG.debug("Forced JDBC to binary mode!"); } catch (SQLException e) { LOG.
         * warn("Failed to unwrap the connection to PgConnection even though isWrapperFor returned true"
         * ); } }
         * 
         */
    }

    public static List<ValueMapper> select(StringBuilder query, List<HakunaProperty> properties, QueryContext ctx)
            throws Exception {
        query.append("SELECT ");

        Map<StringPair, Integer> columnToIndex = new HashMap<>();
        for (HakunaProperty property : properties) {
            addToSelect(query, columnToIndex, property);
        }
        // Remove trailing comma
        query.setLength(query.length() - 1);

        List<ValueMapper> mappers = new ArrayList<>();
        int iValueContainer = 0;
        for (HakunaProperty property : properties) {
            mappers.add(property.getMapper(columnToIndex, iValueContainer++, ctx));
        }
        return mappers;
    }

    private static void addToSelect(StringBuilder query, Map<StringPair, Integer> columnToIndex,
            HakunaProperty property) {
        if (property instanceof HakunaPropertyComposite) {
            HakunaPropertyComposite composite = (HakunaPropertyComposite) property;
            for (HakunaProperty part : composite.getParts()) {
                addToSelect(query, columnToIndex, part);
            }
        } else {
            String table = property.getTable();
            for (String column : property.getColumns()) {
                StringPair key = new StringPair(table, column);
                if (!columnToIndex.containsKey(key)) {
                    int i = columnToIndex.size();
                    columnToIndex.put(key, i);
                    String s = toSQL(table, column);
                    /*
                     * if (property.getType() == HakunaPropertyType.GEOMETRY) { s =
                     * String.format("ST_AsEWKB(%s)", s); }
                     */
                    query.append(s);
                    query.append(',');
                }
            }
        }
    }

    public static void from(StringBuilder query, String dbSchema, String table) {
        query.append(" FROM ");
        query.append('"').append(dbSchema).append('"').append('.');
        query.append('"').append(table).append('"');
    }

    public static void join(StringBuilder queryBuilder, String dbSchema, Join join) {
        String[] c1SplitCast = join.getLeftColumn().split("::");
        String[] c2SplitCast = join.getRightColumn().split("::");

        String t1 = quote(dbSchema) + '.' + quote(join.getLeftTable());
        String c1 = quote(c1SplitCast[0]);
        String t2 = quote(dbSchema) + '.' + quote(join.getRightTable());
        String c2 = quote(c2SplitCast[0]);

        queryBuilder.append(" JOIN ").append(t2);
        queryBuilder.append(" ON ");
        if (c1SplitCast.length > 1) {
            queryBuilder.append("CAST(");
        }
        queryBuilder.append(t1);
        queryBuilder.append('.');
        queryBuilder.append(c1);
        if (c1SplitCast.length > 1) {
            queryBuilder.append(" AS ").append(c1SplitCast[1]).append(')');
        }

        queryBuilder.append('=');

        if (c2SplitCast.length > 1) {
            queryBuilder.append("CAST(");
        }
        queryBuilder.append(t2);
        queryBuilder.append('.');
        queryBuilder.append(c2);
        if (c2SplitCast.length > 1) {
            queryBuilder.append(" AS ").append(c2SplitCast[1]).append(')');
        }
    }

    private static String quote(String s) {
        return '"' + s + '"';
    }

    public static void where(StringBuilder q, List<Filter> filters) {
        boolean first = true;
        for (Filter filter : filters) {
            String sql = FILTERS.get(filter.getOp()).toSQL(filter);
            if (sql == null) {
                continue;
            }
            if (first) {
                q.append(" WHERE ");
                first = false;
            } else {
                q.append(" AND ");
            }
            q.append(sql);
        }
    }

    public static void orderBy(StringBuilder queryBuilder, HakunaProperty prop, boolean asc) {
        queryBuilder.append(" ORDER BY ");
        queryBuilder.append('"');
        queryBuilder.append(prop.getTable());
        queryBuilder.append('"');
        queryBuilder.append('.');
        queryBuilder.append('"');
        queryBuilder.append(prop.getColumn());
        queryBuilder.append('"');
        queryBuilder.append(asc ? " ASC" : " DESC");
    }

    public static void orderBy(StringBuilder queryBuilder, List<OrderBy> orderBy) {
        if (orderBy == null || orderBy.isEmpty()) {
            return;
        }
        queryBuilder.append(" ORDER BY ");
        for (int i = 0; i < orderBy.size(); i++) {
            HakunaProperty prop = orderBy.get(i).getProperty();
            boolean asc = orderBy.get(i).isAscending();
            if (i > 0) {
                queryBuilder.append(',');
            }
            queryBuilder.append('"');
            queryBuilder.append(prop.getTable());
            queryBuilder.append('"');
            queryBuilder.append('.');
            queryBuilder.append('"');
            queryBuilder.append(prop.getColumn());
            queryBuilder.append('"');
            queryBuilder.append(asc ? " ASC" : " DESC");
        }
    }

    public static void offset(StringBuilder q, int offset) {
        q.append(" OFFSET ").append(offset).append(" ROWS");

    }

    public static void limit(StringBuilder q, int limit) {
        q.append(" FETCH NEXT ").append(limit).append(" ROWS ONLY");
    }

}
