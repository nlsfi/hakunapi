package fi.nls.hakunapi.cql2.function.geometry;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.buffer.BufferParameters;

import fi.nls.hakunapi.core.schemas.FunctionArgumentInfo.FunctionArgumentType;
import fi.nls.hakunapi.core.schemas.FunctionReturnsInfo.FunctionReturnsType;
import fi.nls.hakunapi.cql2.function.Function;
import fi.nls.hakunapi.cql2.model.function.FunctionCall;

public class ST_Buffer extends Function {

    /*
     * reference: http://postgis.net/docs/manual-3.2/ST_Buffer.html
     * ST_Buffer(geom,radius_of_buffer,num_seg_quarter_circle,
     * buffer_style_parameters )
     * geom,radius_of_buffer,num_seg_quarter_circle,buffer_style_parameters
     */

    public ST_Buffer() {
        super("ST_Buffer", null, null);
        argument("geom", FunctionArgumentType.geometry);
        argument("radius_of_buffer", FunctionArgumentType.number);
        argument("buffer_style_parameters", FunctionArgumentType.string);

        returns(FunctionReturnsType.geometry);
    }

    @Override
    public Object visit(FunctionCall functionCall) {

        Geometry geom = toGeometry(functionCall, "geom");
        double radius_of_buffer = toNumber(functionCall, "radius_of_buffer").doubleValue();
        String buffer_style_parameters = toString(functionCall, "buffer_style_parameters");
        String[] parts = buffer_style_parameters.split(" ");
        final Map<String, String> kv = Stream.of(parts).filter(v -> !v.isEmpty()).map(elem -> elem.split("="))
                .filter(v -> v.length != 0).collect(Collectors.toMap(e -> e[0], e -> e[1]));

        int endCapStyle = mapBufferStyleParameter(kv);
        int numSeq = mapNumSegQuarterCircleParameter(kv);

        return geom.buffer(radius_of_buffer, numSeq, endCapStyle);
    }

    private int mapBufferStyleParameter(final Map<String, String> kv) {
        final String join = kv.getOrDefault("join", "round");// 'join=round|mitre|bevel'

        switch (join) {

        case "miter":
        case "mitre":
            return BufferParameters.JOIN_MITRE;
        case "bevel":
            return BufferParameters.JOIN_BEVEL;
        case "round":
        default:
            return BufferParameters.JOIN_ROUND;
        }
    }

    private int mapNumSegQuarterCircleParameter(final Map<String, String> kv) {
        return Integer.parseInt(kv.getOrDefault("quad_segs", "8")); // 'quad_segs=#' default 8
    }

}
