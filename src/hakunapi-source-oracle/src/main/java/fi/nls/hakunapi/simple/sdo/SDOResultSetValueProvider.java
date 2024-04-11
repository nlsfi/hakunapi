package fi.nls.hakunapi.simple.sdo;

import java.sql.ResultSet;

import org.geotools.data.oracle.sdo.GeometryConverter;
import org.locationtech.jts.geom.Geometry;

import fi.nls.hakunapi.core.geom.HakunaGeometry;
import fi.nls.hakunapi.core.geom.HakunaGeometryJTS;
import fi.nls.hakunapi.simple.sdo.sql.ResultSetValueProvider;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleResultSet;
import oracle.jdbc.OracleStruct;

public class SDOResultSetValueProvider extends ResultSetValueProvider {

	private GeometryConverter conv;

	public SDOResultSetValueProvider(OracleConnection oc, ResultSet rs, int numCols) {
		super(rs, numCols);

		conv = new GeometryConverter(oc);

	}

	@Override
	public HakunaGeometry getHakunaGeometry(int i) {
		try {

			OracleResultSet ors = rs.unwrap(OracleResultSet.class);

			OracleStruct struct = ors.getSTRUCT(i + 1);
			if (struct == null) {
				return null;
			}

			Geometry geom = conv.asGeometry(struct);
			return new HakunaGeometryJTS(geom);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
