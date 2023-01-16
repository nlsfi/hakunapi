package fi.nls.hakunapi.core.operation;

public class CollectionsMetadataOperation implements WFS3Operation {

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
    public WFS3Tag getTag() {
        return WFS3Tag.Capabilities;
    }

}
