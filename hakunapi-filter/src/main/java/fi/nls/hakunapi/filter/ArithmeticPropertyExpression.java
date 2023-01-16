package fi.nls.hakunapi.filter;

import java.util.function.BiFunction;

import org.opengis.filter.expression.Expression;

public class ArithmeticPropertyExpression {

    enum Operation {
        ADD((a, b) -> b.doubleValue() - a.doubleValue()),
        SUBTRACT((a, b) -> b.doubleValue() + a.doubleValue()),
        MULTIPLY((a, b) -> b.doubleValue() / a.doubleValue()),
        DIVIDE((a, b) -> b.doubleValue() * a.doubleValue());

        private BiFunction<Number, Number, Number> lambda;

        public Operation negate() {
            switch (this) {
            case ADD:
                return SUBTRACT;
            case SUBTRACT:
                return ADD;
            case MULTIPLY:
                return DIVIDE;
            case DIVIDE:
                return  MULTIPLY;
            default:
                throw new RuntimeException();
            }
        }

        public boolean isSymmetric() {
            switch (this) {
            case ADD:
            case MULTIPLY:
                return true;
            case SUBTRACT:
            case DIVIDE:
                return false;
            default:
                throw new RuntimeException();
            }
        }

        private Operation(BiFunction<Number, Number, Number> lambda) {
            this.lambda = lambda;
        }

        public Number swapToOtherSide(Number left, Number right) {
            return lambda.apply(left, right);
        }
    }

    private final Operation operation;
    private final Expression propertyOrArithmeticExpression;
    private final Number literal;
    private final boolean propertyIsLeft;

    public ArithmeticPropertyExpression(Operation operation, Expression propertyOrArithmeticExpression, Number literal, boolean propertyIsLeft) {
        this.operation = operation;
        this.propertyOrArithmeticExpression = propertyOrArithmeticExpression;
        this.literal = literal;
        this.propertyIsLeft = propertyIsLeft;
    }

    public Operation getOperation() {
        return operation;
    }

    public Expression getProperty() {
        return propertyOrArithmeticExpression;
    }

    public Number getLiteral() {
        return literal;
    }

    public boolean isPropertyLeft() {
        return propertyIsLeft;
    }

}
