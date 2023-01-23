package fi.nls.hakunapi.core;

import java.util.Base64;

public class NextCursor {

    private static final char SEP = ':';

    private final String next;
    private final int offset;

    public NextCursor(String next, int offset) {
        this.next = next == null ? "" : next;
        this.offset = offset;
    }

    public NextCursor(String wire) {
        String packed = new String(Base64.getDecoder().decode(wire));
        int i = packed.indexOf(SEP);
        if (i < 0) {
            throw new IllegalArgumentException("Invalid next cursor");
        }
        this.offset = Integer.parseInt(packed.substring(0, i));
        this.next = packed.substring(i + 1);
    }

    public String toWireFormat() {
        return Base64.getEncoder().encodeToString(getPacked().getBytes());
    }

    private String getPacked() {
        return "" + offset + SEP + next;
    }

    public String getNext() {
        return next;
    }

    public int getOffset() {
        return offset;
    }

}
