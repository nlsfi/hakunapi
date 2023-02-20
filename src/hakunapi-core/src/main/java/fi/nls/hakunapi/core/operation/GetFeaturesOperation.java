package fi.nls.hakunapi.core.operation;

public class GetFeaturesOperation implements ApiOperation {

    @Override
    public String getSummary() {
        return "Retrieve features";
    }

    @Override
    public String getOperationId() {
        return "getFeatures";
    }

    @Override
    public ApiTag getTag() {
        return ApiTag.Features;
    }

    @Override
    public String getDescription() {
        return "Every feature in a dataset belongs to one collection of features. A "
                + "dataset may consist of multiple feature  collections. Typically, a "
                + "feature collection is a collection of features of a similar type, based "
                + "on a common schema.";
    }

    @Override
    public String get200Description() {
        return "Information about the feature collection plus the first features"
                + " matching the selection parameters.";
    }

}
