package fi.nls.hakunapi.core.property;

import java.util.List;
import java.util.function.BiConsumer;

import fi.nls.hakunapi.core.QueryContext;
import fi.nls.hakunapi.core.ValueContainer;
import fi.nls.hakunapi.core.ValueProvider;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class HakunaPropertyArray extends HakunaPropertyDynamic {

	private final HakunaPropertyType componentType;
	private final int arrayDimension;
	
	public HakunaPropertyArray(String name, String table, String column, List<HakunaPropertyType> typeChain, boolean nullable, boolean unique, HakunaPropertyWriter propWriter) {
		super(name, table, column, HakunaPropertyType.ARRAY, nullable, unique, propWriter);
		this.arrayDimension = typeChain.size() - 1;
		this.componentType = typeChain.get(arrayDimension);
	}
	
	public HakunaPropertyType getComponentType() {
	    return componentType;
	}

	@Override
	public Schema<?> getSchema() {
	    Schema schema = getItemSchema();
	    for (int i = 0; i < arrayDimension; i++) {
	        schema = new ArraySchema().items(schema);
	    }
	    return schema;
	}
	
    public Schema getItemSchema() {
        switch (componentType) {
        case BOOLEAN:
            return new BooleanSchema();
        case INT:
            return new IntegerSchema();
        case LONG:
            return new IntegerSchema().format("int64");
        case FLOAT:
            return new NumberSchema().format("float");
        case DOUBLE:
            return new NumberSchema().format("double");
        case STRING:
            return new StringSchema();
        case JSON:
            return new ObjectSchema();
        default:
            throw new IllegalArgumentException("Invalid type for array");
        }
    }

    @Override
    public Object toInner(String value) {
        switch (componentType) {
        case BOOLEAN:
            return Boolean.parseBoolean(value);
        case INT:
            return Integer.parseInt(value);
        case LONG:
            return Long.parseLong(value);
        case FLOAT:
            return Float.parseFloat(value);
        case DOUBLE:
            return Double.parseDouble(value);
        case STRING:
            return value;
        default:
            throw new IllegalArgumentException("Invalid type for array");
        }
    }

    @Override
    public BiConsumer<ValueProvider, ValueContainer> getMapperFunction(int iValueProvider, int iValueContainer, QueryContext ctx) {
        return (vp, vc) -> {
            vc.setObject(iValueContainer, vp.getArray(iValueProvider));
        };
    }

}
