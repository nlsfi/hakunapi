# Configuration reference

Cheat sheet of every configuration key the core modules (`src/`) and the reference webapp (`webapp-jakarta/`) read. The community modules (`src-community/`, for example tiles, smile and esbulk) are not covered here; see their READMEs. For the modules behind them see [modules.md](modules.md); for a working file see [docker/cfg/addresses.properties](../../docker/cfg/addresses.properties).

**Keeping this page current:** a pull request that adds, renames, removes or changes the default of a configuration key is expected to update this page in the same PR.

Notation: `<id>` is a collection id, `<n>` a name you choose. "Default: -" means the key is simply absent when unset.

## Basics

- The configuration is a Java `.properties` file (the webapp reads it as UTF-8, and as ISO-8859-1 if that fails).
- Lists are comma-separated and each item is trimmed: `collections=a, b`.
- A blank value counts as unset for keys read through `HakunaConfigParser.get`/`getMultiple` and `PropertyUtil.getInt` (a blank or non-numeric int falls back to the default). Keys read raw from the properties keep the blank value: `api.*` (`api.title=` gives an empty title), `servers.<n>.*` (a blank `servers.<n>.url` gives an empty URL; a missing one fails startup with a `NullPointerException`), `getfeatures.limit.*` and `srid.<code>.*` (a blank `srid.<code>.geometryDimension` fails startup; blank `wrapXMin`/`wrapXMax` count as unset, but whitespace-only fails). `srid.<code>.*` and `getfeatures.limit.*` also get no `${}` expansion.
- Booleans are read with `Boolean.parseBoolean`: anything but `true` (any case) is `false`.
- `${NAME}` in a value is replaced from the environment variable `NAME`, else from the JVM system property `NAME`. An unknown name is left as `${NAME}`.

### Where the file is found

Tried in this order. A property is looked up as an environment variable first (name uppercased, `.` replaced by `_`, so `hakunapi.config.path` is `HAKUNAPI_CONFIG_PATH`), then as a JVM system property.

| Property | Meaning |
| --- | --- |
| `<context>.hakunapi.config.path` | Path of the file for the webapp deployed at `/<context>` |
| `<context>.hakuna.config.path` | Same, older name |
| `hakunapi.config.path` | Path of the file, for any context |
| `hakuna.config.path` | Legacy: a directory; the file is `<dir>/<context>.properties` |
| `HAKUNA_APPLICATION_JSON` | Env var or system property. JSON `{"<context>": {"key": "value"}}`; the entries are applied over the file's keys for that context |

```properties
# -Dhakunapi.config.path=/app/config/features.properties
# or:  export HAKUNAPI_CONFIG_PATH=/app/config/features.properties
```

## Service

### API metadata

| Key | Default | Description |
| --- | --- | --- |
| `api.title` | `hakunapi OGC API Features Server` | Title in OpenAPI and on the landing page |
| `api.version` | `0.0.1` | API version |
| `api.description` | `hakunapi OGC API Features Server` | Description |
| `api.termsOfService` | - | URL |
| `api.contact.name`, `api.contact.email`, `api.contact.url` | - | Contact is published if at least one is set |
| `api.license.name`, `api.license.url` | - | License is published only if `name` is set |
| `servers` | required | Comma-separated server names |
| `servers.<n>.url` | required per server | Base URL, used for every link the server generates. Trailing `/` is removed |
| `servers.<n>.description` | - | Description |

```properties
servers=dev
servers.dev.url=http://localhost:8080/features
servers.dev.description=Development server
```

### Links

Every link needs all four of `href`, `rel`, `type`, `title`.

| Key | Description |
| --- | --- |
| `api.links` | Names of extra links on the landing page |
| `api.links.<n>.href`, `.rel`, `.type`, `.title` | The link |
| `api.collections.links` | Names of extra links in the top-level `links` of `/collections` (JSON and HTML; the HTML page lists all but `self` and `alternate`) |
| `api.collections.links.<n>.href`, `.rel`, `.type`, `.title` | The link |

```properties
api.links=about
api.links.about.href=https://www.example.org/about
api.links.about.rel=about
api.links.about.type=text/html
api.links.about.title=About the service
```

### Languages (`locale`)

| Key | Description |
| --- | --- |
| `locale` | Languages served, comma-separated; the first is the default. Enables `lang` / `Accept-Language` negotiation |
| `locale.<n>.path` | Optional UTF-8 properties catalog overlaying the main config; path relative to the config file. Only title and description keys are accepted (`api.title`, `api.description`, `api.links.<n>.title`, `collections.<id>.title`, `collections.<id>.description`, `collections.<id>.links.<n>.title`, `default.collections.links.<n>.title`) |
| `locale.<n>.lang` | Language tag; default `<n>`, lowercased. Declaring the same language twice fails startup |

When `locale` is set every `schema.<n>` must resolve (through `schema.<n>.lang`, default `<n>`) to a language listed in `locale`, or startup fails.

### Security schemes (OpenAPI description)

| Key | Description |
| --- | --- |
| `security` | Names of schemes. None configured means none published |
| `security.<n>.type` | `http` (published as `basic`) or `apiKey`. Anything else fails startup |
| `security.<n>.in` | For `apiKey`, required: `header`, `query` or `cookie` |
| `security.<n>.name` | For `apiKey`, required: header / query parameter / cookie name |

This only describes the scheme in the API definition; Hakunapi does not enforce it.

### Request handling

| Key | Default | Description |
| --- | --- | --- |
| `getfeatures.limit.default` | `1000` | `limit` when the request has none. An unparsable value falls back to the default |
| `getfeatures.limit.max` | `10000` | Largest accepted `limit` |
| `hakuna.cors` | `true` | Register the CORS filter |
| `hakuna.gzip` | `true` | Register gzip response/request filters |
| `hakuna.content-length-buffer` | `8192` | Jersey `OUTBOUND_CONTENT_LENGTH_BUFFER`, in bytes |

### Coordinate reference systems

CRS:84 (`84`) and EPSG:4326 (`4326`) are always known, and `srid.84.*` / `srid.4326.*` keys are ignored. Every SRID used by a collection must be listed in `srid`, otherwise startup fails.

| Key | Default | Description |
| --- | --- | --- |
| `srid` | `84,4326` | Extra known SRIDs, comma-separated |
| `srid.<code>.latLon` | `false` | Axis order is lat,lon (north,east) |
| `srid.<code>.degrees` | `false` | Units are degrees |
| `srid.<code>.geometryDimension` | `XY` | `XY`, `XYZ` or `XYZM` (case-sensitive) |
| `srid.<code>.wrapXMin`, `srid.<code>.wrapXMax` | - | Antimeridian wrap range; set both or neither |
| `srid.<code>.proj4` | - | proj4 definition of a projected CRS. Used by the HTML pages only: the Leaflet background map of that CRS needs it (together with `formats.html.<srid>.tileUrl`). Not used for reprojection |

```properties
srid=3067,4258
srid.3067.latLon=false
srid.4258.latLon=true
srid.4258.degrees=true
```

### Output formats

`formats` lists the enabled formats; the default is `json` only. Keys under `formats.<n>.` are passed to the format; `formats.<n>.type` defaults to `<n>`, so the name normally is the type.

| Type | Module | Keys |
| --- | --- | --- |
| `json` | hakunapi-geojson | `forceLonLat` (default `false`), `formatter.degrees`, `formatter.meters` |
| `jsonfg` | hakunapi-jsonfg | `formatter.degrees`, `formatter.meters` |
| `html` | hakunapi-html | `dir`, `tileUrl`, `tileAttribution`, `tileOriginX`, `tileOriginY`, `<srid>.tileUrl` (and the other three per SRID) |
| `gpkg` | hakunapi-gpkg | `dir` (required) |
| `csv` | hakunapi-csv | none |

A format whose factory is not on the classpath, or that rejects its keys, is skipped without an error only when its factory's `canCreate` says no (a `gpkg` or `html` with a missing or unusable `dir` silently disappears). An unknown format type is also silently ignored (no log). A bad `formatter.degrees`/`formatter.meters` value (not six integers) in `json` or `jsonfg` fails startup instead. If `formats` is set, `json` is only enabled when listed; it is the default only when no format ends up enabled. The GML writer has no factory and cannot be enabled.

| Key | Default | Description |
| --- | --- | --- |
| `formats.<n>.formatter.degrees` | `0,5,0,8,0,7` | Six integers: min and max decimals for float, double and ordinate values, for degree coordinates |
| `formats.<n>.formatter.meters` | `0,5,0,8,0,3` | Same, for metre coordinates |
| `formats.json.forceLonLat` | `false` | Write coordinates lon,lat whatever the CRS axis order |
| `formats.html.dir` | - | Directory of Freemarker templates; they override the built-in ones, which remain the fallback. Must be an existing directory or the format is skipped |
| `formats.html.tileUrl` | OpenStreetMap | Background tile URL template with `{z}/{x}/{y}` |
| `formats.html.tileAttribution` | OpenStreetMap credit | HTML attribution. The OSM defaults apply only if none of the four tile keys is set |
| `formats.html.tileOriginX`, `formats.html.tileOriginY` | - | Origin of a non-WebMercator tile grid |
| `formats.html.<srid>.tileUrl` etc. | - | Background map for requests in that SRID; the same four keys per SRID |
| `formats.gpkg.dir` | required | Writable directory for the generated GeoPackage files |

```properties
formats=json,html,gpkg,csv
formats.html.3067.tileUrl=https://example.org/wmts/{z}/{y}/{x}.png
formats.html.3067.tileOriginX=-548576
formats.html.3067.tileOriginY=8388608
formats.gpkg.dir=/tmp/hakunapi
formats.json.formatter.degrees=0,5,0,8,0,7
```

### Telemetry

| Key | Default | Description |
| --- | --- | --- |
| `telemetry.mode` | `NOP` | `NOP` (off) or `log-json` (hakunapi-telemetry) |
| `telemetry.logger` | - | Logger name that receives the JSON lines. Effectively required: without it nothing is logged. An unknown `telemetry.mode` (case-sensitive) silently means `NOP` (only an info log) |
| `telemetry.collections` | - | `*` for all collections, or a list of ids. Unset means no telemetry. With a list, `telemetry.collections.<id>.name` sets the logged name of that collection (default the id); with `*` that key is ignored |
| `telemetry.fields` | - | Names under which request headers are logged |
| `telemetry.fields.<h>.header` | `<h>` | The actual request header whose value is logged as `<h>` |

### JSON Schema files (titles, descriptions, code lists)

| Key | Default | Description |
| --- | --- | --- |
| `schema` | - | Names of schema files; typically the language |
| `schema.<n>.path` | required | Path, relative to the config file (an entry without one is skipped with a warning) |
| `schema.<n>.lang` | `<n>` | Language tag, lowercased |

```properties
schema=en,fi
schema.en.path=addresses_schema_en.json
schema.fi.path=addresses_schema_fi.json
```

### Sources

| Key | Default | Description |
| --- | --- | --- |
| `db.classes` | `fi.nls.hakunapi.simple.postgis.PostGISSimpleSource` | Source classes to load. Use `fi.nls.hakunapi.source.gpkg.GpkgSimpleSource` for GeoPackage, or both comma-separated. A class that cannot be loaded is logged and skipped |
| `db` | - | Names of databases configured inline, see [Databases](#databases) |
| `joins` | - | PostGIS only, experimental: names of table joins, see [Joins](#postgis-joins-experimental) |

## Collections

| Key | Default | Description |
| --- | --- | --- |
| `collections` | - | Comma-separated collection ids. With none the service starts collectionless (useful with API extensions) |
| `collections.<id>.title` | from the source, else `<id>` | Title. A `title` in a `schema` file overrides it |
| `collections.<id>.description` | from the source, else the title | Description. A `description` in a `schema` file overrides it |
| `collections.<id>.type` | `default.collections.type`, else `pg` | Source type: `pg` or `gpkg`. Only consulted when more than one source is loaded |
| `collections.<id>.srid` | `default.collections.srid` | Comma-separated SRIDs the collection can be served in. Must all be in the global `srid`. In practice required for a geometry collection: the storage SRID must be in this list |
| `collections.<id>.proj` | `fi.nls.hakunapi.proj.gt.GeoToolsProjectionTransformerFactory` | `ProjectionTransformerFactory` class. A class that fails to load is logged and ignored |
| `collections.<id>.metadata` | - | Extra collection metadata: a JSON object, or the path to a readable JSON file. A relative path is resolved against the properties file itself as if it were a directory, so only an absolute path works in practice |
| `collections.<id>.extent.spatial.crs84` | - | `minX,minY,maxX,maxY` in CRS:84. The warning about a missing spatial extent is logged only when neither this nor `extent.spatial` is set. If both are set this wins. Exactly 4 values, otherwise startup fails |
| `collections.<id>.extent.spatial` | - | Same, 4 values or 6 (with Z). Must be within lon -180..180, lat -90..90. Needs a geometry |
| `collections.<id>.extent.temporal` | - | Start and end instants, UTC with a `Z` (`2020-01-01T00:00:00Z`). Needs `time` |
| `collections.<id>.time` | - | Property names used by the `datetime` parameter |
| `collections.<id>.time.exclusive` | - | Subset of `time` whose bounds are exclusive; the others are inclusive |
| `default.collections.type` | `pg` | Default for `collections.<id>.type` |
| `default.collections.srid` | - | Default for `collections.<id>.srid` |

```properties
collections=buildings
collections.buildings.title=Buildings
collections.buildings.srid=3067,4258
collections.buildings.extent.spatial.crs84=19.0,59.7,31.6,70.1
collections.buildings.time=created
collections.buildings.extent.temporal=2020-01-01T00:00:00Z,2026-01-01T00:00:00Z
```

### Collection links

Each link needs `href`, `rel`, `type` and `title`. `default.collections.links` apply to every collection and come before the collection's own.

| Key | Description |
| --- | --- |
| `default.collections.links` | Link names added to every collection |
| `default.collections.links.<n>.href`, `.rel`, `.type`, `.title` | The link |
| `collections.<id>.links` | Link names for this collection |
| `collections.<id>.links.<n>.href`, `.rel`, `.type`, `.title` | The link |

### Pagination

| Key | Default | Description |
| --- | --- | --- |
| `collections.<id>.pagination.strategy` | `cursor` | `cursor`, `offset` or `hybrid` |
| `collections.<id>.pagination` | the id property | Properties to order by |
| `collections.<id>.pagination.order` | `ASC` for each | `ASC` or `DESC` per entry of `pagination` |
| `collections.<id>.pagination.unordered` | `false` | With no `pagination`, do not order at all |

### Queryables and static filters

`parameters` makes properties filterable with `?name=value`. The properties they refer to are also the collection's queryables: they are listed in `/collections/<id>/queryables`, and a CQL2 `filter` can only use these properties (any other fails with `Non queryable property`).

| Key | Default | Description |
| --- | --- | --- |
| `collections.<id>.parameters` | - | Names of query parameters |
| `collections.<id>.parameters.<p>.prop` | `<p>` | Property it filters. `$id` is the id, `$geometry` (or `$geom`) the geometry |
| `collections.<id>.parameters.<p>.name` | the property name | Query parameter name |
| `collections.<id>.parameters.<p>.desc` | `Filter the collection by <name>` | OpenAPI description |
| `collections.<id>.parameters.<p>.impl` | `fi.nls.hakunapi.simple.servlet.operation.param.DefaultFilterMapper` | Class that turns the value into a filter, a `FilterMapper` or `CustomizableGetFeatureParam` |
| `collections.<id>.parameters.<p>.impl.arg` | - | Argument handed to the class |

Ready-made classes (package `fi.nls.hakunapi.simple.servlet.operation.param`):

| Class | `impl.arg` |
| --- | --- |
| `DefaultFilterMapper` | none; equality |
| `NotEqualToFilterMapper` | none |
| `LikeFilterMapper` | `caseInsensitive[,wild[,single[,escape]]]`; defaults `false`, `*`, `?`, `\` |
| `DatetimeRangeMapper`, `DateRangeMapper` | an integer bit mask (1 = start inclusive, 2 = end inclusive), or two flags from `GT`, `GTE`, `LT`, `LTE` (e.g. `GTE,LT`), applied in order on top of both-inclusive. No arg leaves the mask at 0: both bounds exclusive |
| `TimestampParam` | name of the end-time property (an unknown name means no end property); the filtered property must be `timestamptz` |
| `ArrayOperationParam` | a `FilterOp` name, default `ARRAY_OVERLAPS`, which is also the only accepted value; array properties only |
| `WKTGeometryOperationParam` | required: `INTERSECTS` (the only operation; no arg fails startup); geometry properties only |
| `BboxExtraPropertyParam` | none; geometry properties only |

Static filters are always applied to the collection:

| Key | Description |
| --- | --- |
| `collections.<id>.filters` | Filter names |
| `collections.<id>.filters.<f>.type` | required: `=`, `IN`, `!=`, `<>`, `NOT IN`, `>`, `<`, `>=`, `<=`, `BETWEEN`, `IS NULL`, `IS NOT NULL`, `AND`, `OR` |
| `collections.<id>.filters.<f>.prop` | required except for `AND`/`OR`: property name |
| `collections.<id>.filters.<f>.value` | Comma-separated value(s): one for `>`, `<`, `>=`, `<=`; exactly two for `BETWEEN`; several for `=`/`IN` (OR) and `!=`/`<>`/`NOT IN` (AND). For `AND`/`OR`, the names of other filters |

```properties
collections.parcels.parameters=name,kind
collections.parcels.parameters.name.impl=fi.nls.hakunapi.simple.servlet.operation.param.LikeFilterMapper
collections.parcels.parameters.name.impl.arg=true
collections.parcels.filters=active
collections.parcels.filters.active.type=NOT IN
collections.parcels.filters.active.prop=status
collections.parcels.filters.active.value=deleted,draft
```

### Response cache

| Key | Default | Description |
| --- | --- | --- |
| `collections.<id>.cache` | `false` | Enable the cache |
| `collections.<id>.cache.size` | `100` | Entries |
| `collections.<id>.cache.refresh` | `60000` | Milliseconds |
| `collections.<id>.cache.expire` | `120000` | Milliseconds; must be at least `refresh`. All three must be positive |

## Properties and columns

These keys are read by both sources unless noted. `<p>` is the property name as published.

| Key | Default | Description |
| --- | --- | --- |
| `collections.<id>.db` | required | Database name, see [Databases](#databases) |
| `collections.<id>.table` | required for `pg`; `<id>` for `gpkg` | Table |
| `collections.<id>.schema` | `public` | `pg` only: database schema |
| `collections.<id>.id.mapping` | required for `pg`; the primary key for `gpkg` | Id column. `pg` accepts several (comma-separated) for a composite id |
| `collections.<id>.id.table` | the table | `pg`: table of each id column |
| `collections.<id>.id.transformer` | required for a composite id | `pg`: `ValueTransformer` class that joins the parts |
| `collections.<id>.id.transformer.arg` | - | `pg`: argument for it |
| `collections.<id>.geometry.mapping` | - | `pg`: geometry column. `gpkg` discovers it |
| `collections.<id>.geometry.table` | the table | `pg` |
| `collections.<id>.srid.storage` | `0` | SRID of the stored geometry, used only when the database reports none (0), as for a view or an untyped column. If the database knows the SRID it wins, and a different value here only logs a warning. If neither knows it, startup fails (`srid is unknown`). For `pg` the result must also be in the collection's `srid`; `gpkg` reads the key too |
| `collections.<id>.properties` | all columns except the id and geometry columns | Published properties. Unset or `*` discovers the columns; `*,~col` leaves one out (`~col` is honoured only after a leading `*`). Discovery always skips the geometry column. `pg`: the id column is skipped only with a single `id.mapping`, a composite id is not excluded. `gpkg`: the primary key is always skipped |
| `collections.<id>.properties.<p>.mapping` | `<p>` | Column. A single-quoted value (`'text'`) is a constant instead of a column |
| `collections.<id>.properties.<p>.table` | the table | `pg` only |
| `collections.<id>.properties.<p>.enum` | - | Allowed values; `INT`, `LONG` and `STRING` properties only. Published as an enumeration |
| `collections.<id>.properties.<p>.transformer` | - | `ValueTransformer` class |
| `collections.<id>.properties.<p>.transformer.arg` | - | Argument for it |
| `collections.<id>.properties.<p>.unique` | `false` | Values are unique |
| `collections.<id>.properties.<p>.hidden` | `false` | Not written to the output |
| `collections.<id>.casei` | `lower` | `pg` only: `lower` or `upper` (any case), the function applied for case-insensitive comparisons. Any other value fails startup with a `NoSuchElementException` (`Optional.get()` on an empty match) |
| `collections.<id>.sourceproj` | `false` | `pg` only: reproject in the database instead of in Hakunapi when the requested SRID differs from the storage SRID |

Transformers are in `fi.nls.hakunapi.core.transformer`: `NullIf`, `IfNull`, `PrefixTransformer`, `LongToString`, `StringManipulator`, `StringJoinerTransformer`, `JSONCodeListTransformer`, `TimestampToDateTransformer`, `TimestamptzToDateTransformer`, `FixedLengthStringToIntegerTransformer`, `TransformChain`. Their `transformer.arg` formats are class specific; read the class.

```properties
collections.buildings.db=db
collections.buildings.table=building
collections.buildings.id.mapping=id
collections.buildings.geometry.mapping=geom
collections.buildings.properties=name,use,source
collections.buildings.properties.use.mapping=building_use
collections.buildings.properties.use.enum=residential,industrial
collections.buildings.properties.source.mapping='Cadastre'
```

### Databases

`collections.<id>.db` names a database. What the name means depends on the source.

**PostGIS (`pg`)** - the name is one of:

- `java:comp/env/<jndi-name>`: a JNDI `DataSource` from the container.
- listed in `db=a,b`: then every `db.<name>.*` key is a [HikariCP](https://github.com/brettwooldridge/HikariCP#gear-configuration-knobs-baby) setting, with the prefix removed.
- otherwise: `<name>.properties` next to the main config file, holding HikariCP settings. `${NAME}` placeholders work in it.

```properties
db=main
db.main.dataSourceClassName=org.postgresql.ds.PGSimpleDataSource
db.main.dataSource.serverName=${DB_HOST}
db.main.dataSource.databaseName=gis
db.main.dataSource.user=reader
db.main.dataSource.password=${DB_PASSWORD}
```

**GeoPackage (`gpkg`)** - the name is first tried as a file: an absolute path, or one relative to the config file's directory. The file is opened with a pool of up to 50 connections. If no such file exists the name is handled as for `pg` (`db=` list with `db.<name>.*`, else `<name>.properties`), with `driverClassName` defaulting to the SQLite driver.

### PostGIS joins (Experimental)

Joins are only weakly supported and may change.

| Key | Description |
| --- | --- |
| `joins` | Join names |
| `joins.<j>.type` | `oneToOne`, `oneToMany`, `manyToOne` or `manyToMany` (any case) |
| `joins.<j>.leftTable`, `.leftColumn`, `.rightTable`, `.rightColumn` | The joined columns |
| `joins.<j>.joinTable`, `.joinColumnLeft`, `.joinColumnRight` | `manyToMany` only: the link table |

## Not read by the code

Seen in example files or older docs, but nothing in `src/`, `src-community/` or `webapp-jakarta/` reads them (a test-only data source reads `writeNulls`):

- `collections.<id>.writeNulls`
- `collections.<id>.geometry.storageSrid` (use `collections.<id>.srid.storage`)
- `telemetry.module.<n>`: a constant exists, nothing reads it
- `formats=geojson` in [modules.md](modules.md): the type is `json`
