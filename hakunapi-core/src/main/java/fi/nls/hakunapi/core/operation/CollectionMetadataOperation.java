package fi.nls.hakunapi.core.operation;

public class CollectionMetadataOperation implements WFS3Operation {

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
    public WFS3Tag getTag() {
        return WFS3Tag.Capabilities;
    }
    
}
