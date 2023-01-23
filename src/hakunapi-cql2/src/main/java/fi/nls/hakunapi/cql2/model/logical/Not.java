package fi.nls.hakunapi.cql2.model.logical;

import fi.nls.hakunapi.cql2.model.Expression;

public class Not implements LogicalExpression {

    private Expression expression;

    private Not(Expression expression) {
        this.expression = expression;
    }

    public static Expression from(Expression e) {
        if (e instanceof Not) {
            return ((Not) e).expression;
        } else {
            return new Not(e);
        }
    }

    public Expression getExpression() {
        return expression;
    }

}
