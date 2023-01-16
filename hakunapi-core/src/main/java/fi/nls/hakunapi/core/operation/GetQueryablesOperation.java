package fi.nls.hakunapi.core.operation;

public class GetQueryablesOperation implements WFS3Operation {

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
    public WFS3Tag getTag() {
        return WFS3Tag.Capabilities;
    }

}
