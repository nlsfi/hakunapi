package fi.nls.hakunapi.gpkg.function;

import org.locationtech.jts.geom.Envelope;

public class ST_MaxX extends EnvelopeFunction {
    
    @Override
    public String getName() {
        return "ST_MaxX";
    }

    @Override
    protected int getEnvelopeIndex() {
        return 1;
    }

    @Override
    protected double getValueFromEnvelope(Envelope envelope) {
        return envelope.getMaxX();
    }
    
}
