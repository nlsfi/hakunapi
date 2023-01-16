package fi.nls.hakunapi.core.operation;

public class GetSchemaOperation implements WFS3Operation {

    @Override
    public String getSummary() {
        return "Get JSON schema for items in collection with id `collectionId`";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getOperationId() {
        return "getSchema";
    }

    @Override
    public String get200Description() {
        return "JSON schema for items in collection";
    }

    @Override
    public WFS3Tag getTag() {
        return WFS3Tag.Capabilities;
    }

}
