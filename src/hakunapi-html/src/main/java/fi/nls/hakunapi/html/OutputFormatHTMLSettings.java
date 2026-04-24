package fi.nls.hakunapi.html;

/**
 * Only public string fields allowed in this class
 * @see fi.nls.hakunapi.html.OutputFormatFactoryHTML
 */
public class OutputFormatHTMLSettings {

    static final OutputFormatHTMLSettings OSM;
    static {
        OSM = new OutputFormatHTMLSettings();
        OSM.tileUrl = "https://tile.openstreetmap.org/{z}/{x}/{y}.png";
        OSM.tileAttribution = "&copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors";
    }

    public String tileUrl;
    public String tileAttribution;
    public String tileOriginX;
    public String tileOriginY;

    public String getTileUrl() {
        return tileUrl;
    }

    public void setTileUrl(String tileUrl) {
        this.tileUrl = tileUrl;
    }

    public String getTileAttribution() {
        return tileAttribution;
    }

    public void setTileAttribution(String tileAttribution) {
        this.tileAttribution = tileAttribution;
    }

    public String getTileOriginX() {
        return tileOriginX;
    }

    public void setTileOriginX(String tileOriginX) {
        this.tileOriginX = tileOriginX;
    }

    public String getTileOriginY() {
        return tileOriginY;
    }

    public void setTileOriginY(String tileOriginY) {
        this.tileOriginY = tileOriginY;
    }

}
