package fi.nls.hakunapi.simple.sdo.filter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.geotools.data.oracle.sdo.GeometryConverter;
import org.locationtech.jts.geom.Geometry;

import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.projection.ProjectionHelper;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.sql.filter.SQLFilter;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleStruct;

public abstract class SDOGeometryFunction implements SQLFilter {

	public abstract String getFunctionName();

	@Override
	public String toSQL(Filter filter) {
		HakunaProperty prop = filter.getProp();

		return String.format(
				//
				"MDSYS.SDO_RELATE(\"%s\".\"%s\", %s,'MASK=%s')='TRUE'",
				//
				prop.getTable(), prop.getColumn(), "?",
				//
				getFunctionName());
	}

	@Override
	public int bind(Filter filter, Connection c, PreparedStatement ps, int i) throws SQLException {
		HakunaPropertyGeometry prop = (HakunaPropertyGeometry) filter.getProp();
		Geometry geom = (Geometry) filter.getValue();
		geom = ProjectionHelper.reprojectToStorageCRS(prop, geom);
		
		OracleConnection oc = c.unwrap(OracleConnection.class);

		GeometryConverter conv = new GeometryConverter(oc);

		OracleStruct geomStruct = conv.toSDO(geom, prop.getStorageSRID());

		ps.setObject(i++, geomStruct);
		//ps.setInt(i++, geom.getSRID());

		return i;
	}

}
