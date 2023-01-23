package fi.nls.hakunapi.gpkg;

import java.util.Objects;

public class GPKGSpatialRefSys {
    
    private final String srsName;
    private final int srsId;
    private final String organisation;
    private final int organizationCoordsysId;
    private final String definition;
    private final String description;
    
    public GPKGSpatialRefSys(String srsName, int srsId, String organisation, int organizationCoordsysId, String definition) {
        this(srsName, srsId, organisation, organizationCoordsysId, definition, null);
    }
    
    public GPKGSpatialRefSys(String srsName, int srsId, String organisation, int organizationCoordsysId,
            String definition, String description) {
        this.srsName = Objects.requireNonNull(srsName);
        this.srsId = srsId;
        this.organisation = Objects.requireNonNull(organisation);
        this.organizationCoordsysId = organizationCoordsysId;
        this.definition = Objects.requireNonNull(definition);
        this.description = description;
    }

    public String getSrsName() {
        return srsName;
    }

    public int getSrsId() {
        return srsId;
    }

    public String getOrganisation() {
        return organisation;
    }

    public int getOrganizationCoordsysId() {
        return organizationCoordsysId;
    }

    public String getDefinition() {
        return definition;
    }

    public String getDescription() {
        return description;
    }
    
}
