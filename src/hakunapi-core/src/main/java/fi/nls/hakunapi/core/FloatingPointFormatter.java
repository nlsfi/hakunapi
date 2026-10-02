package fi.nls.hakunapi.core;

public interface FloatingPointFormatter {
    
    public int maxDecimalsFloat();
    public int maxDecimalsDouble();
    public int maxDecimalsOrdinate();
    
    public int writeFloat(float f, byte[] b, int off);
    public int writeDouble(double d, byte[] b, int off);
    public int writeOrdinate(double x, byte[] b, int off);

    // A coordinate's ordinates in one call, so that an implementation can share
    // work between them; b needs room for each ordinate plus the separators
    public default int writeOrdinates(double x, double y, byte separator, byte[] b, int off) {
        off = writeOrdinate(x, b, off);
        b[off++] = separator;
        return writeOrdinate(y, b, off);
    }

    public default int writeOrdinates(double x, double y, double z, byte separator, byte[] b, int off) {
        off = writeOrdinates(x, y, separator, b, off);
        b[off++] = separator;
        return writeOrdinate(z, b, off);
    }

    public default int writeOrdinates(double x, double y, double z, double m, byte separator, byte[] b, int off) {
        off = writeOrdinates(x, y, separator, b, off);
        b[off++] = separator;
        return writeOrdinates(z, m, separator, b, off);
    }
    
    public int writeFloat(float f, char[] arr, int off);
    public int writeDouble(double d, char[] arr, int off);
    public int writeOrdinate(double x, char[] arr, int off);

    public String writeFloat(float f);
    public String writeDouble(double d);
    public String writeOrdinate(double x);

}
