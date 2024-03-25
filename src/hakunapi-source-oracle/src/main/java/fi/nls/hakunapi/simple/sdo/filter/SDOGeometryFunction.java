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
import fi.nls.hakunapi.simple.sdo.sql.filter.SQLFilter;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleStruct;

public abstract class SDOGeometryFunction implements SQLFilter {

    public abstract String getMask();

    @Override
    public String toSQL(Filter filter) {
        HakunaProperty prop = filter.getProp();

        return String.format(
                //
                //
                "MDSYS.SDO_RELATE(\"%s\".\"%s\", %s,'MASK=%s')='TRUE'",
                //
                prop.getTable(), prop.getColumn(), "?",
                //
                getMask());
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
        // ps.setInt(i++, geom.getSRID());

        return i;
    }

    /*
     * https://docs.oracle.com/database/121/SPATL/sdo_relate.htm#SPATL1039
     * https://docs.oracle.com/database/121/SPATL/spatial-relationships-and-filtering.htm#SPATL460
     * 
     * The mask keyword specifies the topological relationship of interest. This is
     * a required parameter. Valid mask keyword values are one or more of the
     * following in the nine-intersection pattern: TOUCH, OVERLAPBDYDISJOINT,
     * OVERLAPBDYINTERSECT, EQUAL, INSIDE, COVEREDBY, CONTAINS, COVERS, ANYINTERACT,
     * ON. Multiple masks are combined with the logical Boolean operator OR, for
     * example, 'mask=inside+touch'. See Spatial Relationships and Filtering for an
     * explanation of the nine-intersection relationship pattern.
     */

}
