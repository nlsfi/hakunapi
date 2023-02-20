package fi.nls.hakunapi.core.operation;

public class CollectionMetadataOperation implements ApiOperation {

    @Override
    public String getSummary() {
        return "Describe the {collectionId} feature collection";
    }

    @Override
    public String getDescription() {
        return "Metadata about the {collectionId} collection shared by this API";
    }

    @Override
    public String get200Description() {
        return "Metadata about the {collectionId} collection shared by this API";
    }

    @Override
    public String getOperationId() {
        return "describeCollection";
    }

    @Override
    public ApiTag getTag() {
        return ApiTag.Capabilities;
    }
    
}
