package fi.nls.hakunapi.filter;

import java.util.Date;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.geotools.filter.FilterCapabilities;
import org.geotools.filter.function.FilterFunction_buffer;
import org.locationtech.jts.geom.Geometry;
import org.opengis.filter.And;
import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.ExcludeFilter;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.Id;
import org.opengis.filter.IncludeFilter;
import org.opengis.filter.Not;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNil;
import org.opengis.filter.PropertyIsNotEqualTo;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.expression.Add;
import org.opengis.filter.expression.BinaryExpression;
import org.opengis.filter.expression.Divide;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.ExpressionVisitor;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.Multiply;
import org.opengis.filter.expression.NilExpression;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.expression.Subtract;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.Beyond;
import org.opengis.filter.spatial.BinarySpatialOperator;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.Crosses;
import org.opengis.filter.spatial.DWithin;
import org.opengis.filter.spatial.Disjoint;
import org.opengis.filter.spatial.Equals;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Touches;
import org.opengis.filter.spatial.Within;
import org.opengis.filter.temporal.After;
import org.opengis.filter.temporal.AnyInteracts;
import org.opengis.filter.temporal.Before;
import org.opengis.filter.temporal.Begins;
import org.opengis.filter.temporal.BegunBy;
import org.opengis.filter.temporal.During;
import org.opengis.filter.temporal.EndedBy;
import org.opengis.filter.temporal.Ends;
import org.opengis.filter.temporal.Meets;
import org.opengis.filter.temporal.MetBy;
import org.opengis.filter.temporal.OverlappedBy;
import org.opengis.filter.temporal.TContains;
import org.opengis.filter.temporal.TEquals;
import org.opengis.filter.temporal.TOverlaps;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.filter.ArithmeticPropertyExpression.Operation;

public class GeoToolsFilter2HakunaFilter implements FilterVisitor, ExpressionVisitor {

    private FeatureType ft;
    private int filterSrid;

    public GeoToolsFilter2HakunaFilter(FeatureType ft, int filterSrid) {
        this.ft = ft;
        this.filterSrid = filterSrid;
    }

    public Filter map(org.opengis.filter.Filter filter) {
        if (!createFilterCapabilities().fullySupports(filter)) {
            throw new IllegalArgumentException("Filter type not supported");
        }
        return (Filter) filter.accept(this, null);
    }

    private FilterCapabilities createFilterCapabilities() {
        FilterCapabilities capabilities = new FilterCapabilities();

        capabilities.addType(IncludeFilter.class);
        capabilities.addType(ExcludeFilter.class);

        capabilities.addType(PropertyName.class);
        capabilities.addType(Literal.class);

        capabilities.addAll(FilterCapabilities.LOGICAL_OPENGIS);
        capabilities.addAll(FilterCapabilities.SIMPLE_COMPARISONS_OPENGIS);

        capabilities.addType(Add.class);
        capabilities.addType(Subtract.class);
        capabilities.addType(Divide.class);
        capabilities.addType(Multiply.class);

        capabilities.addType(PropertyIsLike.class);
        capabilities.addType(PropertyIsNull.class);
        capabilities.addType(PropertyIsBetween.class);
        capabilities.addType(Id.class);

        capabilities.addType(BBOX.class);
        capabilities.addType(Intersects.class);
        capabilities.addType(Contains.class);
        capabilities.addType(Crosses.class);
        capabilities.addType(Disjoint.class);
        capabilities.addType(Equals.class);
        capabilities.addType(Overlaps.class);
        capabilities.addType(Touches.class);
        capabilities.addType(Within.class);

        /*
        capabilities.addType(AnyInteracts.class);
        capabilities.addType(Before.class);
        capabilities.addType(After.class);
        capabilities.addType(Meets.class);
        capabilities.addType(MetBy.class);
        capabilities.addType(TOverlaps.class);
        capabilities.addType(OverlappedBy.class);
        capabilities.addType(Begins.class);
        capabilities.addType(BegunBy.class);
        capabilities.addType(During.class);
        capabilities.addType(TContains.class);
        capabilities.addType(Ends.class);
        capabilities.addType(EndedBy.class);
        capabilities.addType(TEquals.class);
        */

        capabilities.addType(FilterFunction_buffer.class);

        return capabilities;
    }

    @Override
    public Object visit(ExcludeFilter filter, Object data) {
        return Filter.DENY;
    }

    @Override
    public Object visit(IncludeFilter filter, Object data) {
        return Filter.PASS;
    }

    @Override
    public HakunaProperty visit(PropertyName expr, Object data) {
        for (HakunaProperty queryable : ft.getQueryableProperties()) {
            if (expr.getPropertyName().equals(queryable.getName())) {
                return queryable;
            }
        }
        throw new IllegalArgumentException("Not queryable property " + expr.getPropertyName());
    }

    @Override
    public Object visit(Literal expr, Object data) {
        Object value = expr.getValue();
        if (value instanceof Date) {
            value = ((Date) value).toInstant();
        }
        return value;
    }

    @Override
    public Object visit(NilExpression expr, Object data) {
        return null;
    }

    @Override
    public Object visit(Function expr, Object data) {
        throw new UnsupportedOperationException("Unsupported function");
    }

    @Override
    public Object visit(Add expr, Object data) {
        return visitArithmeticExpression(expr, data, Operation.ADD);
    }

    @Override
    public Object visit(Subtract expr, Object data) {
        return visitArithmeticExpression(expr, data, Operation.SUBTRACT);
    }

    @Override
    public Object visit(Divide expr, Object data) {
        return visitArithmeticExpression(expr, data, Operation.DIVIDE);
    }

    @Override
    public Object visit(Multiply expr, Object data) {
        return visitArithmeticExpression(expr, data, Operation.MULTIPLY);
    }

    private Object visitArithmeticExpression(BinaryExpression expr, Object data, Operation op) {
        if (isLiteralOrResolvableArithmeticExpression(expr)) {
            return expr.evaluate(null);
        }

        Expression e1 = expr.getExpression1();
        Expression e2 = expr.getExpression2();

        Expression property;
        Object literal;
        boolean propertyIsLeft;

        if (isPropertyNameArithmeticExpression(e1) && isLiteralOrResolvableArithmeticExpression(e2)) {
            property = e1;
            literal = e2.accept(this, null);
            propertyIsLeft = true;
        } else if (isLiteralOrResolvableArithmeticExpression(e1) && isPropertyNameArithmeticExpression(e2)) {
            literal = e1.accept(this, null);
            property = e2;
            propertyIsLeft = false;
        } else {
            throw new UnsupportedOperationException("Too complex arithmetic expression");
        }

        if (literal instanceof Number) {
            return new ArithmeticPropertyExpression(op, property, (Number) literal, propertyIsLeft);
        } else {
            throw new IllegalArgumentException("Arithmetic operations only work with numbers");
        }
    }

    private static boolean isPropertyNameArithmeticExpression(Expression e) {
        if (e instanceof PropertyName) {
            return true;
        }
        if (!(e instanceof BinaryExpression)) {
            return false;
        }
        BinaryExpression be = (BinaryExpression) e;
        if (isArithmeticExpression(be)) {
            Expression e1 = be.getExpression1();
            Expression e2 = be.getExpression2();
            if (isPropertyNameArithmeticExpression(e1) && isLiteralOrResolvableArithmeticExpression(e2)) {
                return true;
            } else if (isLiteralOrResolvableArithmeticExpression(e1) && isPropertyNameArithmeticExpression(e2)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isLiteralOrResolvableArithmeticExpression(Expression e) {
        if (e instanceof Literal) {
            return true;
        }
        if (!(e instanceof BinaryExpression)) {
            return false;
        }
        BinaryExpression be = (BinaryExpression) e;
        if (isArithmeticExpression(be)) {
            Expression e1 = be.getExpression1();
            Expression e2 = be.getExpression2();
            if (isLiteralOrResolvableArithmeticExpression(e1) && isLiteralOrResolvableArithmeticExpression(e2)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isArithmeticExpression(BinaryExpression e) {
        return e instanceof Add || e instanceof Subtract || e instanceof Divide || e instanceof Multiply;
    }

    @Override
    public Object visit(And filter, Object data) {
        return Filter.and(filter.getChildren().stream()
                .map(sub -> sub.accept(this, data))
                .map(Filter.class::cast)
                .distinct()
                .collect(Collectors.toList()));
    }

    @Override
    public Object visit(Or filter, Object data) {
        return Filter.or(filter.getChildren().stream()
                .map(sub -> sub.accept(this, data))
                .map(Filter.class::cast)
                .distinct()
                .collect(Collectors.toList()));
    }

    @Override
    public Object visit(Not filter, Object data) {
        return ((Filter) filter.getFilter().accept(this, data)).negate();
    }

    @Override
    public Object visit(PropertyIsBetween filter, Object data) {
        Expression expr = filter.getExpression();
        Expression lb = filter.getLowerBoundary();
        Expression ub = filter.getUpperBoundary();

        HakunaProperty prop = (HakunaProperty) expr.accept(this, null);
        Object lower = lb.accept(this, null);
        Object upper = ub.accept(this, null);

        return Filter.and(
                Filter.greaterThanOrEqualTo(prop, lower.toString()),
                Filter.lessThanThanOrEqualTo(prop, upper.toString())
        );
    }

    @Override
    public Object visit(PropertyIsEqualTo filter, Object data) {
        return visit((BinaryComparisonOperator) filter, data, Filter::equalTo);
    }

    @Override
    public Object visit(PropertyIsNotEqualTo filter, Object data) {
        return visit((BinaryComparisonOperator) filter, data, Filter::notEqualTo);
    }

    @Override
    public Object visit(PropertyIsGreaterThan filter, Object data) {
        return visit((BinaryComparisonOperator) filter, data, Filter::greaterThan);
    }

    @Override
    public Object visit(PropertyIsGreaterThanOrEqualTo filter, Object data) {
        return visit((BinaryComparisonOperator) filter, data, Filter::greaterThanOrEqualTo);
    }

    @Override
    public Object visit(PropertyIsLessThan filter, Object data) {
        return visit((BinaryComparisonOperator) filter, data, Filter::lessThan);
    }

    @Override
    public Object visit(PropertyIsLessThanOrEqualTo filter, Object data) {
        return visit((BinaryComparisonOperator) filter, data, Filter::lessThanThanOrEqualTo);
    }

    private Object visit(BinaryComparisonOperator filter, Object data, BiFunction<HakunaProperty, String, Filter> type) {
        Expression e1 = filter.getExpression1();
        Expression e2 = filter.getExpression2();

        if (isLiteralOrResolvableArithmeticExpression(e1) && isLiteralOrResolvableArithmeticExpression(e2)) {
            return filter.evaluate(null) ? Filter.PASS : Filter.DENY;
        }

        Expression prop;
        Object literal;
        boolean propWasLeft;

        if (isPropertyNameArithmeticExpression(e1) && isLiteralOrResolvableArithmeticExpression(e2)) {
            prop = e1;
            literal = e2.accept(this, null);
            propWasLeft = true;
        } else if (isLiteralOrResolvableArithmeticExpression(e1) && isPropertyNameArithmeticExpression(e2)) {
            literal = e1.accept(this, null);
            prop = e2;
            propWasLeft = false;
        } else {
            throw new UnsupportedOperationException("Filter not supported");
        }

        HakunaProperty property;
        while (true) {
            if (prop instanceof PropertyName) {
                property = (HakunaProperty) ((PropertyName) prop).accept(this, null);
                break;
            }
            ArithmeticPropertyExpression ap = (ArithmeticPropertyExpression) prop.accept(this, null);
            Number literalNumber = (Number) literal;
            if (!ap.getOperation().isSymmetric() && !ap.isPropertyLeft()) { // 100 / foo < 30 <=> 100 < 30 * foo <=> 100 / 30 < foo
                propWasLeft = !propWasLeft;
                literal = ap.getOperation().negate().swapToOtherSide(literalNumber, ap.getLiteral());
            } else {
                literal = ap.getOperation().swapToOtherSide(ap.getLiteral(), literalNumber);
            }
            prop = ap.getProperty();
        }

        Filter hakunaFilter = type.apply(property, literal.toString());
        return propWasLeft ? hakunaFilter : hakunaFilter.inverse();
    }

    @Override
    public Object visit(PropertyIsLike filter, Object data) {
        HakunaProperty prop = (HakunaProperty) filter.getExpression().accept(this, null);
        String value = filter.getLiteral();
        char wild = filter.getWildCard().charAt(0);
        char single = filter.getSingleChar().charAt(0);
        char escape = filter.getEscape().charAt(0);
        boolean caseInsensitive = !filter.isMatchingCase();
        return Filter.like(prop, value, wild, single, escape, caseInsensitive);
    }

    @Override
    public Object visit(PropertyIsNull filter, Object data) {
        Expression e = filter.getExpression();
        Object o = e.accept(this, null);
        if (o instanceof HakunaProperty) {
            return Filter.isNull((HakunaProperty) o);
        }
        return o == null ? Filter.PASS : Filter.DENY;
    }

    @Override
    public Object visit(PropertyIsNil filter, Object data) {
        throw new UnsupportedOperationException("isNil not supported");
    }

    @Override
    public Object visit(Id filter, Object data) {
        return Filter.or(filter.getIDs().stream()
                .map(id -> Filter.equalTo(ft.getId(), id.toString()))
                .collect(Collectors.toList()));
    }

    @Override
    public Object visit(BBOX filter, Object data) {
        return visit((BinarySpatialOperator) filter, data, Filter::intersects);
    }

    @Override
    public Object visit(Intersects filter, Object data) {
        return visit((BinarySpatialOperator) filter, data, Filter::intersects);
    }

    @Override
    public Object visit(Contains filter, Object data) {
        return visit((BinarySpatialOperator) filter, data, Filter::contains);
    }

    @Override
    public Object visit(Crosses filter, Object data) {
        return visit((BinarySpatialOperator) filter, data, Filter::crosses);
    }

    @Override
    public Object visit(Disjoint filter, Object data) {
        return visit((BinarySpatialOperator) filter, data, Filter::disjoint);
    }

    @Override
    public Object visit(Equals filter, Object data) {
        return visit((BinarySpatialOperator) filter, data, Filter::equals);
    }

    @Override
    public Object visit(Overlaps filter, Object data) {
        return visit((BinarySpatialOperator) filter, data, Filter::overlaps);
    }

    @Override
    public Object visit(Touches filter, Object data) {
        return visit((BinarySpatialOperator) filter, data, Filter::touches);
    }

    @Override
    public Object visit(Within filter, Object data) {
        return visit((BinarySpatialOperator) filter, data, Filter::within);
    }

    @Override
    public Object visit(Beyond filter, Object data) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(DWithin filter, Object data) {
        throw new UnsupportedOperationException("Not supported");
    }

    private Object visit(BinarySpatialOperator filter, Object data, BiFunction<HakunaPropertyGeometry, Geometry, Filter> type) {
        Expression e1 = filter.getExpression1();
        Expression e2 = filter.getExpression2();
        boolean leftIsProperty = true;

        if (isLiteralOrBufferedGeometry(e1) && e2 instanceof PropertyName) {
            e1 = filter.getExpression2();
            e2 = filter.getExpression1();
            leftIsProperty = false;
        }

        if (e1 instanceof PropertyName && isLiteralOrBufferedGeometry(e2)) {
            HakunaProperty prop = (HakunaProperty) e1.accept(this, null);
            if (prop.getType() != HakunaPropertyType.GEOMETRY) {
                throw new IllegalArgumentException("Property not geometry property!");
            }
            Geometry geom = e2.evaluate(null, Geometry.class);
            geom.setSRID(filterSrid);
            Filter f = type.apply((HakunaPropertyGeometry) prop, geom);
            return leftIsProperty ? f : f.inverse();
        }

        throw new UnsupportedOperationException("Not supported");
    }

    private boolean isLiteralOrBufferedGeometry(Expression e) {
        return e instanceof Literal || e instanceof FilterFunction_buffer;
    }

    @Override
    public Object visit(AnyInteracts filter, Object data) {
        throw new UnsupportedOperationException("anyinteracts not supported");
    }

    @Override
    public Object visit(Before filter, Object data) {
        throw new UnsupportedOperationException("before not supported");
    }

    @Override
    public Object visit(After filter, Object data) {
        throw new UnsupportedOperationException("after not supported");
    }

    @Override
    public Object visit(Meets filter, Object data) {
        throw new UnsupportedOperationException("meets not supported");
    }

    @Override
    public Object visit(MetBy filter, Object data) {
        throw new UnsupportedOperationException("metby not supported");
    }

    @Override
    public Object visit(TOverlaps filter, Object data) {
        throw new UnsupportedOperationException("toverlaps not supported");
    }

    @Override
    public Object visit(OverlappedBy filter, Object data) {
        throw new UnsupportedOperationException("overlappedby not supported");
    }

    @Override
    public Object visit(Begins filter, Object data) {
        throw new UnsupportedOperationException("begins not supported");
    }

    @Override
    public Object visit(BegunBy filter, Object data) {
        throw new UnsupportedOperationException("begunby not supported");
    }

    @Override
    public Object visit(During filter, Object data) {
        throw new UnsupportedOperationException("during not supported");
    }

    @Override
    public Object visit(TContains filter, Object data) {
        throw new UnsupportedOperationException("tcontains not supported");
    }

    @Override
    public Object visit(Ends filter, Object data) {
        throw new UnsupportedOperationException("ends not supported");
    }

    @Override
    public Object visit(EndedBy filter, Object data) {
        throw new UnsupportedOperationException("endedby not supported");
    }

    @Override
    public Object visit(TEquals filter, Object data) {
        throw new UnsupportedOperationException("tequals not supported");
    }

    @Override
    public Object visitNullFilter(Object data) {
        return data;
    }

}
