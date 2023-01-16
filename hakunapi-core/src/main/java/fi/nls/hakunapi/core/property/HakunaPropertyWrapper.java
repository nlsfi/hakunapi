package fi.nls.hakunapi.core.property;

import java.util.Map;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.FeatureWriter;
import fi.nls.hakunapi.core.QueryContext;
import fi.nls.hakunapi.core.ValueMapper;
import fi.nls.hakunapi.core.ValueProvider;
import fi.nls.hakunapi.core.util.StringPair;

public abstract class HakunaPropertyWrapper implements HakunaProperty {

    protected final HakunaProperty wrapped;

    public HakunaPropertyWrapper(HakunaProperty wrapped) {
        this.wrapped = wrapped;
    }

    public HakunaProperty getWrapped() {
        return wrapped;
    }

    @Override
    public boolean isStatic() {
        return wrapped.isStatic();
    }

    @Override
    public void write(ValueProvider vc, int i, FeatureWriter writer) throws Exception {    
        wrapped.write(vc, i, writer);
    }

    @Override
    public String getName() {
        return wrapped.getName();
    }

    @Override
    public String getTable() {
        return wrapped.getTable();
    }

    @Override
    public HakunaPropertyType getType() {
        return wrapped.getType();
    }

    @Override
    public boolean nullable() {
        return wrapped.nullable();
    }

    @Override
    public boolean unique() {
        return wrapped.unique();
    }

    @Override
    public String getColumn() {
        return wrapped.getColumn();
    }

    @Override
    public FeatureType getFeatureType() {
        return wrapped.getFeatureType();
    }

    @Override
    public void setFeatureType(FeatureType ft) {
        wrapped.setFeatureType(ft);
    }

    @Override
    public ValueMapper getMapper(Map<StringPair, Integer> columnToIndex, int iValueContainer, QueryContext ctx) {
        return wrapped.getMapper(columnToIndex, iValueContainer, ctx);
    }

    @Override
    public HakunaPropertyWriter getPropertyWriter() {
        return wrapped.getPropertyWriter();
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
