package fi.nls.hakunapi.core;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import fi.nls.hakunapi.core.geom.HakunaGeometry;

public class ObjectArrayValueContainer implements ValueContainer {

    public Object[] values;

    public ObjectArrayValueContainer(int numCols) {
        this(new Object[numCols]);
    }
    
    private ObjectArrayValueContainer(Object[] arr) {
        this.values = arr;
    }
    
    public static ObjectArrayValueContainer wrap(Object[] arr) {
        return new ObjectArrayValueContainer(arr);
    }
    
    @Override
    public void setNull(int i) {
        values[i] = null;
    }


    @Override
    public boolean isNull(int i) {
        return values[i] == null;
    }

    @Override
    public Boolean getBoolean(int i) {
        return (Boolean) values[i];
    }

    @Override
    public void setBoolean(int i, boolean v) {
        values[i] = v;
    }

    @Override
    public Integer getInt(int i) {
        return (Integer) values[i];
    }

    @Override
    public void setInt(int i, int v) {
        values[i] = v;
    }

    @Override
    public Long getLong(int i) {
        return (Long) values[i];
    }

    @Override
    public void setLong(int i, long v) {
        values[i] = v;
    }

    @Override
    public Float getFloat(int i) {
        return (Float) values[i];
    }

    @Override
    public void setFloat(int i, float v) {
        values[i] = v;
    }

    @Override
    public Double getDouble(int i) {
        return (Double) values[i];
    }

    @Override
    public void setDouble(int i, double v) {
        values[i] = v;
    }

    @Override
    public Object getObject(int i) {
        return values[i];
    }

    @Override
    public void setObject(int i, Object o) {
        values[i] = o;
    }

    @Override
    public String getString(int i) {
        return (String) values[i];
    }

    @Override
    public Instant getInstant(int i) {
        return (Instant) values[i];
    }

    @Override
    public LocalDateTime getLocalDateTime(int i) {
        return (LocalDateTime) values[i];
    }

    @Override
    public LocalDate getLocalDate(int i) {
        return (LocalDate) values[i];
    }

    @Override
    public HakunaGeometry getHakunaGeometry(int i) {
        return (HakunaGeometry) values[i];
    }

    @Override
    public UUID getUUID(int i) {
        return (UUID) values[i];
    }

    @Override
    public ValueContainer copyToMemory() {
        ObjectArrayValueContainer copy = new ObjectArrayValueContainer(values.length);
        System.arraycopy(values, 0, copy.values, 0, values.length);
        return copy;
    }

    @Override
    public int size() {
        return values.length;
    }

    @Override
    public Object[] getArray(int i) {
        return (Object[]) values[i];
    }
    
    @Override
    public byte[] getJSON(int i) {
        return (byte[]) values[i];
    }

}
