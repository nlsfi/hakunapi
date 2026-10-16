package fi.nls.hakunapi.simple.servlet.jakarta;

public final class PreEncodedBytes {

    private final byte[] bytes;

    public PreEncodedBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }

}
