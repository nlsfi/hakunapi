package fi.nls.hakunapi.cql2.model.logical;

import java.util.ArrayList;
import java.util.List;

import fi.nls.hakunapi.cql2.model.Expression;

public class Or implements LogicalExpression {

    private final List<Expression> children;

    private Or() {
        this(new ArrayList<>());
    }

    private Or(List<Expression> children) {
        this.children = children;
    }

    public static Or from(Expression left, Expression right) {
        if (left instanceof Or && right instanceof Or) {
            ((Or) left).children.addAll(((Or) right).children);
            return (Or) left;
        } else if (left instanceof Or)  {
            ((Or) left).children.add(right);
            return (Or) left;
        } else if (right instanceof Or) {
            ((Or) right).children.add(left);
            return (Or) right;
        } else {
            Or and = new Or();
            and.children.add(left);
            and.children.add(right);
            return and;
        }
    }

    public static Expression from(List<Expression> or) {
        return new Or(or);
    }

    public List<Expression> getChildren() {
        return children;
    }

}
