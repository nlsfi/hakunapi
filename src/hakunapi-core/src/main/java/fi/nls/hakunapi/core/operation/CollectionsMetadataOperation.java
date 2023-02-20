package fi.nls.hakunapi.core.operation;

public class CollectionsMetadataOperation implements ApiOperation {

    @Override
    public String getSummary() {
        return "Describe the feature collections in the dataset";
    }

    @Override
    public String getDescription() {
        return "Metadata about the feature collections shared by this API";
    }

    @Override
    public String get200Description() {
        return "Metadata about the feature collections shared by this API";
    }

    @Override
    public String getOperationId() {
        return "describeCollections";
    }

    @Override
    public ApiTag getTag() {
        return ApiTag.Capabilities;
    }

}
