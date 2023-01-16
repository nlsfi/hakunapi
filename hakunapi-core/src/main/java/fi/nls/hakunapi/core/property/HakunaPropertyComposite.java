package fi.nls.hakunapi.core.property;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import fi.nls.hakunapi.core.ObjectArrayValueContainer;
import fi.nls.hakunapi.core.QueryContext;
import fi.nls.hakunapi.core.ValueContainer;
import fi.nls.hakunapi.core.ValueMapper;
import fi.nls.hakunapi.core.ValueProvider;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.filter.FilterOp;
import fi.nls.hakunapi.core.transformer.ValueTransformer;
import fi.nls.hakunapi.core.util.StringPair;
import io.swagger.v3.oas.models.media.Schema;

/**
 * Property composed of multiple properties
 */
public class HakunaPropertyComposite extends HakunaPropertyBase {

    private final List<HakunaProperty> parts;
    private final ValueTransformer transformer;

    public HakunaPropertyComposite(String name, List<HakunaProperty> parts, ValueTransformer transformer, boolean unique, HakunaPropertyWriter propWriter) {
        super(name, parts.get(0).getTable(), transformer.getPublicType(), parts.stream().anyMatch(HakunaProperty::nullable), unique, propWriter);
        this.parts = parts;
        this.transformer = transformer;
    }

    @Override
    public String getColumn() {
        return null;
    }
    
    @Override
    public List<String> getColumns() {
        return parts.stream().map(HakunaProperty::getColumns)
                .collect(ArrayList::new, List::addAll, List::addAll);
    }
    
    @Override
    public Schema<?> getSchema() {
        return transformer.getPublicSchema();
    }

    public List<HakunaProperty> getParts() {
        return parts;
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    public Filter toFilter(FilterOp op, String s) {
        Object[] values = (Object[]) transformer.toInner(s);
        if (values == null) {
            return Filter.DENY;
        }
        List<Filter> and = new ArrayList<>(values.length);
        for (int i = 0; i < values.length; i++) {
            HakunaProperty prop = parts.get(i);
            Object value = values[i];
            if (prop.isStatic()) {
                if (!((HakunaPropertyStatic) prop).matches(value)) {
                    return Filter.DENY;
                }
            } else {
                and.add(new Filter(op, prop, value));
            }
        }
        return Filter.and(and);
    }

    @Override
    public Object toInner(String value) {
        return transformer.toInner(value);
    }
    
    @Override
    public ValueMapper getMapper(Map<StringPair, Integer> columnToIndex, int iValueContainer, QueryContext ctx) {
        List<BiConsumer<ValueProvider, ValueContainer>> subProcesses = new ArrayList<>(parts.size());
        for (int i = 0; i < parts.size(); i++) {
            HakunaProperty part = parts.get(i);
            ValueMapper partMapper = part.getMapper(columnToIndex, i, ctx);
            subProcesses.add(partMapper);
        }

        BiConsumer<ValueProvider, ValueContainer> mapperFunction = (vp, vc) -> {
            ObjectArrayValueContainer tmp = new ObjectArrayValueContainer(parts.size());
            subProcesses.forEach(it -> it.accept(vp, tmp));
            vc.setObject(iValueContainer, transformer.toPublic(tmp.values));
        };
        
        return new ValueMapper(this, mapperFunction);
    }
    
}
