package fi.nls.hakunapi.gpkg.function;

import java.nio.ByteOrder;
import java.sql.SQLException;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBReader;

import fi.nls.hakunapi.gpkg.GPKGGeometry;

public abstract class EnvelopeFunction extends GPKGFunction {

    @Override
    protected void xFunc() throws SQLException {
        if (args() != 1) {
            throw new SQLException("Expected args() = 1");
        }
        byte[] geometry = value_blob(0);
        /* Check magic?
        if (geometry[0] != 'G' || geometry[1] != 'P') {
            throw new SQLException("Illegal magic number!");
        }
         */
        byte flags = geometry[3];
        ByteOrder bo = GPKGGeometry.getByteOrder(flags);
        int envelopeIndicator = GPKGGeometry.getEnvelopeIndicatorCode(flags);
        if (envelopeIndicator != GPKGGeometry.NO_ENVELOPE) {
            result(Bytes.readDouble(geometry, 8 + getEnvelopeIndex() * 8, bo));
        } else {
            int off = GPKGGeometry.getWKBOffset(envelopeIndicator);
            Geometry g;
            try {
                g = new WKBReader().read(new ByteArrayViewInStream(geometry, off));
            } catch (Exception e) {
                throw new SQLException(e);
            }
            result(getValueFromEnvelope(g.getEnvelopeInternal()));
        }
    }

    protected abstract int getEnvelopeIndex();
    protected abstract double getValueFromEnvelope(Envelope envelope);

}
