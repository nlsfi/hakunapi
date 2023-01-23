package fi.nls.hakunapi.cql2.model;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Geometry;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
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

public class ExpressionToHakunaFilter implements ExpressionVisitor {

    private final Map<String, HakunaProperty> queryables;

    public ExpressionToHakunaFilter(FeatureType ft) {
        this.queryables = ft.getQueryableProperties().stream()
                .collect(Collectors.toMap(HakunaProperty::getName, it -> it));
    }

    @Override
    public Object visit(And and) {
        return Filter
                .and(and.getChildren().stream().map(this::visit).map(Filter.class::cast).collect(Collectors.toList()));
    }

    @Override
    public Object visit(Or or) {
        return Filter
                .or(or.getChildren().stream().map(this::visit).map(Filter.class::cast).collect(Collectors.toList()));
    }

    @Override
    public Object visit(Not not) {
        return ((Filter) visit(not.getExpression())).negate();
    }

    @Override
    public Object visit(BinaryComparisonPredicate p) {
        HakunaProperty prop = visit(p.getProp());
        String value = (String) visit(p.getValue());

        switch (p.getOp()) {
        case EQ:
            return Filter.equalTo(prop, value);
        case NEQ:
            return Filter.notEqualTo(prop, value);
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
    public Object visit(LikePredicate p) {
        HakunaProperty prop = visit(p.getProperty());
        String pattern = p.getPattern();
        return Filter.like(prop, pattern, '%', '_', '\\', false);
    }

    @Override
    public Object visit(IsNullPredicate p) {
        return Filter.isNull(visit(p.getProperty()));
    }

    @Override
    public HakunaProperty visit(PropertyName p) {
        HakunaProperty prop = queryables.get(p.getValue());
        if (prop == null) {
            throw new IllegalArgumentException("Non queryable property " + p.getValue());
        }
        return prop;
    }

    @Override
    public Object visit(BooleanLiteral p) {
        return p.getValue() ? "true" : "false";
    }

    @Override
    public Object visit(NumberLiteral p) {
        return Double.toString(p.getValue());
    }

    @Override
    public Object visit(StringLiteral p) {
        return p.getValue();
    }

    @Override
    public Object visit(DateLiteral p) {
        return p.getDate().toString();
    }

    @Override
    public Object visit(TimestampLiteral p) {
        return p.getTimestamp().toString();
    }

    @Override
    public Object visit(SpatialPredicate p) {
        HakunaProperty property = visit(p.getProp());
        if (!(property instanceof HakunaPropertyGeometry)) {
            throw new IllegalArgumentException(
                    "Unexpected property type, " + p.getProp().getValue() + " is not a geometry property");
        }
        HakunaPropertyGeometry prop = (HakunaPropertyGeometry) property;

        Object geometry = visit(p.getValue());
        if (!(geometry instanceof Geometry)) {
            ExpressionToString toString = new ExpressionToString();
            toString.visit(p.getValue());
            String str = toString.finish();
            throw new IllegalArgumentException("Unexpected value, " + str + " could not be converted to a geometry");
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
    public Object visit(SpatialLiteral p) {
        return p.getGeometry();
    }

    @Override
    public Object visit(FunctionCall fn) {
        Optional<Function> function = CQL2Functions.INSTANCE.getAnyFunction(fn.getName());

        if (function.isPresent()) {
            return function.get().visit(fn);
        }
        throw new RuntimeException("Unmapped Function");

    }

}
