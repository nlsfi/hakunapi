package fi.nls.hakunapi.cql2.model;

public interface Expression {

    default public Object accept(ExpressionVisitor visitor, Object context) {
        return visitor.visit(this, context);
    }

    default public boolean isCasei() {
        return false;
    }

}
