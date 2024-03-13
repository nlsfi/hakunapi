package fi.nls.hakunapi.simple.servlet.javax;

import java.util.Iterator;
import java.util.List;

import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.FeatureStream;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.FeatureWriter;
import fi.nls.hakunapi.core.NextCursor;
import fi.nls.hakunapi.core.ValueProvider;
import fi.nls.hakunapi.core.param.LimitParam;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.request.WriteReport;

public class SimpleFeatureWriter {

    public static WriteReport writeFeatureCollection(FeatureCollectionWriter writer,
            FeatureType ft, List<HakunaProperty> properties, FeatureStream features, int offset, int limit) throws Exception {
        if (limit == LimitParam.UNLIMITED) {
            return writeFeatureCollectionFully(writer, ft, properties, features);
        }
        
        int numberReturned = 0;
        for (; numberReturned < limit; numberReturned++) {
            if (!features.hasNext()) {
                 break;
            }
            writeFeature(writer, ft, properties, features.next());
            writer.endFeature();
        }
        
        NextCursor next = null;
        if (numberReturned == limit && features.hasNext()) {
            next = ft.getPaginationStrategy().getNextCursor(offset, limit, features.next());
        }
        
        features.close();

        return new WriteReport(numberReturned, next);
    }
    
    public static WriteReport writeFeatureCollectionFully(FeatureCollectionWriter writer, FeatureType ft,
            List<HakunaProperty> properties, FeatureStream features) throws Exception {
        int numberReturned = 0;
        while (features.hasNext()) {
            ValueProvider feature = features.next();
            writeFeature(writer, ft, properties, feature);
            writer.endFeature();
            numberReturned++;
        }
        return new WriteReport(numberReturned, null);
    }

    public static void writeFeature(FeatureWriter writer, FeatureType ft, List<HakunaProperty> properties,
            ValueProvider feature) throws Exception {
        int i = 0;
        for (HakunaProperty property : properties) {
            property.write(feature, i++, writer);
        }
    }

}
