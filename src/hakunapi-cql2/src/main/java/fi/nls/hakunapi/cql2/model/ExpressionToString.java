package fi.nls.hakunapi.cql2.model;

import org.locationtech.jts.io.WKTWriter;

import fi.nls.hakunapi.cql2.model.function.FunctionCall;
import fi.nls.hakunapi.cql2.model.literal.BooleanLiteral;
import fi.nls.hakunapi.cql2.model.literal.DateLiteral;
import fi.nls.hakunapi.cql2.model.literal.NumberLiteral;
import fi.nls.hakunapi.cql2.model.literal.PropertyName;
import fi.nls.hakunapi.cql2.model.literal.StringLiteral;
import fi.nls.hakunapi.cql2.model.literal.TimestampLiteral;
import fi.nls.hakunapi.cql2.model.logical.And;
import fi.nls.hakunapi.cql2.model.logical.LogicalExpression;
import fi.nls.hakunapi.cql2.model.logical.Not;
import fi.nls.hakunapi.cql2.model.logical.Or;
import fi.nls.hakunapi.cql2.model.spatial.SpatialLiteral;
import fi.nls.hakunapi.cql2.model.spatial.SpatialPredicate;

/**
 * Just an example ExpressionVisitor
 * Probably not that useful
 */
public class ExpressionToString implements ExpressionVisitor {

    private StringBuilder sb = new StringBuilder();

    public String finish() {
        return sb.toString();
    }

    @Override
    public Object visit(And and) {
        boolean first = true;
        for (Expression e : and.getChildren()) {
            if (!first) {
                sb.append(" AND ");
            }
            if (e instanceof LogicalExpression) {
                sb.append("(");
            }
            visit(e);
            if (e instanceof LogicalExpression) {
                sb.append(")");
            }
            first = false;
        }
        return null;
    }

    @Override
    public Object visit(Or or) {
        boolean first = true;
        for (Expression e : or.getChildren()) {
            if (!first) {
                sb.append(" OR ");
            }
            if (e instanceof LogicalExpression) {
                sb.append("(");
            }
            visit(e);
            if (e instanceof LogicalExpression) {
                sb.append(")");
            }
            first = false;
        }
        return null;
    }

    @Override
    public Object visit(Not not) {
        sb.append("NOT (");
        this.visit(not.getExpression());
        sb.append(")");

        return null;
    }

    @Override
    public Object visit(BinaryComparisonPredicate expression) {
        visit(expression.getProp());
        sb.append(' ');
        sb.append(expression.getOp().op);
        sb.append(' ');
        visit(expression.getValue());
        return null;
    }

    @Override
    public Object visit(LikePredicate p) {
        visit(p.getProperty());
        sb.append(" LIKE ");
        sb.append(p.getPattern());
        return null;
    }

    @Override
    public Object visit(IsNullPredicate p) {
        visit(p.getProperty());
        sb.append(" IS NULL");
        return null;
    }

    @Override
    public Object visit(PropertyName expression) {
        sb.append("\"" + expression.getValue() + "\"");
        return null;
    }

    @Override
    public Object visit(NumberLiteral number) {
        sb.append(number.getValue());
        return null;
    }

    @Override
    public Object visit(BooleanLiteral bool) {
        sb.append(bool.getValue());
        return null;
    }

    @Override
    public Object visit(StringLiteral string) {
        sb.append("'" + string.getValue().replace("'", "''") + "'");
        return null;
    }



    @Override
    public Object visit(DateLiteral p) {
        sb.append("'" + p.getDate().toString() + "'");
        return null;
    }

    @Override
    public Object visit(TimestampLiteral p) {
        sb.append("'" + p.getTimestamp().toString() + "'");
        return null;
    }

    @Override
    public Object visit(SpatialPredicate p) {
        sb.append(p.getOp().name());
        sb.append('(');
        visit(p.getProp());
        sb.append(',');
        sb.append(' ');
        visit(p.getValue());
        sb.append(')');
        return null;
    }

    @Override
    public Object visit(SpatialLiteral p) {
        sb.append(new WKTWriter().write(p.getGeometry()));
        return null;
    }

    @Override
    public Object visit(FunctionCall fn) {
        sb.append(fn.getName());
        sb.append('(');
        boolean f = true;
        for (Expression arg : fn.getArgs()) {
            if (!f) {
                sb.append(", ");
            }
            visit(arg);
            f = false;
        }
        sb.append(')');
        return null;
    }

}
