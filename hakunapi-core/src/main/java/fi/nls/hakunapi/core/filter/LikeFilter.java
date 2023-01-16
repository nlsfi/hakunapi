package fi.nls.hakunapi.core.filter;

import java.util.Objects;

import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyType;

public class LikeFilter extends Filter {
    
    private final char wildCard;
    private final char singleChar;
    private final char escape;
    private final boolean caseInsensitive;

    protected LikeFilter(FilterOp like, HakunaProperty prop, Object value, char wildCard, char singleChar, char escape, boolean caseInsensitive) {
        super(FilterOp.LIKE, prop, Objects.requireNonNull(value));
        if (prop.getType() != HakunaPropertyType.STRING) {
            throw new IllegalArgumentException("LikeFilter is limited to strings");
        }
        this.wildCard = wildCard;
        this.singleChar = singleChar;
        this.escape = escape;
        if (wildCard == singleChar || wildCard == escape || singleChar == escape) {
            throw new IllegalArgumentException("Wildcard char, single char and escape char must be distinguishable from each other");
        }
        this.caseInsensitive = caseInsensitive;
    }
    
    public char getWildCard() {
        return wildCard;
    }

    public char getSingleChar() {
        return singleChar;
    }

    public char getEscape() {
        return escape;
    }

    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        LikeFilter o = (LikeFilter) obj;
        return wildCard == o.wildCard
                && singleChar == o.singleChar
                && escape == o.escape
                && caseInsensitive == o.caseInsensitive;
    }
    
}
