package fi.nls.hakunapi.core.schemas;

import java.util.Collections;
import java.util.List;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class SpatialExtent {

    private static final String DESCRIPTION = "The spatial extent of the features in the collection.";
    private static final String DESCRIPTION_BBOX = "One or more bounding boxes that describe the spatial extent of the dataset. In the Core only a single bounding box is supported. Extensions may support additional areas. If multiple areas are provided, the union of the boundingboxes describes the spatial extent.";
    private static final String DESCRIPTION_BBOX_ITEM = "Each bounding box is provided as four or six numbers, depending on whether the coordinate reference system includes a vertical axis (height or depth)"
            + "\r\n* Lower left corner, coordinate axis 1"
            + "\r\n* Lower left corner, coordinate axis 2"
            + "\r\n* Minimum value, coordinate axis 3 (optional)"
            + "\r\n* Upper right corner, coordinate axis 1"
            + "\r\n* Upper right corner, coordinate axis 2"
            + "\r\n* Maximum value, coordinate axis 3 (optional)"
            + "\r\nThe coordinate reference system of the values is WGS 84 longitude/latitude (http://www.opengis.net/def/crs/OGC/1.3/CRS84) unless a different coordinate reference system is specified in `crs`. For WGS 84 longitude/latitude the values are in most cases the sequence of minimum longitude, minimum latitude, maximum longitude and maximum latitude. However, in cases where the box spans the antimeridian the first value(west-most box edge) is larger than the third value (east-most box edge). If the vertical axis is included, the third and the sixth number are the bottom and the top of the 3-dimensional bounding box. If a feature has multiple spatial geometry properties, it is the decision of the server whether only a single spatial geometry property is used to determine the extent or all relevant geometries.";
    private static final String DESCRIPTION_CRS = "Coordinate reference system of the coordinates in the spatial extent (property `bbox`). The default reference system is WGS 84 longitude/latitude. In the Core this is the only supported coordinate reference system. Extensions may support additional coordinate reference systems and add additional enum values";

    private final List<double[]> bbox;
    private final String crs;

    public SpatialExtent(double[] bbox, String crs) {
        this(Collections.singletonList(bbox), crs);
    }

    public SpatialExtent(List<double[]> bbox, String crs) {
        this.bbox = bbox;
        this.crs = crs;
    }

    public List<double[]> getBbox() {
        return bbox;
    }

    public String getCrs() {
        return crs;
    }

    static Schema toSchema() {
        Schema singleBboxSchema = new ArraySchema()
                .items(new NumberSchema())
                .minItems(4)
                .maxItems(6)
                .example(new double[] { -180, -90, 180, 90 })
                .description(DESCRIPTION_BBOX_ITEM);

        Schema bboxSchema = new ArraySchema()
                .items(singleBboxSchema)
                .minItems(1)
                .description(DESCRIPTION_BBOX);

        Schema crsSchema = new StringSchema()
                ._enum(Collections.singletonList(Crs.CRS84))
                ._default(Crs.CRS84)
                .description(DESCRIPTION_CRS);

        return new ObjectSchema()
                .addProperty("bbox", bboxSchema)
                .addProperty("crs", crsSchema)
                .description(DESCRIPTION);
    }

}
