package fi.nls.hakunapi.simple.sdo.sql.filter;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.simple.sdo.sql.SQLUtil;

public abstract class SQLComparison implements SQLFilter {

    @Override
    public String toSQL(Filter filter) {
        HakunaProperty prop = filter.getProp();
        return SQLUtil.toSQL(prop) + getOp() + "?";
    }

    @Override
    public int bind(Filter filter, Connection c, PreparedStatement ps, int i) throws SQLException {
        return doBind(filter.getValue(), c, ps, i);
    }
    
    protected static int doBind(Object value, Connection c, PreparedStatement ps, int i) throws SQLException {
        if (value instanceof Boolean) {
            ps.setBoolean(i, (Boolean) value);
        } else if (value instanceof Integer) {
            ps.setInt(i, (Integer) value);
        } else if (value instanceof Long) {
            ps.setLong(i, (Long) value);
        } else if (value instanceof Float) {
            ps.setFloat(i, (Float) value);
        } else if (value instanceof Double) {
            ps.setDouble(i, (Double) value);
        } else if (value instanceof String) {
            ps.setString(i, (String) value);
        } else if (value instanceof LocalDate) {
            ps.setDate(i, Date.valueOf((LocalDate) value));
        } else if (value instanceof Instant) {
            ps.setTimestamp(i, Timestamp.from((Instant) value));
        } else if (value instanceof OffsetDateTime) {
            ps.setTimestamp(i, Timestamp.from(((OffsetDateTime)value).toInstant()));
        } else if (value instanceof UUID) {
            ps.setObject(i, (UUID) value, java.sql.Types.OTHER);
        } else {
            throw new UnsupportedOperationException("Unsupported value type " + value.getClass().getName());
        }
        return i + 1;
    }

    public abstract String getOp(); 

}
