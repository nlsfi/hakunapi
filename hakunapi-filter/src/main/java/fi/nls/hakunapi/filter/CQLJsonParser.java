package fi.nls.hakunapi.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.FilterParser;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.property.HakunaProperty;

public class CQLJsonParser implements FilterParser {

    public static final CQLJsonParser INSTANCE = new CQLJsonParser();

    private static final ObjectMapper OM = new ObjectMapper();
    
    private CQLJsonParser() {
        // Use INSTANCE
    }

    @Override
    public String getCode() {
        return "cql-json-array";
    }

    @Override
    public Filter parse(FeatureType ft, String json, int filterSrid) throws IllegalArgumentException {
        List<Object> list;
        try {
            list = OM.readValue(json, List.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid input");
        }
        return getLogicalExpression(ft, list);
    }

    private Filter getLogicalExpression(FeatureType ft, List<Object> list) {
        String op = (String) list.get(0);

        if (isComparisonOp(op)) {
            return getComparisonOp(ft, op, list.get(1), list.get(2));
        }

        switch (op) {
        case "and":
            return Filter.and(list.stream().skip(1)
                    .map(it -> getLogicalExpression(ft, (List<Object>) it))
                    .collect(Collectors.toList()));
        case "or":
            return Filter.or(list.stream().skip(1)
                    .map(it -> getLogicalExpression(ft, (List<Object>) it))
                    .collect(Collectors.toList()));
        case "not":
            return getLogicalExpression(ft, (List) list.get(1)).negate();
        case "between":
            return getBetween(ft, list.get(1), list.get(2), list.get(3));
        case "like":
            return like(ft, list.subList(1, list.size()));
        case "in":
            return in(ft, list.subList(1, list.size()));
        }
        return null;
    }

    private Filter in(FeatureType ft, List<Object> arguments) {
        HakunaProperty prop = getProperty(ft, arguments.get(0));
        if (prop == null) {
            throw new IllegalArgumentException("in first argument must be property");
        }
        List<Filter> or = new ArrayList<>();
        for (int i = 1; i < arguments.size(); i++) {
            Object v = arguments.get(i);
            String value = toScalarValue(v);
            if (value != null) {
                or.add(Filter.equalTo(prop, value));
            }
        }
        return Filter.or(or);
    }

    private Filter getBetween(FeatureType ft, Object property, Object lower, Object upper) {
        HakunaProperty prop = getProperty(ft, property);
        if (prop == null) {
            throw new IllegalArgumentException("between first argument must be property");
        }
        if (lower == null || upper == null) {
            throw new IllegalArgumentException("null not allowed value for between");
        }
        String _lower = toScalarValue(lower);
        if (_lower == null) {
            throw new IllegalArgumentException("Invalid lower value for between");
        }
        String _upper = toScalarValue(upper);
        if (_upper == null) {
            throw new IllegalArgumentException("Invalid upper value for between");
        }
        Filter gte = Filter.greaterThanOrEqualTo(prop, _lower);
        Filter lte = Filter.lessThanThanOrEqualTo(prop, _upper);
        return Filter.and(gte, lte);
    }

    private Filter like(FeatureType ft, List<Object> arguments) {
        HakunaProperty prop = getProperty(ft, arguments.get(0));
        String value = (String) arguments.get(1);
        Object arg3 = arguments.size() > 2 ? arguments.get(2) : null;
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
        
        return Filter.like(prop, value, wild, single, escape, caseInsensitive);
    }

    private boolean isComparisonOp(String op) {
        switch (op) {
        case "=":
        case "eq":
        case "<>":
        case "neq":
        case ">":
        case "gt":
        case ">=":
        case "gte":
        case "<":
        case "lt":
        case "<=":
        case "lte":
            return true;
        default:
            return false;
        }
    }

    private Filter getComparisonOp(FeatureType ft, String op, Object operand1, Object operand2) {
        HakunaProperty prop = getProperty(ft, operand1);
        String value = toScalarValue(operand2); 
        if (prop == null) {
            prop = getProperty(ft, operand2);
            value = toScalarValue(operand1);
        }

        switch (op) {
        case "=":
        case "eq":
            if (value == null) {
                return Filter.isNull(prop);
            }
            return Filter.equalTo(prop, value);
        case "<>":
        case "neq":
            if (value == null) {
                return Filter.isNotNull(prop);
            }
            return Filter.notEqualTo(prop, value);
        }

        if (value == null) {
            throw new IllegalArgumentException("null not possible value for operation " + op);
        }

        switch (op) {
        case ">":
        case "gt":
            return Filter.greaterThan(prop, value);
        case ">=":
        case "gte":
            return Filter.greaterThanOrEqualTo(prop, value);
        case "<":
        case "lt":
            return Filter.lessThan(prop, value);
        case "<=":
        case "lte":
            return Filter.lessThanThanOrEqualTo(prop, value);
        }
        
        // We never end up here
        return null;
    }

    private HakunaProperty getProperty(FeatureType ft, Object operand) {
        if (operand instanceof List) {
            List<Object> list = (List) operand;
            Object op = list.get(0);
            if ("id".equals(op)) {
                return ft.getId();
            } else if ("geometry".equals(op)) {
                return ft.getGeom();
            } else if ("property".equals(list.get(0))) {
                String propertyName = (String) list.get(1);
                return ft.getQueryableProperties().stream()
                        .filter(it -> it.getName().equals(propertyName))
                        .findAny()
                        .orElse(null);
            }
        }
        return null;
    }

    private String toScalarValue(Object operand) {
        if (operand == null) {
            return null;
        }
        if (operand instanceof String) {
            return (String) operand;
        }
        if (operand instanceof Boolean) {
            return operand.toString();
        }
        if (operand instanceof Number) {
            return operand.toString();
        }
        return null;
    }

}
