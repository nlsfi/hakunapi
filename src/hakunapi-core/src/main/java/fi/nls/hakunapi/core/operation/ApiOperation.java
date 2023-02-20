package fi.nls.hakunapi.core.operation;

/**
 * Operations that should be published in the API response
 */
public interface ApiOperation {

    public String getSummary();
    public String getDescription();
    public String getOperationId();
    public String get200Description();
    public ApiTag getTag();

}
