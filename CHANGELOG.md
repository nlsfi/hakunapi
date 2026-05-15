# Changelog

## 2.0.0

For a full list of changes see: https://github.com/nlsfi/hakunapi/milestone/17

### Breaking changes (migration guide)

- **Java 21 required.** Minimum runtime/build raised from Java 11. Upgrade JDK before building or deploying.
- **Jakarta only.** All `javax.*` support dropped. Replace any `javax.servlet.*` / `javax.ws.rs.*` imports with `jakarta.*` equivalents. The `webapp-javax` module is gone — switch deployments to `webapp-jakarta`.
- **Jackson 3.** Migrated from Jackson 2 to Jackson 3. Custom serializers/deserializers, `ObjectMapper` configuration, and module dependencies must be updated to the Jackson 3 API (package `tools.jackson.*`).
- **Oracle source removed.** Modules `hakunapi-source-oracle` and `hakunapi-oracle-webapp-jakarta` dropped. Users needing Oracle Spatial data sources should pin hakunapi ≤ 1.x or maintain the module out-of-tree.
- **OpenTelemetry removed.** Modules `hakunapi-telemetry-opentelemetry` and `hakunapi-telemetry-webapp-jakarta` dropped. The `hakunapi-telemetry` JSON-log mode and NOP telemetry remain available; the `ServiceTelemetry`/`RequestTelemetry`/`TelemetryFactory` SPI in `hakunapi-core` is unchanged.

### Functional changes

- HTML `FeatureCollection` rendering revamped.
- Bbox SRID is now always set.
- Fixed HTML projection handling (incl. `bbox-crs` no-op projection case).
- Removed obsolete `.github/workflows/deploy.yml` CI workflow.
- README: added table of selected dependencies.

### Library updates

- `org.postgresql:postgresql` 42.7.10 → 42.7.11
- `org.apache.logging.log4j:log4j-core` 2.25.3 → 2.25.4

## < 1.7.1

No Changelog available, see git history for releases 1.0.0 – 1.7.1.
