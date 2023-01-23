package fi.nls.hakunapi.core.operation;

public class OperationImpl {

    public final WFS3Operation operation;
    public final Class<?> implementation;

    public OperationImpl(WFS3Operation operation, Class<?> implementation) {
        this.operation = operation;
        this.implementation = implementation;
    }

}
