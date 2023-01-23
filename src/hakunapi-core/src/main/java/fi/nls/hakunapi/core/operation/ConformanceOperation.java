package fi.nls.hakunapi.core.operation;

public class ConformanceOperation implements WFS3Operation {

    @Override
    public String getSummary() {
        return "information about standards that this API conforms to";
    }

    @Override
    public String getDescription() {
        return "List all requirements classes specified in a standard (e.g., WFS 3.0 " +
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
    public WFS3Tag getTag() {
        return WFS3Tag.Capabilities;
    }

}
