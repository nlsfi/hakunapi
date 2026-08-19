# hakunapi-esbulk

Community module providing an ElasticSearch Bulk API output format for Hakunapi,
using the media type `application/x-ndjson`.

The [ElasticSearch Bulk API](https://www.elastic.co/docs/api/doc/elasticsearch/operation/operation-bulk)
expects newline-delimited JSON (NDJSON), and this module writes feature data in
that variant so it can be fed directly into a bulk indexing request.

## Usage

Enable the format in a feature service configuration:

```
formats=geojson,esbulk
```

The format is discovered at runtime via `ServiceLoader`
(`META-INF/services/fi.nls.hakunapi.core.OutputFormatFactorySpi`), so it is enough
for the module to be on the classpath.

 Add it explicitly to your own deployment:

```xml
<dependency>
    <groupId>fi.nls.hakunapi</groupId>
    <artifactId>hakunapi-esbulk</artifactId>
</dependency>
```

The version is managed by the Hakunapi root POM, so it may be omitted when
building against Hakunapi as a parent.

## Limitations

Common geometry types (Point, LineString, Polygon, MultiPoint, MultiLineString and
MultiPolygon) are supported, but GeometryCollection is not.

## Status and support

This is a community module under `src-community/`. It is built and (automatically) tested together with the Hakunapi core modules, but it is not actively developed and is not covered by the support the National Land Survey of Finland provides for core modules. Maintenance depends on the submitters and other contributors.

See [SUBMITTERS](SUBMITTERS) for authors and [CONTRIBUTING.md](../../CONTRIBUTING.md)
for how to contribute.
