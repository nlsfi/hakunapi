package fi.nls.hakunapi.gpkg.function;

import org.locationtech.jts.geom.Envelope;

public class ST_MinX extends EnvelopeFunction {
    
    @Override
    public String getName() {
        return "ST_MinX";
    }

    @Override
    protected int getEnvelopeIndex() {
        return 0;
    }

    @Override
    protected double getValueFromEnvelope(Envelope envelope) {
        return envelope.getMinX();
    }

}
