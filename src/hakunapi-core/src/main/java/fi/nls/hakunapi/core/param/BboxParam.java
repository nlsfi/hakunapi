package fi.nls.hakunapi.core.param;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;

import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.SRIDCode;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.geom.Bbox;
import fi.nls.hakunapi.core.projection.ProjectionHelper;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.Parameter.StyleEnum;
import io.swagger.v3.oas.models.parameters.QueryParameter;

public class BboxParam implements GetFeatureParam {

    public static final String TAG = BboxParam.class.getName();
    public static final String DESCRIPTION = "Only features that have a geometry that intersects the bounding box are selected.\n" +
            "The bounding box is provided as four or six numbers, depending on whether the\n" +
            "coordinate reference system includes a vertical axis (elevation or depth):\n\n" +
            "* Lower left corner, coordinate axis 1\n" +
            "* Lower left corner, coordinate axis 2\n" +
            "* Lower left corner, coordinate axis 3 (optional)\n" +
            "* Upper right corner, coordinate axis 1\n" +
            "* Upper right corner, coordinate axis 2\n" +
            "* Upper right corner, coordinate axis 3 (optional)\n\n" +
            "The coordinate reference system of the values is WGS84 longitude/latitude\n" +
            "(http://www.opengis.net/def/crs/OGC/1.3/CRS84) unless a different coordinate\n" +
            "reference system is specified in the parameter `bbox-crs`.\n\n" +
            "For WGS84 longitude/latitude the values are in most cases the sequence of\n" +
            "minimum longitude, minimum latitude, maximum longitude and maximum latitude.\n" +
            "However, in cases where the box spans the antimeridian the first value\n" +
            "(west-most box edge) is larger than the third value (east-most box edge).\n\n" +
            "If a feature has multiple spatial geometry properties, it is the decision of the\n" +
            "server whether only a single spatial geometry property is used to determine\n" +
            "the extent or all relevant geometries.";

    @Override
    public String getParamName() {
        return "bbox";
    }

    @Override
    public Parameter toParameter(FeatureServiceConfig service) {
        return new QueryParameter()
                .name(getParamName())
                .style(StyleEnum.FORM)
                .explode(false)
                .description(DESCRIPTION)
                .schema(new ArraySchema()
                        .items(new NumberSchema())
                        .description(DESCRIPTION)
                        .minItems(4)
                        .maxItems(6));
    }

    @Override
    public void modify(FeatureServiceConfig service, GetFeatureRequest request, String value) throws IllegalArgumentException {
        if (value == null || value.isEmpty()) {
            return;
        }

        int bboxSrid = request.getBboxSrid();

        Coordinate[] coordinates = Bbox.parseCoordinates(value);

        // Get SRIDCode for wrap-x bounds
        SRIDCode sridCode = service.getSridCode(bboxSrid)
                .orElseThrow(() -> new IllegalArgumentException("Unknown SRID: " + bboxSrid));

        // Handle lat/lon axis order (swap before checking wrap-x)
        if (sridCode.isLatLon()) {
            for (Coordinate c : coordinates) {
                swapAxisOrder(c);
            }
        }

        double x1 = coordinates[0].x;
        double y1 = coordinates[0].y;
        double x2 = coordinates[1].x;
        double y2 = coordinates[1].y;

        // Create bbox geometry (preserves coordinate order for wrap-x detection)
        Geometry bbox = Bbox.createBboxGeometry(x1, y1, x2, y2, null);
        bbox.setSRID(bboxSrid);

        // Check if this is a bbox that crosses the wrap-x boundary (antimeridian)
        if (Bbox.isWrapXCrossingBbox(bbox)) {
            if (sridCode.supportsWrapX()) {
                bbox = Bbox.splitWrapXBbox(bbox, sridCode.getWrapXMin(), sridCode.getWrapXMax());
            } else {
                throw new IllegalArgumentException(
                    "Invalid bbox: west-most edge is larger than east-most edge. " +
                    "This is only valid for coordinate systems that support wrap-x.");
            }
        }

        boolean wasMultiPolygon = bbox instanceof MultiPolygon;

        for (GetFeatureCollection c : request.getCollections()) {
            HakunaPropertyGeometry geometryProp = c.getFt().getGeom();
            if (geometryProp != null) {
                Geometry geom = ProjectionHelper.reprojectToStorageCRS(geometryProp, bbox);

                // Optimization: if reprojection caused the two polygons to touch, merge them
                if (wasMultiPolygon && geom instanceof MultiPolygon) {
                    geom = Bbox.tryMergeMultiPolygon((MultiPolygon) geom);
                }

                c.addFilter(Filter.intersects(geometryProp, geom).setTag(TAG));
            }
        }

        request.addQueryParam(getParamName(), value);
    }

    private static void swapAxisOrder(Coordinate c) {
        final double x = c.getX();
        final double y = c.getY();
        c.setX(y);
        c.setY(x);
    }

}
