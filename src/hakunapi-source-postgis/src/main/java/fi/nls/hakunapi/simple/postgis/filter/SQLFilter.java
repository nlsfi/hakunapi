package fi.nls.hakunapi.simple.postgis.filter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import fi.nls.hakunapi.core.filter.Filter;

public interface SQLFilter {

    public String toSQL(Filter filter);
    public int bind(Filter filter, Connection c, PreparedStatement ps, int i) throws SQLException;

}
