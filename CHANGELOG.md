# Changelog

## 2.1.0

### Internationalization / Localization

- **Language negotiation for descriptive elements.** The landing page,
  `/collections` and `/collections/{collectionId}` honour a `lang` query
  parameter and the `Accept-Language` header, and report `Content-Language`.
  `lang` wins over `Accept-Language`; an unsupported value falls back to the
  first declared language. Titles and descriptions come from optional
  per-language catalogs declared with `locale=`:

  ```properties
  locale=en,fi
  locale.fi.path=messages_fi.properties
  ```

  A catalog is a sparse UTF-8 overlay on the main config, resolved per key, so
  partial translations are fine and a declared language needs no catalog at all.
  Only `api.title`, `api.description`, `collections.<id>.title` and
  `collections.<id>.description` may be translated; any other key fails at
  startup naming the key. Link titles are not localizable — declare one link per
  language instead, each with its own `api.links.<name>.hreflang`, which is now
  supported. See `docker/cfg/addresses.properties` for a worked example.

  Multi-language resources emit `rel=alternate` links with `hreflang`, and
  `lang` is propagated into generated links. `Link.hreflang` is now populated
  (previously never set). `/collections` advertises the union of its
  collections' languages.

  Configuring one language or none is unchanged behaviour. Not yet localizable:
  hakunapi's own English strings in JSON and HTML chrome, and the OpenAPI
  document at `/api`.

- Behaviour change to `/collections/{collectionId}/schema`: an unmatched `lang`
  parameter previously fell through to the non-localized defaults. It now falls
  back to the first configured language, as an unmatched `Accept-Language`
  already did.

- When `locale=` is configured, every `schema=` entry must resolve to a declared
  language or startup fails naming the schema. The language is
  `schema.<name>.lang`, defaulting to the schema's name, so `schema=en,fi`
  alongside `locale=en,fi` needs no change while a schema named for anything
  else must now declare its language:

  ```properties
  schema=my_schema
  schema.my_schema.path=my_schema.json
  schema.my_schema.lang=fi
  ```

  Services with no `locale=` are unaffected.

### Functional changes

- `collectionInfo.ftl` shows the collection id alongside its title and
  description, and links to the collection's schema. `collections.ftl` lists
  entries as `collection_id (Title)`.

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
- `org.apache.logging.log4j:log4j-core` 2.25.3 → 2.26.0
- `org.geotools` 34.2 → 35.1
- `org.slf4j:slf4j-api` 1.7.25 → 2.0.18 (log4j binding switched `log4j-slf4j-impl` → `log4j-slf4j2-impl`)
- `com.zaxxer:HikariCP` 4.0.3 → 7.1.0
- `com.github.ben-manes.caffeine:caffeine` 2.9.3 → 3.2.4
- `org.xerial:sqlite-jdbc` 3.47.0.0 → 3.53.2.0

## < 1.7.1

No Changelog available, see git history for releases 1.0.0 – 1.7.1.
