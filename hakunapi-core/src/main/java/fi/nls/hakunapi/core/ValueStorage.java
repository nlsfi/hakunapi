package fi.nls.hakunapi.core;import java.util.function.Predicate;

public interface ValueStorage extends FeatureStream {
    
    public ValueContainer createNew();
    public void add(ValueContainer vc);
    public void removeIf(Predicate<ValueContainer> filter);

}
