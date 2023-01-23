package fi.nls.hakunapi.core.operation;

/**
 * Operations that should be published in the API response
 */
public interface WFS3Operation {

    public String getSummary();
    public String getDescription();
    public String getOperationId();
    public String get200Description();
    public WFS3Tag getTag();

}
