package fi.nls.hakunapi.core;

import java.util.function.BiConsumer;

import fi.nls.hakunapi.core.property.HakunaProperty;

public class ValueMapper implements BiConsumer<ValueProvider, ValueContainer> {
    
    private final HakunaProperty property;
    private final BiConsumer<ValueProvider, ValueContainer> wrapped;
    
    public ValueMapper(HakunaProperty property, BiConsumer<ValueProvider, ValueContainer> wrapped) {
        this.property = property;
        this.wrapped = wrapped;
    }
    
    public HakunaProperty getProperty() {
        return property;
    }
    
    @Override
    public void accept(ValueProvider t, ValueContainer u) {
        wrapped.accept(t, u);
    }

}
