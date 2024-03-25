package fi.nls.hakunapi.simple.sdo.sql;

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

public class BufferedResultSet implements FeatureStream {

	protected static final Logger LOG = LoggerFactory.getLogger(BufferedResultSet.class);

	protected final Connection c;
	protected final PreparedStatement ps;
	protected final ResultSet rs;
	protected final ResultSetValueProvider valueProvider;
	protected final List<ValueMapper> mappers;
	protected final ValueContainer[] buffer;

	protected int i;
	protected int j;
	protected boolean closed;

	public BufferedResultSet(Connection c, PreparedStatement ps, ResultSet rs, int numColsRs, List<ValueMapper> mappers,
	        int bufSize) {
		this.c = c;
		this.ps = ps;
		this.rs = rs;
		this.valueProvider = createValueProvider(rs, numColsRs);
		this.mappers = mappers;
		this.buffer = new ValueContainer[bufSize];
	}

	protected ResultSetValueProvider createValueProvider(ResultSet rs, int numColsRs) {
		return new ResultSetValueProvider(rs, numColsRs);
	}

	@Override
	public boolean hasNext() {
		if (i < j) {
			return true;
		}
		if (closed) {
			return false;
		}

		i = 0;
		j = 0;
		try {
			for (; j < buffer.length; j++) {
				if (!rs.next()) {
					close();
					break;
				}
				ValueContainer container = buffer[j];
				if (container == null) {
					buffer[j] = container = new ObjectArrayValueContainer(mappers.size());
				}
				for (ValueMapper mapper : mappers) {
					mapper.accept(valueProvider, container);
				}
			}
			return j > 0;
		} catch (Exception e) {
			LOG.error("Failed to retrieve more features", e);
			close();
			return false;
		}
	}

	@Override
	public ValueProvider next() {
		return buffer[i++];
	}

	@Override
	public void close() {
		closed = true;
		U.closeSilent(rs);
		U.closeSilent(ps);
		U.closeSilent(c);
	}

}
