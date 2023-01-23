package fi.nls.hakunapi.gpkg.function;

import org.locationtech.jts.geom.Envelope;

public class ST_MinY extends EnvelopeFunction {
    
    @Override
    public String getName() {
        return "ST_MinY";
    }

    @Override
    protected int getEnvelopeIndex() {
        return 2;
    }

    @Override
    protected double getValueFromEnvelope(Envelope envelope) {
        return envelope.getMinY();
    }
    
}
