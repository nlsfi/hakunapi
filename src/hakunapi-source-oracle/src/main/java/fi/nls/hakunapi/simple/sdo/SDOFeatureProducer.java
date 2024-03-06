package fi.nls.hakunapi.simple.sdo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.nls.hakunapi.core.FeatureProducer;
import fi.nls.hakunapi.core.FeatureStream;
import fi.nls.hakunapi.core.PaginationStrategy;
import fi.nls.hakunapi.core.QueryContext;
import fi.nls.hakunapi.core.ValueMapper;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.join.Join;
import fi.nls.hakunapi.core.join.JoinType;
import fi.nls.hakunapi.core.param.LimitParam;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import fi.nls.hakunapi.core.util.EmptyFeatureStream;
import fi.nls.hakunapi.core.util.U;

public class SDOFeatureProducer implements FeatureProducer {

	private static final Logger LOG = LoggerFactory.getLogger(SDOFeatureProducer.class);
	private static final int BATCH_SIZE = 250;
	private final DataSource ds;

	public SDOFeatureProducer(DataSource ds) {
		this.ds = ds;
	}

	@Override
	public FeatureStream getFeatures(GetFeatureRequest request, GetFeatureCollection col) throws Exception {
		SDOFeatureType ft = (SDOFeatureType) col.getFt();
		List<Filter> filters = col.getFilters();
		int limit = request.getLimit();
		PaginationStrategy pagination = ft.getPaginationStrategy();

		if (filters.stream().anyMatch(it -> it == Filter.DENY)) {
			return new EmptyFeatureStream();
		}

		Set<String> tablesNeeded = new HashSet<>();
		List<Join> joinsNeeded = new ArrayList<>();

		tablesNeeded.add(ft.getPrimaryTable());
		addTablesAndJoinsNeeded(ft.getPrimaryTable(), ft.getJoins(), tablesNeeded, joinsNeeded);

		QueryContext ctx = new QueryContext();
		ctx.setSRID(request.getSRID());

		StringBuilder q = new StringBuilder();
		List<ValueMapper> mappers = SDOUtil.select(q, col.getProperties(), ctx);
		SDOUtil.from(q, ft.getDbSchema(), ft.getPrimaryTable());
		for (Join join : joinsNeeded) {
			SDOUtil.join(q, ft.getDbSchema(), join);
		}
		SDOUtil.where(q, filters);
		if (limit != LimitParam.UNLIMITED) {
			if (pagination.shouldSortBy()) {
				SDOUtil.orderBy(q, pagination.getProperties(), pagination.getAscending());
			}
			if (pagination.shouldOffset()) {
				SDOUtil.offset(q, request.getOffset());
			}
			// Limit by n + maxGroupSize for pagination purposes
			SDOUtil.limit(q, limit + pagination.getMaxGroupSize());
		}
		String query = q.toString();
		
		LOG.info(query);

		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			c = ds.getConnection();
			SDOUtil.prepareConnection(c);
			c.setAutoCommit(false);
			ps = c.prepareStatement(query);
			SDOUtil.bind(c, ps, filters);
			LOG.info("{}", ps.toString());
			int bufSize = BATCH_SIZE;
			ps.setFetchSize(bufSize);
			rs = ps.executeQuery();
			int numColsRs = rs.getMetaData().getColumnCount();
			return new SDOBufferedResultSet(c, ps, rs, numColsRs, mappers, bufSize);
		} catch (Exception e) {
			U.closeSilent(rs);
			U.closeSilent(ps);
			U.closeSilent(c);
			throw e;
		}
	}

	private void addTablesAndJoinsNeeded(String currentTable, List<Join> joins, Set<String> tablesNeeded,
	        List<Join> joinsNeeded) {
		for (Join join : joins) {
			if (join.getJoinType() != JoinType.OneToOne) {
				throw new IllegalArgumentException("Not simple!");
			}
			if (join.getLeftTable().equals(currentTable)) {
				String tableToJoin = join.getRightTable();
				if (!tablesNeeded.contains(tableToJoin)) {
					tablesNeeded.add(tableToJoin);
					joinsNeeded.add(join);
					addTablesAndJoinsNeeded(tableToJoin, joins, tablesNeeded, joinsNeeded);
				}
			}
		}
	}

	@Override
	public int getNumberMatched(GetFeatureRequest request, GetFeatureCollection col) throws Exception {
		SDOFeatureType ft = (SDOFeatureType) col.getFt();
		List<Filter> filters = col.getFilters();

		List<HakunaProperty> allProperties = new ArrayList<>();
		allProperties.add(ft.getId());
		if (ft.getGeom() != null) {
			allProperties.add(ft.getGeom());
		}
		allProperties.addAll(col.getProperties());

		StringBuilder q = new StringBuilder("SELECT COUNT(*)");
		SDOUtil.from(q, ft.getDbSchema(), ft.getPrimaryTable());
		SDOUtil.where(q, filters);
		String query = q.toString();

		try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(query)) {
			SDOUtil.bind(c, ps, filters);
			LOG.debug(ps.toString());
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					return -1;
				}
				return rs.getInt(1);
			}
		}
	}

}
