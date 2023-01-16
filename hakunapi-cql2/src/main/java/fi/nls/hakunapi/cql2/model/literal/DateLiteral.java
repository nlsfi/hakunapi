package fi.nls.hakunapi.cql2.model.literal;

import java.time.LocalDate;

public class DateLiteral implements Literal {

    private final LocalDate date;

    public DateLiteral(LocalDate date) {
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }

}
