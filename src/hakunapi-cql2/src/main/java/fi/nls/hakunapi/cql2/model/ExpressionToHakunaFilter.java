package fi.nls.hakunapi.cql2.model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.locationtech.jts.geom.Geometry;

import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.core.schemas.FunctionArgumentInfo;
import fi.nls.hakunapi.core.schemas.FunctionArgumentInfo.FunctionArgumentType;
import fi.nls.hakunapi.cql2.function.CQL2Functions;
import fi.nls.hakunapi.cql2.function.Function;
import fi.nls.hakunapi.cql2.model.function.FunctionCall;
import fi.nls.hakunapi.cql2.model.literal.BooleanLiteral;
import fi.nls.hakunapi.cql2.model.literal.DateLiteral;
import fi.nls.hakunapi.cql2.model.literal.NumberLiteral;
import fi.nls.hakunapi.cql2.model.literal.PropertyName;
import fi.nls.hakunapi.cql2.model.literal.StringLiteral;
import fi.nls.hakunapi.cql2.model.literal.TimestampLiteral;
import fi.nls.hakunapi.cql2.model.logical.And;
import fi.nls.hakunapi.cql2.model.logical.Not;
import fi.nls.hakunapi.cql2.model.logical.Or;
import fi.nls.hakunapi.cql2.model.spatial.SpatialLiteral;
import fi.nls.hakunapi.cql2.model.spatial.SpatialPredicate;

public class ExpressionToHakunaFilter implements ExpressionVisitor<FilterContext> {

    @Override
    public Object visit(And and, FilterContext context) {
        return Filter
                .and(and.getChildren().stream().map(child -> visit(child, context)).map(Filter.class::cast).collect(Collectors.toList()));
    }

    @Override
    public Object visit(Or or, FilterContext context) {
        return Filter
                .or(or.getChildren().stream().map(child -> visit(child, context)).map(Filter.class::cast).collect(Collectors.toList()));
    }

    @Override
    public Object visit(Not not, FilterContext context) {
        return ((Filter) visit(not.getExpression(), context)).negate();
    }

    @Override
    public Object visit(BinaryComparisonPredicate p, FilterContext context) {
        PropertyName propertyName = p.getProp();
        Expression e = p.getValue();
        HakunaProperty prop = visit(propertyName, context);
        String value = (String) visit(e, context);
        boolean caseInsensitive = propertyName.isCasei() || e.isCasei();

        switch (p.getOp()) {
        case EQ:
            return Filter.equalTo(prop, value, caseInsensitive);
        case NEQ:
            return Filter.notEqualTo(prop, value, caseInsensitive);
        case LT:
            return Filter.lessThan(prop, value);
        case LTE:
            return Filter.lessThanThanOrEqualTo(prop, value);
        case GT:
            return Filter.greaterThan(prop, value);
        case GTE:
            return Filter.greaterThanOrEqualTo(prop, value);
        }

        throw new RuntimeException("Unmapped BinaryComparisonOperator, contact service administrator");
    }

    @Override
    public Object visit(LikePredicate p, FilterContext context) {
        PropertyName propertyName = p.getProperty();
        StringLiteral pattern = p.getPattern();
        HakunaProperty prop = visit(propertyName, context);
        String value = pattern.getValue();
        boolean caseInsensitive = propertyName.isCasei() || pattern.isCasei();
        return Filter.like(prop, value, '%', '_', '\\', caseInsensitive);
    }

    @Override
    public Object visit(IsNullPredicate p, FilterContext context) {
        return Filter.isNull(visit(p.getProperty(), context));
    }

    @Override
    public HakunaProperty visit(PropertyName p, FilterContext context) {
        return context.queryable(p.getValue())
            .orElseThrow(() -> new IllegalArgumentException("Non queryable property " + p.getValue()));
    }

    @Override
    public Object visit(BooleanLiteral p, FilterContext context) {
        return p.getValue() ? "true" : "false";
    }

    @Override
    public Object visit(NumberLiteral p, FilterContext context) {
        return Double.toString(p.getValue());
    }

    @Override
    public Object visit(StringLiteral p, FilterContext context) {
        return p.getValue();
    }

    @Override
    public Object visit(DateLiteral p, FilterContext context) {
        return p.getDate().toString();
    }

    @Override
    public Object visit(TimestampLiteral p, FilterContext context) {
        return p.getTimestamp().toString();
    }

    @Override
    public Object visit(SpatialPredicate p, FilterContext context) {
        HakunaProperty property = visit(p.getProp(), context);
        if (!(property instanceof HakunaPropertyGeometry)) {
            throw new IllegalArgumentException(
                    "Unexpected property type, " + p.getProp().getValue() + " is not a geometry property");
        }
        HakunaPropertyGeometry prop = (HakunaPropertyGeometry) property;

        Object geometry = visit(p.getValue(), context);
        if (!(geometry instanceof Geometry)) {
            throw new IllegalArgumentException("Expected geometry");
        }
        Geometry geom = (Geometry) geometry;

        switch (p.getOp()) {
        case S_INTERSECTS:
            return Filter.intersects(prop, geom);
        case S_CONTAINS:
            return Filter.contains(prop, geom);
        case S_CROSSES:
            return Filter.crosses(prop, geom);
        case S_DISJOINT:
            return Filter.disjoint(prop, geom);
        case S_EQUALS:
            return Filter.equals(prop, geom);
        case S_OVERLAPS:
            return Filter.overlaps(prop, geom);
        case S_TOUCHES:
            return Filter.touches(prop, geom);
        case S_WITHIN:
            return Filter.within(prop, geom);
        }

        throw new RuntimeException("Unmapped Geometry Comparison Operator, contact service administrator");
    }

    @Override
    public Object visit(SpatialLiteral p, FilterContext context) {
        return p.getGeometry();
    }

    @Override
    public Object visit(FunctionCall functionCall, FilterContext context) {
        List<Object> args = functionCall.getArgs().stream()
                .map(arg -> visit(arg, context))
                .collect(Collectors.toList());
        return CQL2Functions.INSTANCE.getAnyFunction(functionCall.getName())
            .map(f -> invokeFunction(f, args, context))
            .orElseThrow(() -> new RuntimeException("Unmapped Function"));
    }

    private static final Map<String, java.util.function.Function<Object, Object>> BACK_FROM_STRING = Map.of(
        FunctionArgumentType.number.name(), ExpressionToHakunaFilter::parseNumber
    );

    private static final Number parseNumber(Object o) {
        try {
            return Double.parseDouble(o.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object invokeFunction(Function f, List<Object> visitedArgs, FilterContext context) {
        // visit() transformed all literal values (except Geometries) to strings for Filter
        // => Convert them back as the Functions expect objects that match the arg type
        List<FunctionArgumentInfo> fArgs = f.getArguments();
        int n = Math.min(fArgs.size(), visitedArgs.size());
        List<Object> args = IntStream.range(0, n)
            .mapToObj(i -> {
                FunctionArgumentInfo fArg = fArgs.get(i);
                Object arg = visitedArgs.get(i);
                return BACK_FROM_STRING.getOrDefault(fArg.getType().get(0), o -> o).apply(arg);
            })
            .collect(Collectors.toList());
        return f.invoke(args, context);
    }

    @Override
    public Object visit(EmptyExpression ee, FilterContext context) {
        return Filter.PASS;
    }

}
