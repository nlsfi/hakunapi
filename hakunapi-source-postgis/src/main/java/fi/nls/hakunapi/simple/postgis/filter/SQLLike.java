package fi.nls.hakunapi.simple.postgis.filter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.filter.LikeFilter;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.simple.postgis.SQLUtil;

public class SQLLike implements SQLFilter {
    
    @Override
    public String toSQL(Filter filter) {
        if (filter.getProp().isStatic()) {
            throw new UnsupportedOperationException();
        }

        LikeFilter likeFilter = (LikeFilter) filter;
        HakunaProperty prop = filter.getProp();
        
        String propertyName;
        if (likeFilter.isCaseInsensitive()) {
            propertyName = String.format("lower(%s)", SQLUtil.toSQL(prop));
        } else {
            propertyName = SQLUtil.toSQL(prop);
        }

        return propertyName + " LIKE ?";
    }
    
    @Override
    public int bind(Filter filter, Connection c, PreparedStatement ps, int i) throws SQLException {
        LikeFilter likeFilter = (LikeFilter) filter;
        String value = (String) filter.getValue();
        char wild = likeFilter.getWildCard();
        char single = likeFilter.getSingleChar();
        char escape = likeFilter.getEscape();
        value = replaceWildcards(value, wild, single, escape);
        if (likeFilter.isCaseInsensitive()) {
            value = value.toLowerCase();
        }
        ps.setString(i, value);
        return i + 1;
    }
    
    protected static String replaceWildcards(String value, char wild, char single, char escape) {
        if (wild == '%' && single == '_' && escape == '\\') {
            return value;
        }

        // wild(*), single(?), escape(\)
        // foo\?bar => LIKE foo?bar
        // foo*bar => LIKE foo%bar
        // foo\\?bar => LIKE foo\\%bar
        
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch == escape) {
                if (i + 1 == value.length()) { // foo@ (escape '@') => foo
                    break;
                }
                char next = value.charAt(++i);
                // foo@bar (escape '@', wild '*', single '?') => foobar
                
                // foo@@bar (escape '@', wild '*', single '?') => foo@bar
                
                // foo@%bar (escape '@', wild '*', single '?') => foo\%bar
                // foo@%bar (escape '@', wild '%', single '?') => foo\%bar
                // foo@*bar (escape '@', wild '*', single '?') => foo*bar

                // foo@_bar (escape '@', wild '*', single '?') => foo\_bar
                // foo@?bar (escape '@', wild '*', single '?') => foo\_bar
                // foo@_bar (escape '@', wild '*', single '_') => foo\_bar
                
                if (next == '\\') {
                    sb.append('\\').append('\\');
                } else if (next == '%') {
                    sb.append('\\').append('%');
                } else if (next == '_') {
                    sb.append('\\').append('_');
                } else {
                    sb.append(next);
                }
            } else if (ch == single) {
                sb.append('_');
            } else if (ch == '_') {
                sb.append('\\').append('_');
            } else if (ch == wild) {
                sb.append('%');
            } else if (ch == '%') {
                sb.append('\\').append('%');
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

}
