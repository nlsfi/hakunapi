package fi.nls.hakunapi.core.property;

import fi.nls.hakunapi.core.FeatureWriter;
import fi.nls.hakunapi.core.ValueProvider;
import io.swagger.v3.oas.models.media.Schema;

public class HakunaPropertyHidden extends HakunaPropertyWrapper {

    public HakunaPropertyHidden(HakunaProperty wrapped) {
        super(wrapped);
    }

    @Override
    public Schema<?> getSchema() {
        return null;
    }

    @Override
    public Object toInner(String value) {
        return wrapped.toInner(value);
    }

    @Override
    public void write(ValueProvider vc, int i, FeatureWriter writer) throws Exception {
        // NOP
    }

    @Override
    public HakunaPropertyWriter getPropertyWriter() {
        return HakunaPropertyWriters.HIDDEN;
    }

}
