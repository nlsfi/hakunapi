package fi.nls.hakunapi.core.operation;

public class GetQueryablesOperation implements ApiOperation {

    @Override
    public String getSummary() {
        return "Lists the queryable attributes for the feature collection with id `collectionId`";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getOperationId() {
        return "getQueryables";
    }

    @Override
    public String get200Description() {
        return "List of queryable attributes";
    }

    @Override
    public ApiTag getTag() {
        return ApiTag.Capabilities;
    }

}
