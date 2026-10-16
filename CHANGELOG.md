# Changelog

## Unreleased

### Functional changes

- An unknown `crs`, `bbox-crs` or `filter-crs` SRID returns 400 instead of 500, and a collection `srid` missing from the global `srid` fails startup with a message naming both.
- New extension point `ApiExtensionFactory` (`fi.nls.hakunapi.core.extension`), found with `ServiceLoader`: a module on the classpath can add conformance classes, operations, landing page and collection links, collections not backed by a feature type, `f` values and OpenAPI schemas to the reference webapp without subclassing `HakunaContextListener`. A factory returns `null` when nothing is configured for it; one that throws fails startup. Extensions are `Closeable` and closed with the context. `CollectionInfo` gains `setItemType` so an extension can publish a collection that is not a feature collection. Nothing in this repository implements it yet.
- The `f` query parameter error (400) now lists every accepted value: metadata formats, configured output formats and those added by API extensions, not only the metadata formats.
- Fixed CSV output (`f=csv`) failing with a NullPointerException on the first geometry or floating-point value: no number formatter was ever set. CSV now uses the GeoJSON defaults: coordinates with 3 decimals for metres and 7 for degrees, double properties with up to 8 and float properties with up to 5.
- New community module `hakunapi-bytes` (Java 22+): a shared byte access layer for file-backed sources, covering local files (pread, or mmap or a whole-file in-memory copy as an opt-in) and HTTP Range with a shared block cache and adaptive read-ahead. It also reads an entry of a SOZip (seek-optimized ZIP) archive with random access, locally or over HTTP, and opens a location written in GDAL's `/vsizip/` / `/vsicurl/` syntax. No source uses it yet. It is built only on a 22+ JDK, via the new `ffm` profile in `src-community/pom.xml`.
- GeoJSON date and timestamp properties are written straight into the output buffer instead of via an intermediate `String`, cutting ~17% off the write time of a date-bearing feature collection. Output bytes are unchanged.
- Ordinates and fixed-decimal numbers are formatted eight digits at a time (SWAR) instead of one at a time, for every output format writing through `DefaultFloatingPointFormatter` (GeoJSON, JSON-FG, HTML, CSV). Output bytes are unchanged, with one fix: a custom `formatter.*` setting whose minimum decimal count is 1 or more no longer keeps one extra trailing zero (`0.5` rather than `0.50` for minimum 1, maximum 2).
- `FloatingPointFormatter` has new default `writeOrdinates` methods that format a coordinate's 2-4 ordinates in one call. `DefaultFloatingPointFormatter` uses them to convert two short digit groups at once (both integral parts in degrees, both fractions in metres). The default metre (0-3 decimals) and degree (0-7 decimals) settings take specialised paths, metres with the fraction read from a table. Existing implementations keep working unchanged. Callers of the `byte[]` methods must leave room for the widest value, not just the bytes written: `DefaultFloatingPointFormatter` now stores whole 8-byte words and may write up to 18 bytes from the offset (37 for an x, y pair) whatever offset it returns. All writers in this repository already reserve more than that.
- JSON-FG: the m ordinate of a 4D `geometry` (CRS84) is now written with the degree formatter like x, y and z. When `place` was in a projected CRS, m used to get the metre formatter's decimals.
- CSV: WKT geometries with Z or ZM coordinates now separate their coordinates with commas (`LINESTRING Z(1 2 3,4 5 6)`, previously `LINESTRING Z(1 2 34 5 6)`).
- `hakunapi-source-gpkg` reads rows one at a time into a reused container instead of batches of 250 (`BufferedResultSet` is removed from this module; `hakunapi-source-postgis` has its own copy). Geometry blobs are read through SQLite's own buffer instead of a `byte[]` per feature, and NULL checks are asked of SQLite by storage class. Output is unchanged.
- New community module `hakunapi-tiles-core`: the OGC API - Tiles model (tilesets, tile matrix sets, tile and layer types, the `VectorTileSource` and `MapTileSource` extension points), `tiles.*` configuration parsing and a `zoom-routed-vector` source that picks a backing source by zoom. Nothing serves tiles yet. `ConformanceClass` gains the OGC API - Tiles and tile matrix set URIs, and `HakunaConfigParser` a `getProperties()` accessor.
- New community module `hakunapi-tiles-servlet-jakarta`: serves OGC API - Tiles (tile matrix sets, vector tilesets and tiles of a collection or the dataset, map tilesets and tiles) through an `ApiExtensionFactory`, so it is enabled by being on the classpath and publishes nothing when no tile layer is configured. No tile source implements the SPIs yet.
- `hakunapi-simple-servlet-jakarta` supports pre-encoded responses: an entity of `PreEncodedBytes` or `PreEncodedOutput` is sent as is, skipped by the gzip interceptor, and `byte[]` is described as a binary string in OpenAPI.
- A service no longer needs a `collections` property: with none, it starts as a collectionless service, serving the common conformance classes and whatever API extensions publish.

## 2.0.0

For a full list of changes see: https://github.com/nlsfi/hakunapi/milestone/17

### Breaking changes (migration guide)

- **Java 21 required.** Minimum runtime/build raised from Java 11. Upgrade JDK before building or deploying.
- **Jakarta only.** All `javax.*` support dropped. Replace any `javax.servlet.*` / `javax.ws.rs.*` imports with `jakarta.*` equivalents. The `webapp-javax` module is gone — switch deployments to `webapp-jakarta`.
- **Jackson 3.** Migrated from Jackson 2 to Jackson 3. Custom serializers/deserializers, `ObjectMapper` configuration, and module dependencies must be updated to the Jackson 3 API (package `tools.jackson.*`).
- **Oracle source removed.** Modules `hakunapi-source-oracle` and `hakunapi-oracle-webapp-jakarta` dropped. Users needing Oracle Spatial data sources should pin hakunapi ≤ 1.x or maintain the module out-of-tree.
- **OpenTelemetry removed.** Modules `hakunapi-telemetry-opentelemetry` and `hakunapi-telemetry-webapp-jakarta` dropped. The `hakunapi-telemetry` JSON-log mode and NOP telemetry remain available; the `ServiceTelemetry`/`RequestTelemetry`/`TelemetryFactory` SPI in `hakunapi-core` is unchanged.
- **Community modules moved and unbundled.** Modules `hakunapi-smile`, `hakunapi-esbulk`, `hakunapi-gml` and `hakunapi-proj-jhe` moved from `src/` to the new `src-community/` folder and are no longer bundled in the reference `features.war` (`hakunapi-simple-webapp-jakarta`). Maven coordinates and Java packages are unchanged, and versions are still managed by the root POM. Deployments that enable `formats=...,smile` or `formats=...,esbulk`, or select the projection factory `fi.nls.hakunapi.proj.jhe.JHeProjectionTransformerFactory` via the `proj` configuration key, must now add the corresponding dependency to their own webapp and rebuild. `hakunapi-proj-gt` remains the bundled default for coordinate transformations. See [CONTRIBUTING.md](CONTRIBUTING.md#community-modules).
- **Global `srid` must list every collection SRID.** The global `srid` property is the CRS registry, and every SRID in `collections.<x>.srid` or `default.collections.srid` must be found in it. Otherwise a request using that CRS failed with HTTP 500. Add the missing SRIDs to the global `srid` list.

### Functional changes

- HTML `FeatureCollection` rendering revamped.
- Bbox SRID is now always set.
- Fixed HTML projection handling (incl. `bbox-crs` no-op projection case).
- Fixed broken maps on HTML feature pages: the proj4 and proj4leaflet CDN URLs pointed at files the packages do not publish, so jsDelivr generated them on the fly and their SRI hashes drifted.
- Removed obsolete `.github/workflows/deploy.yml` CI workflow.
- Documentation: added a table of selected dependencies to [docs/overview/modules.md](docs/overview/modules.md).
- New `WKBBacked` / `NavigableHakunaGeometry` geometry API in `hakunapi-core`: a geometry whose coordinates are already WKB bytes can be walked in place, without materializing a JTS object tree. `HakunaGeometry.toNavigable()` has a JTS-based default, so every backing keeps working. `HakunaGeometryEWKB` and `HakunaGeometryGPKG` implement it.
- Reprojection of 2D WKB-backed geometry now transforms the coordinates in place (`EWKBTransformer.transform`) instead of building and discarding a JTS tree per feature. Geometry with Z or M keeps the JTS path. The dimension is read per geometry from the WKB, so a column that declares Z but stores plain XY reprojects correctly.
- `HakunaGeometryGPKG` reads its WKB through the `ByteBuffer` rather than its backing array, so it also works for a direct buffer. No change on the JDBC path, whose blobs are always heap-backed.

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
