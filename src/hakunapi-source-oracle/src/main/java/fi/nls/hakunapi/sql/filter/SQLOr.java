package fi.nls.hakunapi.sql.filter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.List;

import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.filter.FilterOp;

public class SQLOr implements SQLFilter {

    private final EnumMap<FilterOp, SQLFilter> filterOpToImpl;

    public SQLOr(EnumMap<FilterOp, SQLFilter> filterOpToImpl) {
        this.filterOpToImpl = filterOpToImpl;
    }

    @Override
    public String toSQL(Filter filter) {
        @SuppressWarnings("unchecked")
        List<Filter> subFilters = (List<Filter>) filter.getValue();
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        boolean first = true;
        for (Filter subFilter : subFilters) {
            if (!first) {
                sb.append(" OR ");
            }
            SQLFilter sqlFilter = filterOpToImpl.get(subFilter.getOp());
            if (sqlFilter != null) {
                sb.append('(').append(sqlFilter.toSQL(subFilter)).append(')');
                first = false;
            }
        }
        sb.append(')');
        return sb.toString();
    }

    @Override
    public int bind(Filter filter, Connection c, PreparedStatement ps, int i) throws SQLException {
        @SuppressWarnings("unchecked")
        List<Filter> subFilters = (List<Filter>) filter.getValue();
        for (Filter subFilter : subFilters) {
            SQLFilter sqlFilter = filterOpToImpl.get(subFilter.getOp());
            if (sqlFilter != null) {
                i = sqlFilter.bind(subFilter, c, ps, i);
            }
        }
        return i;
    }

}

