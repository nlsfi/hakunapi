package fi.nls.hakunapi.core.operation;

public class LandingPageOperation implements ApiOperation {

    @Override
    public String getSummary() {
        return "Landing page of this API";
    }

    @Override
    public String getDescription() {
        return "The landing page provides links to the API definition, the Conformance "
                + "statements and the metadata about the feature data in this dataset.";
    }

    @Override
    public String get200Description() {
        return "Links to the API capabilities";
    }

    @Override
    public String getOperationId() {
        return "getLandingPage";
    }

    @Override
    public ApiTag getTag() {
        return ApiTag.Capabilities;
    }

}
