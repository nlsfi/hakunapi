package fi.nls.hakunapi.cql2.model;

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

public interface ExpressionVisitor {

    default public Object visit(Expression e, Object context) {
        if (e instanceof BinaryComparisonPredicate) {
            return visit((BinaryComparisonPredicate) e, context);
        } else if (e instanceof LikePredicate) {
            return visit((LikePredicate) e, context);
        } else if (e instanceof IsNullPredicate) {
            return visit((IsNullPredicate) e, context);
        } else if (e instanceof BooleanLiteral) {
            return visit((BooleanLiteral) e, context);
        } else if (e instanceof NumberLiteral) {
            return visit((NumberLiteral) e, context);
        } else if (e instanceof StringLiteral) {
            return visit((StringLiteral) e, context);
        } else if (e instanceof DateLiteral) {
            return visit((DateLiteral) e, context);
        } else if (e instanceof TimestampLiteral) {
            return visit((TimestampLiteral) e, context);
        } else if (e instanceof PropertyName) {
            return visit((PropertyName) e, context);
        } else if (e instanceof And) {
            return visit((And) e, context);
        } else if (e instanceof Or) {
            return visit((Or) e, context);
        } else if (e instanceof Not) {
            return visit((Not) e, context);
        } else if (e instanceof SpatialPredicate) {
            return visit((SpatialPredicate) e, context);
        } else if (e instanceof SpatialLiteral) {
            return visit((SpatialLiteral) e, context);
        } else if (e instanceof FunctionCall) {
            return visit((FunctionCall) e, context);
        } else if (e instanceof EmptyExpression) {
            return visit((EmptyExpression) e, context);
        }

        throw new IllegalStateException("Unknown Expression " + e.getClass());
    }

    public Object visit(And and, Object context);
    public Object visit(Or or, Object context);
    public Object visit(Not not, Object context);

    public Object visit(SpatialPredicate p, Object context);
    public Object visit(SpatialLiteral p, Object context);

    public Object visit(BinaryComparisonPredicate p, Object context);
    public Object visit(LikePredicate p, Object context);
    public Object visit(IsNullPredicate p, Object context);
    
    public Object visit(PropertyName p, Object context);

    public Object visit(BooleanLiteral p, Object context);
    public Object visit(NumberLiteral p, Object context);
    public Object visit(StringLiteral p, Object context);
    public Object visit(DateLiteral p, Object context);
    public Object visit(TimestampLiteral p, Object context);

    public Object visit(FunctionCall fn, Object context);
    public Object visit(EmptyExpression ee, Object context);

}
