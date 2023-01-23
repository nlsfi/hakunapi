package fi.nls.hakunapi.simple.postgis.filter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBWriter;

import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.projection.ProjectionHelper;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;

public abstract class PostGISGeometryFunction implements SQLFilter {
    
    public abstract String getFunctionName(); 

    @Override
    public String toSQL(Filter filter) {
        HakunaProperty prop = filter.getProp();
        return String.format("%s(\"%s\".\"%s\", %s)",
                getFunctionName(), prop.getTable(), prop.getColumn(), "ST_GeomFromWKB(?, ?)");
    }

    @Override
    public int bind(Filter filter, Connection c, PreparedStatement ps, int i) throws SQLException {
        HakunaPropertyGeometry prop = (HakunaPropertyGeometry) filter.getProp();
        Geometry geom = (Geometry) filter.getValue();
        geom = ProjectionHelper.reprojectToStorageCRS(prop, geom);

        int outputDimension = geom.getDimension() > 2 ? 3 : 2;
        byte[] wkb = new WKBWriter(outputDimension, false).write(geom);
        ps.setBytes(i++, wkb);
        ps.setInt(i++, geom.getSRID());

        return i;
    }

}
