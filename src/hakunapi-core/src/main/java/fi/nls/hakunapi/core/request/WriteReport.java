package fi.nls.hakunapi.core.request;

import fi.nls.hakunapi.core.NextCursor;

public class WriteReport {

    public final int numberReturned;
    public final NextCursor next;

    public WriteReport(int numberReturned, NextCursor next) {
        this.numberReturned = numberReturned;
        this.next = next;
    }

}
