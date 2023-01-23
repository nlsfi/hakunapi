package fi.nls.hakunapi.core.property;

import java.util.Objects;

import fi.nls.hakunapi.core.FeatureType;

/**
 * Common base class for a property.
 * When {@link #isStatic()} returns true
 * the implementation is always HakunaPropertyStatic
 */
public abstract class HakunaPropertyBase implements HakunaProperty {

    protected final String name;
    protected final String table;
    protected final HakunaPropertyType type;
    protected final boolean nullable; 
    protected final boolean unique;
    protected final HakunaPropertyWriter propWriter;
    private FeatureType featureType;

    public HakunaPropertyBase(String name, String table, HakunaPropertyType type, boolean nullable, boolean unique, HakunaPropertyWriter propWriter) {
        this.name = Objects.requireNonNull(name);
        this.table = Objects.requireNonNull(table);
        this.type = Objects.requireNonNull(type);
        this.nullable = nullable;
        this.unique = unique;
        this.propWriter = Objects.requireNonNull(propWriter);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTable() {
        return table;
    }

    @Override
    public HakunaPropertyType getType() {
        return type;
    }

    @Override
    public boolean nullable() {
        return nullable;
    }

    @Override
    public boolean unique() {
        return unique;
    }

    @Override
    public FeatureType getFeatureType() {
        return featureType;
    }

    @Override
    public void setFeatureType(FeatureType featureType) {
        if (this.featureType != null) {
            throw new IllegalStateException("FeatureType should not be modified!");
        }
        this.featureType = featureType;
    }

    @Override
    public HakunaPropertyWriter getPropertyWriter() {
        return propWriter;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getTable() == null) ? 0 : getTable().hashCode());
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof HakunaProperty)) {
            return false;
        }
        HakunaProperty other = (HakunaProperty) obj;

        if (getName() == null) {
            if (other.getName() != null)
                return false;
        } else if (!getName().equals(other.getName())) {
            return false;
        }

        if (getTable() == null) {
            if (other.getTable() != null)
                return false;
        } else if (!getTable().equals(other.getTable())) {
            return false;
        }
        if (getType() != other.getType()) {
            return false;
        }
        return true;
    }

}
