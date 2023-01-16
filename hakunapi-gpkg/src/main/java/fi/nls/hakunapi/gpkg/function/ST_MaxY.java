package fi.nls.hakunapi.gpkg.function;

import org.locationtech.jts.geom.Envelope;

public class ST_MaxY extends EnvelopeFunction {
    
    @Override
    public String getName() {
        return "ST_MaxY";
    }

    @Override
    protected int getEnvelopeIndex() {
        return 3;
    }

    @Override
    protected double getValueFromEnvelope(Envelope envelope) {
        return envelope.getMaxY();
    }

}
