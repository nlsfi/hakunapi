package fi.nls.hakunapi.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public interface CRSRegistry {

    public default List<SRIDCode> detect(Properties config, List<Integer> srids) throws IllegalArgumentException {
        List<SRIDCode> codes = new ArrayList<>(srids.size());
        for (int srid : srids) {
            try {
                codes.add(detect(config, srid));
            } catch (IllegalArgumentException e) {
                if (SRIDCode.isKnown(srid)) {
                    codes.add(SRIDCode.getKnown(srid));
                }
                throw e;
            }
        }
        return codes;
    }

    public SRIDCode detect(Properties config, int srid) throws IllegalArgumentException;

}
