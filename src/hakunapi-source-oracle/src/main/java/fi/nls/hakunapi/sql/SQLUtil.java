package fi.nls.hakunapi.sql;

import java.util.EnumMap;

import fi.nls.hakunapi.core.filter.FilterOp;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.sql.filter.SQLAnd;
import fi.nls.hakunapi.sql.filter.SQLEqualTo;
import fi.nls.hakunapi.sql.filter.SQLFilter;
import fi.nls.hakunapi.sql.filter.SQLGreaterThan;
import fi.nls.hakunapi.sql.filter.SQLGreaterThanOrEqualTo;
import fi.nls.hakunapi.sql.filter.SQLIsNotNull;
import fi.nls.hakunapi.sql.filter.SQLIsNull;
import fi.nls.hakunapi.sql.filter.SQLLessThan;
import fi.nls.hakunapi.sql.filter.SQLLessThanOrEqualTo;
import fi.nls.hakunapi.sql.filter.SQLLike;
import fi.nls.hakunapi.sql.filter.SQLNot;
import fi.nls.hakunapi.sql.filter.SQLNotEqualTo;
import fi.nls.hakunapi.sql.filter.SQLOr;

public class SQLUtil {

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

	protected static final EnumMap<FilterOp, SQLFilter> SQL_FILTERS;
	static {
		SQL_FILTERS = new EnumMap<>(FilterOp.class);
		SQL_FILTERS.put(FilterOp.EQUAL_TO, new SQLEqualTo());
		SQL_FILTERS.put(FilterOp.NOT_EQUAL_TO, new SQLNotEqualTo());
		SQL_FILTERS.put(FilterOp.GREATER_THAN, new SQLGreaterThan());
		SQL_FILTERS.put(FilterOp.GREATER_THAN_OR_EQUAL_TO, new SQLGreaterThanOrEqualTo());
		SQL_FILTERS.put(FilterOp.LESS_THAN, new SQLLessThan());
		SQL_FILTERS.put(FilterOp.LESS_THAN_OR_EQUAL_TO, new SQLLessThanOrEqualTo());

		SQL_FILTERS.put(FilterOp.LIKE, new SQLLike());

		SQL_FILTERS.put(FilterOp.NULL, new SQLIsNull());
		SQL_FILTERS.put(FilterOp.NOT_NULL, new SQLIsNotNull());

		SQL_FILTERS.put(FilterOp.OR, new SQLOr(SQL_FILTERS));
		SQL_FILTERS.put(FilterOp.AND, new SQLAnd(SQL_FILTERS));
		SQL_FILTERS.put(FilterOp.NOT, new SQLNot(SQL_FILTERS));

	}

	

}
