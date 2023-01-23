package fi.nls.hakunapi.core.param;

import org.locationtech.jts.geom.Geometry;

import fi.nls.hakunapi.core.WFS3Service;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import fi.nls.hakunapi.core.util.CrsUtil;
import fi.nls.hakunapi.core.util.FilterUtil;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.Parameter.StyleEnum;
import io.swagger.v3.oas.models.parameters.QueryParameter;

public class BboxCrsParam implements GetFeatureParam {

    @Override
    public String getParamName() {
        return "bbox-crs";
    }

    @Override
    public Parameter toParameter(WFS3Service service) {
        return new QueryParameter()
                .name(getParamName())
                .style(StyleEnum.FORM)
                .explode(false)
                .schema(new StringSchema()
                        .format("uri"));
    }

    @Override
    public void modify(WFS3Service service, GetFeatureRequest request, String value)
            throws IllegalArgumentException {
        if (value == null || value.isEmpty()) {
            return;
        }

        for (GetFeatureCollection collection : request.getCollections()) {
            HakunaPropertyGeometry geom = collection.getFt().getGeom();
            if (geom == null) {
                continue;
            }
            Filter bboxFilter = FilterUtil.findFilterByTag(collection.getFilters(), BboxParam.TAG);
            if (bboxFilter == null) {
                continue;
            }
            int srid = CrsUtil.parseSRID(value, getParamName());
            if (!geom.isSRIDSupported(srid)) {
                throw new IllegalArgumentException(CrsUtil.ERR_UNSUPPORTED_CRS);
            }
            Geometry bbox = (Geometry) bboxFilter.getValue();
            bbox.setSRID(srid);
        }
        
        request.addQueryParam(getParamName(), value);
    }

    @Override
    public int priority() {
        return 10; // Run after BboxParam
    }

}
