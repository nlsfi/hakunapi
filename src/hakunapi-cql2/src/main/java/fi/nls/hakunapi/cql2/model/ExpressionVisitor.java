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

    default public Object visit(Expression e) {
        if (e instanceof BinaryComparisonPredicate) {
            return visit((BinaryComparisonPredicate) e);
        } else if (e instanceof LikePredicate) {
            return visit((LikePredicate) e);
        } else if (e instanceof IsNullPredicate) {
            return visit((IsNullPredicate) e);
        } else if (e instanceof BooleanLiteral) {
            return visit((BooleanLiteral) e);
        } else if (e instanceof NumberLiteral) {
            return visit((NumberLiteral) e);
        } else if (e instanceof StringLiteral) {
            return visit((StringLiteral) e);
        } else if (e instanceof DateLiteral) {
            return visit((DateLiteral) e);
        } else if (e instanceof TimestampLiteral) {
            return visit((TimestampLiteral) e);
        } else if (e instanceof PropertyName) {
            return visit((PropertyName) e);
        } else if (e instanceof And) {
            return visit((And) e);
        } else if (e instanceof Or) {
            return visit((Or) e);
        } else if (e instanceof Not) {
            return visit((Not) e);
        } else if (e instanceof SpatialPredicate) {
            return visit((SpatialPredicate) e);
        } else if (e instanceof SpatialLiteral) {
            return visit((SpatialLiteral) e);
        } else if (e instanceof FunctionCall) {
            return visit((FunctionCall) e);
        } else if (e instanceof EmptyExpression) {
            return visit((EmptyExpression) e);
        }

        throw new IllegalStateException("Unknown Expression " + e.getClass());
    }

    public Object visit(And and);
    public Object visit(Or or);
    public Object visit(Not not);

    public Object visit(SpatialPredicate p);
    public Object visit(SpatialLiteral p);

    public Object visit(BinaryComparisonPredicate p);
    public Object visit(LikePredicate p);
    public Object visit(IsNullPredicate p);
    
    public Object visit(PropertyName p);

    public Object visit(BooleanLiteral p);
    public Object visit(NumberLiteral p);
    public Object visit(StringLiteral p);
    public Object visit(DateLiteral p);
    public Object visit(TimestampLiteral p);

    public Object visit(FunctionCall fn);
    public Object visit(EmptyExpression ee);

}
