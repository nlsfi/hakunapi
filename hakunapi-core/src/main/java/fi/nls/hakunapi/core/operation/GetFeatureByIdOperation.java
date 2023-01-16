package fi.nls.hakunapi.core.operation;

public class GetFeatureByIdOperation implements WFS3Operation {

    final String operationId;
    final boolean isRepeating;
    public boolean isRepeatingId() { return isRepeating; }

    
    public GetFeatureByIdOperation() {
        operationId = "getFeatureById";
        isRepeating = false;
    }

    public GetFeatureByIdOperation(final String opId, boolean repeat) {
        operationId = opId;
        isRepeating = repeat;
    }

    @Override
    public String getSummary() {
        return "Retrieve a feature by id";
    }

    @Override
    public String getOperationId() {
        return operationId;
    }

    @Override
    public WFS3Tag getTag() {
        return WFS3Tag.Features;
    }

    @Override
    public String getDescription() {
        return "Get specific Feature by id";
    }

    @Override
    public String get200Description() {
        return "The requested feature";
    }

}
