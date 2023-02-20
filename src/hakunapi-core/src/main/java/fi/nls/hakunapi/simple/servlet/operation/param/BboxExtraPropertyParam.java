package fi.nls.hakunapi.simple.servlet.operation.param;

import org.locationtech.jts.geom.Geometry;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.param.BboxParam;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import fi.nls.hakunapi.core.util.FilterUtil;
import io.swagger.v3.oas.models.parameters.Parameter;

public class BboxExtraPropertyParam implements CustomizableGetFeatureParam {

    private FeatureType ft;
    private HakunaPropertyGeometry geom;

    @Override
    public void init(HakunaProperty prop, String name, String desc, String arg) {
        if (!(prop instanceof HakunaPropertyGeometry)) {
            throw new IllegalArgumentException("WKTGeometryOperationParam only allowed for geometry properties!");
        }
        this.ft = prop.getFeatureType();
        this.geom = (HakunaPropertyGeometry) prop;
    }

    @Override
    public String getParamName() {
        return "bbox";
    }

    @Override
    public Parameter toParameter(FeatureServiceConfig service) {
        // This isn't visible to the API document, it only adds to the actual bbox implementation
        return null;
    }

    @Override
    public void modify(FeatureServiceConfig service, GetFeatureRequest request, String value) throws IllegalArgumentException {
        if (value == null || value.isEmpty()) {
            return;
        }

        request.getCollections().stream()
        .filter(c -> c.getFt().getName().equals(ft.getName()))
        .findAny()
        .ifPresent(c -> {
            Filter bboxFilter = FilterUtil.findFilterByTag(c.getFilters(), BboxParam.TAG);
            if (bboxFilter == null) {
                return;
            }
            Geometry bbox = (Geometry) bboxFilter.getValue();
            Filter intersectsIndex = Filter.intersectsIndex(geom, bbox);
            c.addFilter(intersectsIndex);
        });
    }

}
