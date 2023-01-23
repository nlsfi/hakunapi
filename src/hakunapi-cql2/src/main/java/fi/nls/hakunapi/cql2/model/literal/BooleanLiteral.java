package fi.nls.hakunapi.cql2.model.literal;

public class BooleanLiteral implements Literal {

    private final boolean value;

    public BooleanLiteral(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

}
