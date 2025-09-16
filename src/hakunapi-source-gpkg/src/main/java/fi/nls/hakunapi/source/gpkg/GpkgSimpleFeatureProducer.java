package fi.nls.hakunapi.source.gpkg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.nls.hakunapi.core.FeatureProducer;
import fi.nls.hakunapi.core.FeatureStream;
import fi.nls.hakunapi.core.QueryContext;
import fi.nls.hakunapi.core.ValueMapper;
import fi.nls.hakunapi.core.filter.Filter;
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
public class GpkgSimpleFeatureProducer implements FeatureProducer {

    private static final Logger LOG = LoggerFactory.getLogger(GpkgSimpleFeatureProducer.class);
    private static final int BATCH_SIZE = 250;

    @Override
    public FeatureStream getFeatures(GetFeatureRequest request, GetFeatureCollection col) throws Exception {
        GpkgFeatureType ft = (GpkgFeatureType) col.getFt();
        List<Filter> filters = col.getFilters();
        int limit = request.getLimit();

        if (filters.stream().anyMatch(it -> it == Filter.DENY)) {
            return new EmptyFeatureStream();
        }

        QueryContext ctx = new QueryContext();
        ctx.setSRID(request.getSRID());

        StringBuilder q = new StringBuilder();
        List<ValueMapper> mappers = GpkgQueryUtil.select(q, col.getProperties(), ctx);
        GpkgQueryUtil.from(q, ft.getTable());
        GpkgQueryUtil.where(q, filters);
        if (limit != LimitParam.UNLIMITED) {
            GpkgQueryUtil.orderBy(q, col.getOrderBy());
            // Limit by n + 1 so that we know if there's next
            GpkgQueryUtil.limit(q, limit + 1);
            // In SQLite OFFSET must come after LIMIT
            GpkgQueryUtil.offset(q, request.getOffset());
        }
        String query = q.toString();

        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            c = ft.getDatabase().getConnection();
            c.setAutoCommit(false);
            ps = c.prepareStatement(query);
            GpkgQueryUtil.bind(c, ps, filters);
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

    @Override
    public int getNumberMatched(GetFeatureRequest request, GetFeatureCollection col) throws Exception {
        GpkgFeatureType ft = (GpkgFeatureType) col.getFt();
        List<Filter> filters = col.getFilters();

        List<HakunaProperty> allProperties = new ArrayList<>();
        allProperties.add(ft.getId());
        if (ft.getGeom() != null) {
            allProperties.add(ft.getGeom());
        }
        allProperties.addAll(col.getProperties());

        StringBuilder q = new StringBuilder("SELECT COUNT(*)");
        GpkgQueryUtil.from(q, ft.getTable());
        GpkgQueryUtil.where(q, filters);
        String query = q.toString();

        try (Connection c = ft.getDatabase().getConnection();
                PreparedStatement ps = c.prepareStatement(query)) {
            GpkgQueryUtil.bind(c, ps, filters);
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
