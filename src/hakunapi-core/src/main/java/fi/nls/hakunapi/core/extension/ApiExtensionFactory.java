package fi.nls.hakunapi.core.extension;

@FunctionalInterface
public interface ApiExtensionFactory {

    public ApiExtension create(ApiExtensionContext context) throws Exception;

}
