package fi.nls.hakunapi.core.param;

import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import fi.nls.hakunapi.core.util.CrsUtil;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.Parameter.StyleEnum;
import io.swagger.v3.oas.models.parameters.QueryParameter;

public class FilterCrsParam implements GetFeatureParam {

    private static final String DESCRIPTION = "Speficy filter crs";

    @Override
    public String getParamName() {
        return "filter-crs";
    }

    @Override
    public Parameter toParameter(FeatureServiceConfig service) {
        return new QueryParameter()
                .name(getParamName())
                .style(StyleEnum.FORM)
                .explode(false)
                .required(false)
                .description(DESCRIPTION)
                .schema(new StringSchema().format("uri-reference"));
    }

    @Override
    public void modify(FeatureServiceConfig service, GetFeatureRequest request, String value) throws IllegalArgumentException {
        if (value == null || value.isEmpty()) {
            return;
        }

        int srid = CrsUtil.parseSRID(value, getParamName());
        for (GetFeatureCollection collection : request.getCollections()) {
            HakunaPropertyGeometry geom = collection.getFt().getGeom();
            if (geom != null && !geom.isSRIDSupported(srid)) {
                throw new IllegalArgumentException(CrsUtil.ERR_UNSUPPORTED_CRS);
            }
        }

        request.setFilterSrid(srid);
        request.addQueryParam(getParamName(), value);
    }
    
    @Override
    public int priority() {
        return -1; // Run before FilterParam!
    }

}
