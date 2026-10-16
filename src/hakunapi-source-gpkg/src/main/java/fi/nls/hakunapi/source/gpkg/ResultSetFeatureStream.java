package fi.nls.hakunapi.source.gpkg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.nls.hakunapi.core.FeatureStream;
import fi.nls.hakunapi.core.ObjectArrayValueContainer;
import fi.nls.hakunapi.core.ValueContainer;
import fi.nls.hakunapi.core.ValueMapper;
import fi.nls.hakunapi.core.ValueProvider;
import fi.nls.hakunapi.core.util.U;

/** Streams rows one at a time; the returned ValueProvider is valid until the next hasNext(). */
public class ResultSetFeatureStream implements FeatureStream {

    private static final Logger LOG = LoggerFactory.getLogger(ResultSetFeatureStream.class);

    private final Connection c;
    private final PreparedStatement ps;
    private final ResultSet rs;
    private final ResultSetValueProvider valueProvider;
    private final List<ValueMapper> mappers;
    private final ValueContainer container;

    private boolean ready;
    private boolean closed;

    public ResultSetFeatureStream(Connection c, PreparedStatement ps, ResultSet rs, int numColsRs,
            List<ValueMapper> mappers) {
        this.c = c;
        this.ps = ps;
        this.rs = rs;
        this.valueProvider = new ResultSetValueProvider(rs, numColsRs, SqliteBlobReader.of(ps));
        this.mappers = mappers;
        this.container = new ObjectArrayValueContainer(mappers.size());
    }

    @Override
    public boolean hasNext() {
        if (ready) {
            return true;
        }
        if (closed) {
            return false;
        }
        try {
            if (!rs.next()) {
                close();
                return false;
            }
            for (ValueMapper mapper : mappers) {
                mapper.accept(valueProvider, container);
            }
            ready = true;
            return true;
        } catch (Exception e) {
            LOG.error("Failed to retrieve more features", e);
            close();
            return false;
        }
    }

    @Override
    public ValueProvider next() {
        ready = false;
        return container;
    }

    @Override
    public void close() {
        closed = true;
        U.closeSilent(rs);
        U.closeSilent(ps);
        U.closeSilent(c);
    }

}
