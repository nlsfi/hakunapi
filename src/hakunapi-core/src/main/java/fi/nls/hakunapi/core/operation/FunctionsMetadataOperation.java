package fi.nls.hakunapi.core.operation;

public class FunctionsMetadataOperation implements ApiOperation {

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
    public ApiTag getTag() {
        return ApiTag.Capabilities;
    }

}
