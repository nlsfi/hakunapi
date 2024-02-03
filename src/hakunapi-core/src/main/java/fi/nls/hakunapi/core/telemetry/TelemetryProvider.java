package fi.nls.hakunapi.core.telemetry;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;


public class TelemetryProvider {

    private static final ServiceLoader<TelemetryFactory> LOADER =
        ServiceLoader.load(TelemetryFactory.class);

    public static Map<String, ServiceTelemetry> getTelemetries() {
        LOADER.reload();

        final Map<String, ServiceTelemetry> telemetries = new HashMap<>();

        for (TelemetryFactory factory : LOADER) {
            ServiceTelemetry telemetry = factory.createServiceTelemetry();
            telemetries.put(telemetry.getId(), telemetry);
        }
        return telemetries;
    }
}
