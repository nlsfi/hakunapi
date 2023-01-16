package fi.nls.hakunapi.filter;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.FilterParser;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.geom.Bbox;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;

public class JsonFilterExprParser implements FilterParser {

    public static final JsonFilterExprParser INSTANCE = new JsonFilterExprParser();

    private static final ObjectMapper OM = new ObjectMapper();
    private static final EnumSet<HakunaPropertyType> NUMERIC_TYPES = EnumSet.of(
            HakunaPropertyType.INT,
            HakunaPropertyType.LONG,
            HakunaPropertyType.FLOAT,
            HakunaPropertyType.DOUBLE
    );

    private JsonFilterExprParser() {
        // Use INSTANCE
    }

    @Override
    public String getCode() {
        return "json-filter-expr";
    }

    @Override
    public Filter parse(FeatureType ft, String json, int srid) throws IllegalArgumentException {
        List<Object> list;
        try {
            list = OM.readValue(json, List.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid input");
        }
        return toBooleanExpression(ft, list);
    }

    private Filter toBooleanExpression(FeatureType ft, List<Object> list) {
        String op = (String) list.get(0);

        if (isBasicComparisonOp(op)) {
            return toBasicComparisonExpression(ft, list);
        } else if (isSpecialComparisonOp(op)) {
            return toSpecialComparisonExpression(ft, list);
        } else if (isLogicalExpression(op)) {
            return toLogicalExpression(ft, list);
        } else if (isSpatialOp(op)) {
            return toSpatialExpression(ft, list);
        } else {
            throw new IllegalArgumentException("Unknown operation");
        }
    }

    private boolean isLogicalExpression(String op) {
        switch (op) {
        case "all":
        case "any":
        case "!":
            return true;
        default:
            return false;
        }
    }

    private Filter toLogicalExpression(FeatureType ft, List<Object> list) {
        String op = (String) list.get(0);
        switch (op) {
        case "all":
            return Filter.and(list.stream().skip(1)
                    .map(it -> toBooleanExpression(ft, (List<Object>) it))
                    .collect(Collectors.toList()));
        case "any":
            return Filter.or(list.stream().skip(1)
                    .map(it -> toBooleanExpression(ft, (List<Object>) it))
                    .collect(Collectors.toList()));
        case "!":
            return toBooleanExpression(ft, (List) list.get(1)).negate();
        }
        throw new IllegalArgumentException("Not Logical Expression!");
    }

    private boolean isBasicComparisonOp(String op) {
        switch (op) {
        case "==":
        case "!=":
        case ">":
        case ">=":
        case "<":
        case "<=":
            return true;
        default:
            return false;
        }
    }

    private Filter toBasicComparisonExpression(FeatureType ft, List<Object> expr) {
        String op = (String) expr.get(0);

        Object arg1 = expr.get(1);
        Object arg2 = expr.get(2);

        HakunaProperty prop;
        Object value;

        if (isGet(arg1)) {
            prop = toGet(ft, arg1);
            value = arg2;
        } else if (isSimpleValue(arg1)) {
            value = arg1;
            prop = toGet(ft, arg2);
        } else {
            throw new IllegalArgumentException("Unexpected argument for op " + op);
        }

        switch (op) {
        case "==":
            if (value == null) {
                return Filter.isNull(prop);
            }
            return Filter.equalTo(prop, value.toString());
        case "!=":
            if (value == null) {
                return Filter.isNotNull(prop);
            }
            return Filter.notEqualTo(prop, value.toString());
        }

        if (!NUMERIC_TYPES.contains(prop.getType()) || value == null || !(value instanceof Number)) {
            throw new IllegalArgumentException("Expected NumericExpression for op " + op);
        }

        switch (op) {
        case ">":
            return Filter.greaterThan(prop, value.toString());
        case ">=":
            return Filter.greaterThanOrEqualTo(prop, value.toString());
        case "<":
            return Filter.lessThan(prop, value.toString());
        case "<=":
            return Filter.lessThanThanOrEqualTo(prop, value.toString());
        }

        // We never end up here
        throw new IllegalArgumentException("Not Comparison Expression!");
    }
    
    private boolean isSpecialComparisonOp(String op) {
        switch (op) {
        case "like":
            return true;
        case "in":
            return true;
        default:
            return false;
        }
    }
    
    private Filter toSpecialComparisonExpression(FeatureType ft, List<Object> list) {
        String op = (String) list.get(0);
        switch (op) {
        case "like":
            return toLike(ft, list);
        }
        throw new IllegalArgumentException("Not Comparison Expression!");
    }

    private Filter toLike(FeatureType ft, List<Object> list) {
        if (!isGet(list.get(1))) {
            throw new IllegalArgumentException("Invalid like operation, expected get operation for first argument");
        }
        HakunaProperty prop = toGet(ft, list.get(1));
        Object arg2 = list.get(2);
        if (arg2 == null || !(arg2 instanceof String)) {
            throw new IllegalArgumentException("Invalid like operation, expected string value expression for second argument");
        }
        Object arg3 = list.size() > 3 ? list.get(3) : null;
        Map<String, Object> likeParams;
        if (arg3 == null) {
            likeParams = Collections.emptyMap();
        } else {
            if (arg3 instanceof Map) {
                likeParams = (Map<String, Object>) arg3;
            } else {
                throw new IllegalArgumentException("Invalid like operation, expected object for third argument");
            }
        }
        char wild = likeParams.getOrDefault("wildCard", "%").toString().charAt(0);
        char single = '_';
        char escape = '\\';
        boolean caseInsensitive = false;

        return Filter.like(prop, arg2.toString(), wild, single, escape, caseInsensitive);
    }
    
    private boolean isGet(Object expr) {
        if (!(expr instanceof List)) {
            return false;
        }
        List<Object> list = (List) expr;
        Object op = list.get(0);
        if ("get".equals(op)) {
            return true;
        }
        return false;
    }

    private HakunaProperty toGet(FeatureType ft, Object expr) {
        List<Object> list = (List) expr;
        String propertyName = (String) list.get(1);
        return ft.getQueryableProperties().stream()
                .filter(it -> it.getName().equals(propertyName))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Not queryable property " + propertyName));
    }

    private boolean isSimpleValue(Object expr) {
        if (expr == null) {
            return true;
        }
        if (expr instanceof Boolean) {
            return true;
        }
        if (expr instanceof Number) {
            return true;
        }
        if (expr instanceof String) {
            return true;
        }
        return false;
    }

    private boolean isSpatialOp(String op) {
        switch (op) {
        case "intersects":
            return true;
        default:
            return false;
        }
    }

    private Filter toSpatialExpression(FeatureType ft, List<Object> list) {
        String op = (String) list.get(0);
        Object arg1 = list.get(1);
        Object arg2 = list.get(2);

        Object geomArg;
        if (isGeometryOp(arg1)) {
            geomArg = arg2;
        } else if (isGeometryOp(arg2)) {
            geomArg = arg1;
        } else {
            throw new IllegalArgumentException("Expected at least one Geometry operation for op " + op);
        }
        HakunaPropertyGeometry prop = ft.getGeom();

        Geometry value;
        if (isBoundingBoxOp(geomArg)) {
            value = toBoundingBox(geomArg);
        } else {
            if (!(geomArg instanceof Map)) {
                throw new IllegalArgumentException("Expected at least one Geometry value for op " + op);
            }
            value = GeoJSONReader.toGeometry((Map<String, Object>) geomArg);
        }

        switch (op) {
        case "intersects":
            return Filter.intersects(prop, value);
        }
        throw new IllegalArgumentException("Not Spatial Expression!");
    }

    private boolean isGeometryOp(Object expression) {
        if (!(expression instanceof List)) {
            return false;
        }
        return "geometry".equals(((List) expression).get(0));
    }

    private boolean isBoundingBoxOp(Object expression) {
        if (!(expression instanceof List)) {
            return false;
        }
        return "bbox".equals(((List) expression).get(0));
    }

    private Geometry toBoundingBox(Object expr) {
        List<Object> list = (List) expr;
        if (list.size() != 5) {
            throw new IllegalArgumentException("Expected 4 arguments for op " + list.get(0).toString());
        }
        double[] bbox = list.stream().skip(1)
                .mapToDouble(it -> ((Number) it).doubleValue())
                .toArray();
        return Bbox.toGeometry(new Envelope(bbox[0], bbox[2], bbox[1], bbox[3]));
    }

}
