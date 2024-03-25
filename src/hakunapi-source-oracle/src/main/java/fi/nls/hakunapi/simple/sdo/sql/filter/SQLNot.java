package fi.nls.hakunapi.simple.sdo.sql.filter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.EnumMap;

import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.filter.FilterOp;

public class SQLNot implements SQLFilter {

    private final EnumMap<FilterOp, SQLFilter> filterOpToImpl;

    public SQLNot(EnumMap<FilterOp, SQLFilter> filterOpToImpl) {
        this.filterOpToImpl = filterOpToImpl;
    }

    @Override
    public String toSQL(Filter filter) {
        Filter toNegate = (Filter) filter.getValue();
        SQLFilter sqlFilter = filterOpToImpl.get(toNegate.getOp());
        return "NOT " + sqlFilter.toSQL(toNegate);
    }

    @Override
    public int bind(Filter filter, Connection c, PreparedStatement ps, int i) throws SQLException {
        Filter toNegate = (Filter) filter.getValue();
        SQLFilter sqlFilter = filterOpToImpl.get(toNegate.getOp());
        return sqlFilter.bind(toNegate, c, ps, i);
    }

}
