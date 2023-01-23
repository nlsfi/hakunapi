package fi.nls.hakunapi.cql2.model.logical;

import java.util.ArrayList;
import java.util.List;

import fi.nls.hakunapi.cql2.model.Expression;

public class And implements LogicalExpression {

    private final List<Expression> children;
    
    private And() {
        this(new ArrayList<>());
    }
    
    private And(List<Expression> children) {
        this.children = children;
    }

    public static And from(Expression left, Expression right) {
        if (left instanceof And && right instanceof And) {
            ((And) left).children.addAll(((And) right).children);
            return (And) left;
        } else if (left instanceof And)  {
            ((And) left).children.add(right);
            return (And) left;
        } else if (right instanceof And) {
            ((And) right).children.add(left);
            return (And) right;
        } else {
            And and = new And();
            and.children.add(left);
            and.children.add(right);
            return and;
        }
    }

    public static Expression from(List<Expression> and) {
        return new And(and);
    }

    public List<Expression> getChildren() {
        return children;
    }

}
