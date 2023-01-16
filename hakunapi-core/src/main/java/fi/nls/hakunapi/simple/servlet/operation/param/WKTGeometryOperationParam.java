package fi.nls.hakunapi.simple.servlet.operation.param;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.WFS3Service;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.geom.EWKTReader;
import fi.nls.hakunapi.core.geom.HakunaGeometryFactory;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import fi.nls.hakunapi.core.schemas.Crs;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.Parameter.StyleEnum;
import io.swagger.v3.oas.models.parameters.QueryParameter;

public class WKTGeometryOperationParam implements CustomizableGetFeatureParam {

    enum Operation {
        INTERSECTS
    }

    private FeatureType ft;
    private HakunaPropertyGeometry geom;
    private String name;
    private String desc;
    private Operation op;

    @Override
    public void init(HakunaProperty prop, String name, String desc, String arg) {
        if (!(prop instanceof HakunaPropertyGeometry)) {
            throw new IllegalArgumentException("WKTGeometryOperationParam only allowed for geometry properties!");
        }
        this.ft = prop.getFeatureType();
        this.geom = (HakunaPropertyGeometry) prop;
        this.name = name;
        this.desc = desc;
        this.op = Operation.valueOf(arg);
    }

    @Override
    public String getParamName() {
        return name;
    }

    @Override
    public boolean isCommon() {
        return false;
    }

    @Override
    public Parameter toParameter(WFS3Service service) {
        return new QueryParameter()
                .name(getParamName())
                .style(StyleEnum.FORM)
                .explode(false)
                .required(false)
                .description(desc)
                .schema(new StringSchema());
    }

    @Override
    public void modify(WFS3Service service, GetFeatureRequest request, String value) {
        if (value == null || value.isEmpty()) {
            return;
        }

        request.getCollections().stream()
        .filter(c -> c.getFt().getName().equals(ft.getName()))
        .findAny()
        .ifPresent(c -> {
            Filter f = toFilter(value);
            c.addFilter(f);
            request.addQueryParam(name, value);
        });
    }

    private Filter toFilter(String value) {
        try {
            EWKTReader reader = new EWKTReader(new WKTReader(HakunaGeometryFactory.GF));
            Geometry g = reader.read(value);
            if (g.getSRID() <= 0) {
                g.setSRID(Crs.CRS84_SRID);
            }
            return toFilter(g);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid WKT geometry");
        }
    }

    private Filter toFilter(Geometry g) {
        switch (op) {
        case INTERSECTS:
            return Filter.intersects(geom, g);
        }
        throw new IllegalArgumentException("Unhandled operation");
    }

}
