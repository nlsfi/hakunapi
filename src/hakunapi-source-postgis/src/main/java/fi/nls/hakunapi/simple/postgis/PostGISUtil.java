package fi.nls.hakunapi.simple.postgis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.nls.hakunapi.core.QueryContext;
import fi.nls.hakunapi.core.ValueMapper;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.filter.FilterOp;
import fi.nls.hakunapi.core.join.Join;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyComposite;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.core.schemas.Crs;
import fi.nls.hakunapi.core.util.StringPair;
import fi.nls.hakunapi.simple.postgis.filter.PostGISContains;
import fi.nls.hakunapi.simple.postgis.filter.PostGISCrosses;
import fi.nls.hakunapi.simple.postgis.filter.PostGISDisjoint;
import fi.nls.hakunapi.simple.postgis.filter.PostGISEquals;
import fi.nls.hakunapi.simple.postgis.filter.PostGISIntersects;
import fi.nls.hakunapi.simple.postgis.filter.PostGISIntersectsIndex;
import fi.nls.hakunapi.simple.postgis.filter.PostGISOverlaps;
import fi.nls.hakunapi.simple.postgis.filter.PostGISTouches;
import fi.nls.hakunapi.simple.postgis.filter.PostGISWithin;
import fi.nls.hakunapi.simple.postgis.filter.PostgresArrayOverlaps;
import fi.nls.hakunapi.simple.postgis.filter.SQLAnd;
import fi.nls.hakunapi.simple.postgis.filter.SQLEqualTo;
import fi.nls.hakunapi.simple.postgis.filter.SQLFilter;
import fi.nls.hakunapi.simple.postgis.filter.SQLGreaterThan;
import fi.nls.hakunapi.simple.postgis.filter.SQLGreaterThanOrEqualTo;
import fi.nls.hakunapi.simple.postgis.filter.SQLIsNotNull;
import fi.nls.hakunapi.simple.postgis.filter.SQLIsNull;
import fi.nls.hakunapi.simple.postgis.filter.SQLLessThan;
import fi.nls.hakunapi.simple.postgis.filter.SQLLessThanOrEqualTo;
import fi.nls.hakunapi.simple.postgis.filter.SQLLike;
import fi.nls.hakunapi.simple.postgis.filter.SQLNot;
import fi.nls.hakunapi.simple.postgis.filter.SQLNotEqualTo;
import fi.nls.hakunapi.simple.postgis.filter.SQLOr;

public class PostGISUtil {

    private static final EnumMap<FilterOp, SQLFilter> FILTERS;
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

        FILTERS.put(FilterOp.INTERSECTS_INDEX, new PostGISIntersectsIndex());
        FILTERS.put(FilterOp.INTERSECTS, new PostGISIntersects());
        FILTERS.put(FilterOp.EQUALS, new PostGISEquals());
        FILTERS.put(FilterOp.DISJOINT, new PostGISDisjoint());
        FILTERS.put(FilterOp.TOUCHES, new PostGISTouches());
        FILTERS.put(FilterOp.WITHIN, new PostGISWithin());
        FILTERS.put(FilterOp.OVERLAPS, new PostGISOverlaps());
        FILTERS.put(FilterOp.CROSSES, new PostGISCrosses());
        FILTERS.put(FilterOp.CONTAINS, new PostGISContains());

        FILTERS.put(FilterOp.ARRAY_OVERLAPS, new PostgresArrayOverlaps());
    }

    public static List<ValueMapper> select(StringBuilder query, List<HakunaProperty> properties, QueryContext ctx) throws Exception {
        query.append("SELECT ");

        Map<StringPair, Integer> columnToIndex = new HashMap<>();
        for (HakunaProperty property : properties) {
            addToSelect(query, columnToIndex, property, ctx);
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

    private static void addToSelect(StringBuilder query, Map<StringPair, Integer> columnToIndex, HakunaProperty property, QueryContext ctx) {
        if (property instanceof HakunaPropertyComposite) {
            HakunaPropertyComposite composite = (HakunaPropertyComposite) property;
            for (HakunaProperty part : composite.getParts()) {
                addToSelect(query, columnToIndex, part, ctx);
            }
        } else {
            String table = property.getTable();
            for (String column : property.getColumns()) {
                StringPair key = new StringPair(table, column);
                if (!columnToIndex.containsKey(key)) {
                    int i = columnToIndex.size();
                    columnToIndex.put(key, i);
                    String s = SQLUtil.toSQL(table, column);
                    if (property.getType() == HakunaPropertyType.GEOMETRY) {
                        HakunaPropertyGeometry g = (HakunaPropertyGeometry) property;
                        if (ctx.isSourceShouldProjectToSrid() && ctx.getSRID() != g.getStorageSRID()) {
                            int srid = ctx.getSRID() == Crs.CRS84_SRID ? 4326 : ctx.getSRID();
                            s = String.format("ST_AsEWKB(ST_Transform(%s, %d))", s, srid);
                        } else {
                            s = String.format("ST_AsEWKB(%s)", s);
                        }
                    }
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
                q.append(" AND " );
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

    public static void orderBy(StringBuilder queryBuilder, List<HakunaProperty> props, List<Boolean> ascending) {
        if (props == null || props.isEmpty()) {
            return;
        }
        queryBuilder.append(" ORDER BY ");
        for (int i = 0; i < props.size(); i++) {
            HakunaProperty prop = props.get(i);
            boolean asc = ascending.get(i);
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
        q.append(" OFFSET ").append(offset);
        
    }

    public static void limit(StringBuilder q, int limit) {
        q.append(" LIMIT ").append(limit);
    }

    public static void bind(Connection c, PreparedStatement ps, List<Filter> filters) throws SQLException {
        int i = 1;
        for (Filter filter : filters) {
            i = FILTERS.get(filter.getOp()).bind(filter, c, ps, i);
        }
    }

}
