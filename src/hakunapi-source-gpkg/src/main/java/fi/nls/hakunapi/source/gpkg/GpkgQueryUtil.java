package fi.nls.hakunapi.source.gpkg;

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
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyComposite;
import fi.nls.hakunapi.core.util.StringPair;
import fi.nls.hakunapi.source.gpkg.filter.GpkgIntersects;
import fi.nls.hakunapi.source.gpkg.filter.SQLAnd;
import fi.nls.hakunapi.source.gpkg.filter.SQLEqualTo;
import fi.nls.hakunapi.source.gpkg.filter.SQLFilter;
import fi.nls.hakunapi.source.gpkg.filter.SQLGreaterThan;
import fi.nls.hakunapi.source.gpkg.filter.SQLGreaterThanOrEqualTo;
import fi.nls.hakunapi.source.gpkg.filter.SQLIsNotNull;
import fi.nls.hakunapi.source.gpkg.filter.SQLIsNull;
import fi.nls.hakunapi.source.gpkg.filter.SQLLessThan;
import fi.nls.hakunapi.source.gpkg.filter.SQLLessThanOrEqualTo;
import fi.nls.hakunapi.source.gpkg.filter.SQLLike;
import fi.nls.hakunapi.source.gpkg.filter.SQLNot;
import fi.nls.hakunapi.source.gpkg.filter.SQLNotEqualTo;
import fi.nls.hakunapi.source.gpkg.filter.SQLOr;
import fi.nls.hakunapi.source.gpkg.filter.SQLUtil;

public class GpkgQueryUtil {

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

        FILTERS.put(FilterOp.INTERSECTS_INDEX, new GpkgIntersects());
        FILTERS.put(FilterOp.INTERSECTS, new GpkgIntersects());
    }

    public static List<ValueMapper> select(StringBuilder query, List<HakunaProperty> properties, QueryContext ctx) throws Exception {
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

    private static void addToSelect(StringBuilder query, Map<StringPair, Integer> columnToIndex, HakunaProperty property) {
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
                    String s = SQLUtil.toSQL(table, column);
                    query.append(s);
                    query.append(',');
                }
            }
        }
    }

    public static void from(StringBuilder query, String table) {
        query.append(" FROM ");
        query.append('"').append(table).append('"');
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
