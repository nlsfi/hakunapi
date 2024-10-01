package fi.nls.hakunapi.simple.postgis.filter;

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
import fi.nls.hakunapi.simple.postgis.SQLFeatureType;
import fi.nls.hakunapi.simple.postgis.SQLUtil;

public abstract class SQLComparison implements SQLFilter {

    @Override
    public String toSQL(Filter filter) {
        String property = caseInsensitiveWrap(filter, SQLUtil.toSQL(filter.getProp()));
        return property + getOp() + "?";
    }

    protected static String caseInsensitiveWrap(Filter filter, String propertyName) {
        if (!filter.isCaseInsensitive()) {
            return propertyName;
        }
        HakunaProperty prop = filter.getProp();
        SQLFeatureType sft = (SQLFeatureType) prop.getFeatureType();
        switch (sft.getCaseInsensitiveStrategy()) {
        case LOWER: return String.format("lower(%s)", propertyName);
        case UPPER: return String.format("upper(%s)", propertyName);
        default: throw new IllegalArgumentException("Unknown caseInsensitiveStrategy " + sft.getCaseInsensitiveStrategy());
        }
    }

    @Override
    public int bind(Filter filter, Connection c, PreparedStatement ps, int i) throws SQLException {
        return doBind(filter, c, ps, i);
    }
    
    protected static int doBind(Filter filter, Connection c, PreparedStatement ps, int i) throws SQLException {
        Object value = filter.getValue();
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
            String s = (String) value;
            if (filter.isCaseInsensitive()) {
                SQLFeatureType sft = (SQLFeatureType) filter.getProp().getFeatureType();
                switch (sft.getCaseInsensitiveStrategy()) {
                case LOWER:
                    s = s.toLowerCase();
                    break;
                case UPPER:
                    s = s.toUpperCase();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown caseInsensitiveStrategy " + sft.getCaseInsensitiveStrategy());
                }
            }
            ps.setString(i, s);
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
