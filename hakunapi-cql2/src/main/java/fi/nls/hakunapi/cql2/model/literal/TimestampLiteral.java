package fi.nls.hakunapi.cql2.model.literal;

import java.time.Instant;

public class TimestampLiteral implements Literal {

    private final Instant timestamp;

    public TimestampLiteral(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

}
