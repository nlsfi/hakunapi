package fi.nls.hakunapi.cql2.model.literal;

public class NumberLiteral implements Literal {

    private final double value;

    public NumberLiteral(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

}
