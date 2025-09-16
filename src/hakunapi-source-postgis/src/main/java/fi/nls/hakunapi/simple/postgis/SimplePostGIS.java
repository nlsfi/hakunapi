package fi.nls.hakunapi.simple.postgis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.postgresql.jdbc.PgConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.nls.hakunapi.core.FeatureProducer;
import fi.nls.hakunapi.core.FeatureStream;
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

/**
 * Streaming implementation
 * Reads a batch of BATCH_SIZE rows at a time
 */
public class SimplePostGIS implements FeatureProducer {

    private static final Logger LOG = LoggerFactory.getLogger(SimplePostGIS.class);
    private static final int BATCH_SIZE = 250;

    private final DataSource ds;

    public SimplePostGIS(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public FeatureStream getFeatures(GetFeatureRequest request, GetFeatureCollection col) throws Exception {
        SQLFeatureType ft = (SQLFeatureType) col.getFt();
        List<Filter> filters = col.getFilters();
        int limit = request.getLimit();

        if (filters.stream().anyMatch(it -> it == Filter.DENY)) {
            return new EmptyFeatureStream();
        }

        Set<String> tablesNeeded = new HashSet<>();
        List<Join> joinsNeeded = new ArrayList<>();

        tablesNeeded.add(ft.getPrimaryTable());
        addTablesAndJoinsNeeded(ft.getPrimaryTable(), ft.getJoins(), tablesNeeded, joinsNeeded);
        
        QueryContext ctx = new QueryContext();
        ctx.setSRID(request.getSRID());
        ctx.setSourceShouldProjectToSrid(ft.isSourceWillProject());

        StringBuilder q = new StringBuilder();
        List<ValueMapper> mappers = PostGISUtil.select(q, col.getProperties(), ctx);
        PostGISUtil.from(q, ft.getDbSchema(), ft.getPrimaryTable());
        for (Join join : joinsNeeded) {
            PostGISUtil.join(q, ft.getDbSchema(), join);
        }
        PostGISUtil.where(q, filters);
        if (limit != LimitParam.UNLIMITED) {
            PostGISUtil.orderBy(q, col.getOrderBy());
            PostGISUtil.offset(q, request.getOffset());
            // Limit by n + 1 so that we know if there's next
            PostGISUtil.limit(q, limit + 1);
        }
        String query = q.toString();

        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            c = ds.getConnection();
            if (c.isWrapperFor(PgConnection.class)) { 
                try {
                    PgConnection pgConn = c.unwrap(PgConnection.class);
                    pgConn.setForceBinary(true);
                    LOG.debug("Forced JDBC to binary mode!");
                } catch (SQLException e) {
                    LOG.warn("Failed to unwrap the connection to PgConnection even though isWrapperFor returned true");
                }
            }
            c.setAutoCommit(false);
            ps = c.prepareStatement(query);
            PostGISUtil.bind(c, ps, filters);
            LOG.info("{}", ps.toString());
            int bufSize = BATCH_SIZE;
            ps.setFetchSize(bufSize);
            rs = ps.executeQuery();
            int numColsRs = rs.getMetaData().getColumnCount();
            return new BufferedResultSet(c, ps, rs, numColsRs, mappers, bufSize);
        } catch (Exception e) {
            U.closeSilent(rs);
            U.closeSilent(ps);
            U.closeSilent(c);
            throw e;
        }
    }

    private void addTablesAndJoinsNeeded(String currentTable, List<Join> joins,
            Set<String> tablesNeeded, List<Join> joinsNeeded) {
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
        SQLFeatureType ft = (SQLFeatureType) col.getFt();
        List<Filter> filters = col.getFilters();

        List<HakunaProperty> allProperties = new ArrayList<>();
        allProperties.add(ft.getId());
        if (ft.getGeom() != null) {
            allProperties.add(ft.getGeom());
        }
        allProperties.addAll(col.getProperties());

        StringBuilder q = new StringBuilder("SELECT COUNT(*)");
        PostGISUtil.from(q, ft.getDbSchema(), ft.getPrimaryTable());
        PostGISUtil.where(q, filters);
        String query = q.toString();

        try (Connection c = ds.getConnection();
                PreparedStatement ps = c.prepareStatement(query)) {
            PostGISUtil.bind(c, ps, filters);
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
