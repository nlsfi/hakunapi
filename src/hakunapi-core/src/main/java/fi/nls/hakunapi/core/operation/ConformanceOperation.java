package fi.nls.hakunapi.core.operation;

public class ConformanceOperation implements ApiOperation {

    @Override
    public String getSummary() {
        return "information about standards that this API conforms to";
    }

    @Override
    public String getDescription() {
        return "List all requirements classes specified in a standard (e.g., OGC API - Features " +
                "Part 1: Core) that the server conforms to";
    }

    @Override
    public String get200Description() {
        return "The URIs of all requirements classes supported by the server";
    }

    @Override
    public String getOperationId() {
        return "getRequirementsClasses";
    }

    @Override
    public ApiTag getTag() {
        return ApiTag.Capabilities;
    }

}
