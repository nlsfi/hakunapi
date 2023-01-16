package fi.nls.hakunapi.cql2.model;

public interface Expression {

    default public Object accept(ExpressionVisitor visitor) {
        return visitor.visit(this);
    }

}
