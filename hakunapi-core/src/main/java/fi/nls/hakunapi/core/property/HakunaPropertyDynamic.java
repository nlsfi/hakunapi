package fi.nls.hakunapi.core.property;

import java.util.Map;
import java.util.function.BiConsumer;

import fi.nls.hakunapi.core.QueryContext;
import fi.nls.hakunapi.core.ValueContainer;
import fi.nls.hakunapi.core.ValueMapper;
import fi.nls.hakunapi.core.ValueProvider;
import fi.nls.hakunapi.core.util.StringPair;

/**
 * Hakuna Property (non-static/literal)
 * Backed by a database column
 */
public abstract class HakunaPropertyDynamic extends HakunaPropertyBase {

    protected final String column;

    public HakunaPropertyDynamic(String name, String table, String column, HakunaPropertyType type, boolean nullable, boolean unique, HakunaPropertyWriter writer) {
        super(name, table, type, nullable, unique, writer);
        this.column = column;
    }

    @Override
    public String getColumn() {
        return column;
    }

    @Override
    public final boolean isStatic() {
        return false;
    }
    
    @Override
    public ValueMapper getMapper(Map<StringPair, Integer> columnToIndex, int iValueContainer, QueryContext ctx) {
        StringPair key = new StringPair(table, column);
        if (!columnToIndex.containsKey(key)) {
            throw new RuntimeException("Could not find column for property: " + getName() + " table: " + getTable() + " column: " + getColumn());
        }
        return new ValueMapper(this, getMapperFunction(columnToIndex.get(key), iValueContainer, ctx));
    }
    
    public abstract BiConsumer<ValueProvider, ValueContainer> getMapperFunction(int iValueProvider, int iValueContainer, QueryContext ctx);

}
