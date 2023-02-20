package fi.nls.hakunapi.core.operation;

public class OperationImpl {

    public final ApiOperation operation;
    public final Class<?> implementation;

    public OperationImpl(ApiOperation operation, Class<?> implementation) {
        this.operation = operation;
        this.implementation = implementation;
    }

}
