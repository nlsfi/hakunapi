package fi.nls.hakunapi.cql2.model.literal;

public class StringLiteral implements Literal {
    
    private final String value;
    private final boolean casei;
    
    public StringLiteral(String value) {
        this(value, false);
    }

    public StringLiteral(String value, boolean casei) {
        this.value = value;
        this.casei = casei;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean isCasei() {
        return casei;
    }

}
