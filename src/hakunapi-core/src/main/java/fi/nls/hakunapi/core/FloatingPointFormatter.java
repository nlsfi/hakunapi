package fi.nls.hakunapi.core;

public interface FloatingPointFormatter {
    
    public int maxDecimalsFloat();
    public int maxDecimalsDouble();
    public int maxDecimalsOrdinate();
    
    public int writeFloat(float f, byte[] b, int off);
    public int writeDouble(double d, byte[] b, int off);
    public int writeOrdinate(double x, byte[] b, int off);
    
    public int writeFloat(float f, char[] arr, int off);
    public int writeDouble(double d, char[] arr, int off);
    public int writeOrdinate(double x, char[] arr, int off);

    public String writeFloat(float f);
    public String writeDouble(double d);
    public String writeOrdinate(double x);

}
