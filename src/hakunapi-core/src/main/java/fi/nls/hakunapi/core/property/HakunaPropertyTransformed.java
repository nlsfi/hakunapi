package fi.nls.hakunapi.core.property;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import fi.nls.hakunapi.core.FeatureWriter;
import fi.nls.hakunapi.core.ValueProvider;
import fi.nls.hakunapi.core.geom.HakunaGeometry;
import fi.nls.hakunapi.core.transformer.ValueTransformer;
import io.swagger.v3.oas.models.media.Schema;

public class HakunaPropertyTransformed extends HakunaPropertyWrapper {

    private final ValueTransformer transformer;

    public HakunaPropertyTransformed(HakunaProperty wrapped, ValueTransformer transformer) {
        super(wrapped);
        this.transformer = transformer;
    }

    @Override
    public String getName() {
        return wrapped.getName();
    }

    @Override
    public HakunaPropertyType getType() {
        return transformer.getPublicType();
    }

    @Override
    public Schema<?> getSchema() {
        return transformer.getPublicSchema();
    }
    
    public HakunaPropertyType getInnerType() {
        return wrapped.getType();
    }

    @Override
    public void write(ValueProvider vc, int i, FeatureWriter writer) throws Exception {
        Object inner = vc.getObject(i);
        Object o = transformer.toPublic(inner);
        String name = getName();
        if (o == null) {
            writer.writeNullProperty(name);
        } else {
            switch (transformer.getPublicType()) {
            case BOOLEAN:
                writer.writeProperty(name, (Boolean) o);
                break;
            case INT:
                writer.writeProperty(name, (Integer) o);
                break;
            case LONG:
                writer.writeProperty(name, (Long) o);
                break;
            case FLOAT:
                writer.writeProperty(name, (Float) o);
                break;
            case DOUBLE:
                writer.writeProperty(name, (Double) o);
                break;
            case STRING:
                writer.writeProperty(name, (String) o);
                break;
            case DATE:
                writer.writeProperty(name, (LocalDate) o);
                break;
            case TIMESTAMP:
                writer.writeProperty(name, (Instant) o);
                break;
            case UUID:
                writer.writeProperty(name, ((UUID) o).toString());
                break;
            case GEOMETRY:
                writer.writeProperty(name, (HakunaGeometry) o);
                break;
            default:
                throw new IllegalArgumentException("Invalid property type");
            }
        }
    }

    @Override
    public Object toInner(String value) {
        return transformer.toInner(value);
    }

}
