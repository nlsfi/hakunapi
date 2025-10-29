package fi.nls.hakunapi.cql2.function.geometry;

import java.util.List;

import org.locationtech.jts.geom.Geometry;

import fi.nls.hakunapi.core.schemas.FunctionArgumentInfo.FunctionArgumentType;
import fi.nls.hakunapi.core.schemas.FunctionReturnsInfo.FunctionReturnsType;
import fi.nls.hakunapi.cql2.function.Function;
import fi.nls.hakunapi.cql2.model.FilterContext;

public class Buffer extends Function<FilterContext> {

    public Buffer() {
        super("Buffer", null, null);
        argument("geom", FunctionArgumentType.geometry);
        argument("radius_of_buffer", FunctionArgumentType.number);

        returns(FunctionReturnsType.geometry);
    }

    @Override
    public Object invoke(List<Object> args, FilterContext context) {
        Geometry geom = getGeometryArg(args, "geom");
        double radius_of_buffer = getNumberArg(args, "radius_of_buffer").doubleValue();
        if (context != null) {
            FilterContext fContext = (FilterContext) context;
            if (fContext.filterSrid().isDegrees()) {
                // TODO: Handle differently?
            }
        }
        return geom.buffer(radius_of_buffer);
    }

}