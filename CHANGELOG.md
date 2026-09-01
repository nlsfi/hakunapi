# Changelog

## 2.0.0

For a full list of changes see: https://github.com/nlsfi/hakunapi/milestone/17

### Breaking changes (migration guide)

- **Java 21 required.** Minimum runtime/build raised from Java 11. Upgrade JDK before building or deploying.
- **Jakarta only.** All `javax.*` support dropped. Replace any `javax.servlet.*` / `javax.ws.rs.*` imports with `jakarta.*` equivalents. The `webapp-javax` module is gone — switch deployments to `webapp-jakarta`.
- **Jackson 3.** Migrated from Jackson 2 to Jackson 3. Custom serializers/deserializers, `ObjectMapper` configuration, and module dependencies must be updated to the Jackson 3 API (package `tools.jackson.*`).
- **Oracle source removed.** Modules `hakunapi-source-oracle` and `hakunapi-oracle-webapp-jakarta` dropped. Users needing Oracle Spatial data sources should pin hakunapi ≤ 1.x or maintain the module out-of-tree.
- **OpenTelemetry removed.** Modules `hakunapi-telemetry-opentelemetry` and `hakunapi-telemetry-webapp-jakarta` dropped. The `hakunapi-telemetry` JSON-log mode and NOP telemetry remain available; the `ServiceTelemetry`/`RequestTelemetry`/`TelemetryFactory` SPI in `hakunapi-core` is unchanged.
- **Community modules moved and unbundled.** Modules `hakunapi-smile`, `hakunapi-esbulk`, `hakunapi-gml` and `hakunapi-proj-jhe` moved from `src/` to the new `src-community/` folder and are no longer bundled in the reference `features.war` (`hakunapi-simple-webapp-jakarta`). Maven coordinates and Java packages are unchanged, and versions are still managed by the root POM. Deployments that enable `formats=...,smile` or `formats=...,esbulk`, or select the projection factory `fi.nls.hakunapi.proj.jhe.JHeProjectionTransformerFactory` via the `proj` configuration key, must now add the corresponding dependency to their own webapp and rebuild. `hakunapi-proj-gt` remains the bundled default for coordinate transformations. See [CONTRIBUTING.md](CONTRIBUTING.md#community-modules).

### Functional changes

- HTML `FeatureCollection` rendering revamped.
- Bbox SRID is now always set.
- Fixed HTML projection handling (incl. `bbox-crs` no-op projection case).
- Fixed broken maps on HTML feature pages: the proj4 and proj4leaflet CDN URLs pointed at files the packages do not publish, so jsDelivr generated them on the fly and their SRI hashes drifted.
- Removed obsolete `.github/workflows/deploy.yml` CI workflow.
- Documentation: added a table of selected dependencies to [docs/overview/modules.md](docs/overview/modules.md).

### Library updates

- `tools.jackson.core` 3.1.6 (see the Jackson 3 migration under breaking changes). `com.fasterxml.jackson.core:jackson-annotations` stays at 2.21 — Jackson 3.1.x reuses the 2.x annotation artifact.
- `org.postgresql:postgresql` 42.7.10 → 42.7.13
- `org.apache.logging.log4j:log4j-core` 2.25.3 → 2.26.1
- `org.geotools` 34.5 → 35.1
- `org.slf4j:slf4j-api` 1.7.25 → 2.0.18 (log4j binding switched `log4j-slf4j-impl` → `log4j-slf4j2-impl`)
- `com.zaxxer:HikariCP` 4.0.3 → 7.1.0
- `com.github.ben-manes.caffeine:caffeine` 2.9.3 → 3.2.4
- `org.xerial:sqlite-jdbc` 3.47.0.0 → 3.53.4.0
- `org.freemarker:freemarker` 2.3.33 → 2.3.34 (2.3.33 was the Google App Engine variant of the artifact; 2.3.34 is the standard build). `org.apache.logging.log4j:log4j-1.2-api` is now a dependency of `hakunapi-simple-webapp-jakarta`, added alongside the FreeMarker upgrade.

## < 1.7.1

No Changelog available, see git history for releases 1.0.0 – 1.7.1.
