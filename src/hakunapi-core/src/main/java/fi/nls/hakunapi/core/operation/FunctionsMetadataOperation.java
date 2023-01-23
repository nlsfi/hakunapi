package fi.nls.hakunapi.core.operation;

public class FunctionsMetadataOperation implements WFS3Operation {

    @Override
    public String getSummary() {
        return "Describe the custom functions available in the service";
    }

    @Override
    public String getDescription() {
        return "Metadata about the custom functions shared by this API";
    }

    @Override
    public String get200Description() {
        return "Metadata about the custom functions shared by this API";
    }

    @Override
    public String getOperationId() {
        return "functions";
    }

    @Override
    public WFS3Tag getTag() {
        return WFS3Tag.Capabilities;
    }

}
