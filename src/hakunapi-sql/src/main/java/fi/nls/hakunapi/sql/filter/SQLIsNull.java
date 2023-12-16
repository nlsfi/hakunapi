package fi.nls.hakunapi.sql.filter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.sql.SQLUtil;

public class SQLIsNull implements SQLFilter {

    @Override
    public String toSQL(Filter filter) {
        HakunaProperty prop = filter.getProp();
        return SQLUtil.toSQL(prop) + " IS NULL";
    }

    @Override
    public int bind(Filter filter, Connection c, PreparedStatement ps, int i) throws SQLException {
        return i;
    }

}
