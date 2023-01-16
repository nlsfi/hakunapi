package fi.nls.hakunapi.core.param;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import fi.nls.hakunapi.core.WFS3Service;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.geom.Bbox;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import fi.nls.hakunapi.core.schemas.Crs;
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
    public Parameter toParameter(WFS3Service service) {
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
    public void modify(WFS3Service service, GetFeatureRequest request, String value) throws IllegalArgumentException {
        if (value == null || value.isEmpty()) {
            return;
        }

        Envelope env = Bbox.parse(value);
        Geometry bbox = Bbox.toGeometry(env);
        bbox.setSRID(Crs.CRS84_SRID);

        for (GetFeatureCollection c : request.getCollections()) {
            HakunaPropertyGeometry geometryProp = c.getFt().getGeom();
            if (geometryProp != null) {
                c.addFilter(Filter.intersects(geometryProp, bbox).setTag(TAG));
            }
        }

        request.addQueryParam(getParamName(), value);
    }

}
