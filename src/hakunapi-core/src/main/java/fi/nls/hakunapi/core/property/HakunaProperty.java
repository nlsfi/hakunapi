package fi.nls.hakunapi.core.property;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.FeatureWriter;
import fi.nls.hakunapi.core.QueryContext;
import fi.nls.hakunapi.core.ValueMapper;
import fi.nls.hakunapi.core.ValueProvider;
import fi.nls.hakunapi.core.util.StringPair;
import io.swagger.v3.oas.models.media.Schema;

public interface HakunaProperty {
    
    public static final Object UNKNOWN = new Object();

    public String getName();
    public String getTable();
    public String getColumn();
    public default List<String> getColumns() {
        return Collections.singletonList(getColumn());
    };
    public HakunaPropertyType getType();
    public boolean nullable();
    public boolean unique();
    public Schema<?> getSchema();
    public boolean isStatic();
    
    public FeatureType getFeatureType();
    public void setFeatureType(FeatureType ft);
    
    public Object toInner(String value);
    public HakunaPropertyWriter getPropertyWriter();
    
    public ValueMapper getMapper(Map<StringPair, Integer> columnToIndex, int out, QueryContext ctx);
    public default void write(ValueProvider vp, int i, FeatureWriter writer) throws Exception {
        getPropertyWriter().write(vp, i, writer);
    }
    
}
