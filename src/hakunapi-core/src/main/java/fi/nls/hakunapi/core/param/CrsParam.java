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

public class CrsParam implements GetFeatureParam {

    private static final String CRS_HEADER = "Content-Crs";

    @Override
    public String getParamName() {
        return "crs";
    }

    @Override
    public Parameter toParameter(FeatureServiceConfig service) {
        return new QueryParameter()
                .name(getParamName())
                .style(StyleEnum.FORM)
                .explode(false)
                .schema(new StringSchema()
                        .format("uri"));
    }

    @Override
    public void modify(FeatureServiceConfig service, GetFeatureRequest request, String value)
            throws IllegalArgumentException {
        if (value != null && !value.isEmpty()) {
            int srid = CrsUtil.parseSRID(value, getParamName());
            for (GetFeatureCollection c : request.getCollections()) {
                HakunaPropertyGeometry geom = c.getFt().getGeom();
                if (geom != null && !geom.isSRIDSupported(srid)) {
                    throw new IllegalArgumentException(CrsUtil.ERR_UNSUPPORTED_CRS);
                }
            }
            request.setSRID(srid);
            request.addQueryParam(getParamName(), value);
        }
        request.addResponseHeader(CRS_HEADER, getCrsValue(request.getSRID()));
    }

    private String getCrsValue(int srid) {
        return getCrsValue(CrsUtil.toUri(srid));
    }

    private String getCrsValue(String crsUri) {
        return "<" + crsUri + ">";
    }

}
