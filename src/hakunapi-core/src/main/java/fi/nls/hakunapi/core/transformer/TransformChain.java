package fi.nls.hakunapi.core.transformer;

import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyTransformed;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import io.swagger.v3.oas.models.media.Schema;

public class TransformChain implements ValueTransformer {

    private ValueTransformer[] chain;

    @Override
    public void init(HakunaProperty property, String arg) throws Exception {
        if (!(property instanceof HakunaPropertyTransformed)) {
            throw new IllegalArgumentException("Expected HakunaPropertyTransformed");
        }

        HakunaProperty p = ((HakunaPropertyTransformed) property).getWrapped();
        String[] piped = arg.split("\\|");
        chain = new ValueTransformer[piped.length];
        for (int i = 0; i < piped.length; i++) {
            String pipe = piped[i];
            int j = pipe.indexOf('(');
            int k = pipe.indexOf(')', j + 1);
            String className = pipe.substring(0, j).trim();
            String args = pipe.substring(j + 1, k).trim();
            ValueTransformer vt = (ValueTransformer) Class.forName(className).newInstance();
            p = new HakunaPropertyTransformed(p, vt);
            vt.init(p, args);
            chain[i] = vt;
        }
    }

    @Override
    public HakunaPropertyType getPublicType() {
        return chain[chain.length - 1].getPublicType();
    }

    @Override
    public Object toInner(String value) throws IllegalArgumentException {
        Object o = value;
        for (int i = chain.length - 1; i >= 0 && o != null && o != HakunaProperty.UNKNOWN; i--) {
            o = chain[i].toInner(o.toString());
        }
        return o;
    }

    @Override
    public Object toPublic(Object o) {
        for (int i = 0; i < chain.length && o != null && o != HakunaProperty.UNKNOWN; i++) {
            o = chain[i].toPublic(o);
        }
        return o;
    }

    @Override
    public Schema<?> getPublicSchema() {
        return chain[chain.length - 1].getPublicSchema();
    }

}
