package fi.nls.hakunapi.simple.sdo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import fi.nls.hakunapi.core.ValueMapper;
import fi.nls.hakunapi.simple.sdo.sql.BufferedResultSet;
import fi.nls.hakunapi.simple.sdo.sql.ResultSetValueProvider;
import oracle.jdbc.driver.OracleConnection;

public class SDOBufferedResultSet extends BufferedResultSet {

	OracleConnection oc;
	
	public SDOBufferedResultSet(Connection c, PreparedStatement ps, ResultSet rs, int numColsRs,
	        List<ValueMapper> mappers, int bufSize) throws SQLException {
		super(c, ps, rs, numColsRs, mappers, bufSize);

		oc = c.unwrap(OracleConnection.class);

	}

	@Override
	protected ResultSetValueProvider createValueProvider(ResultSet rs, int numColsRs) {


		return new SDOResultSetValueProvider(oc, rs, numColsRs);
	}

}
