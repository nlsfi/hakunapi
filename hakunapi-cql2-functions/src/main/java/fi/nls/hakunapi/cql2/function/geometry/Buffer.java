package fi.nls.hakunapi.cql2.function.geometry;

import org.locationtech.jts.geom.Geometry;

import fi.nls.hakunapi.core.schemas.FunctionArgumentInfo.FunctionArgumentType;
import fi.nls.hakunapi.core.schemas.FunctionReturnsInfo.FunctionReturnsType;
import fi.nls.hakunapi.cql2.function.Function;
import fi.nls.hakunapi.cql2.model.function.FunctionCall;

public class Buffer extends Function {

    public Buffer() {
        super("Buffer", null, null);
        argument("geom", FunctionArgumentType.geometry);
        argument("radius_of_buffer", FunctionArgumentType.number);

        returns(FunctionReturnsType.geometry);
    }

    @Override
    public Object visit(FunctionCall functionCall) {
        Geometry geom = toGeometry(functionCall, "geom");
        double radius_of_buffer = toNumber(functionCall, "radius_of_buffer").doubleValue();
        return geom.buffer(radius_of_buffer);
    }

}