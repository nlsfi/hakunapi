package fi.nls.hakunapi.core;

import java.util.function.Function;

@FunctionalInterface
public interface CurrentServerUrlProvider {

    public String get(Function<String, String> dynamicValues);

}
