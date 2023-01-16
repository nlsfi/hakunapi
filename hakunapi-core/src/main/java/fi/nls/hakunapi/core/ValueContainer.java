package fi.nls.hakunapi.core;

public interface ValueContainer extends ValueProvider {

    public void setNull(int i);
    public void setBoolean(int i, boolean v);
    public void setInt(int i, int v);
    public void setLong(int i, long v);
    public void setFloat(int i, float v);
    public void setDouble(int i, double v);
    public void setObject(int i, Object o);
    public ValueContainer copyToMemory();

}
