# hakunapi-smile

Community module providing an unofficial Jackson Smile (GeoJSON) output format
for Hakunapi, using the media type `application/x-jackson-smile`.

[Jackson Smile](https://en.wikipedia.org/wiki/Smile_(data_interchange_format)) is a
binary serialization of the generic JSON data model, which means tools that operate
on JSON may be used with Smile as well, as long as a proper encoder/decoder exists.

This module writes GeoJSON objects (feature collections, features, geometries and
properties) in the Jackson Smile encoding.

## Usage

Enable the format in a feature service configuration:

```
formats=geojson,smile
```

The format is discovered at runtime via `ServiceLoader`
(`META-INF/services/fi.nls.hakunapi.core.OutputFormatFactorySpi`), so it is enough
for the module to be on the classpath.

Add it explicitly to your own deployment:

```xml
<dependency>
    <groupId>fi.nls.hakunapi</groupId>
    <artifactId>hakunapi-smile</artifactId>
</dependency>
```

The version is managed by the Hakunapi root POM, so it may be omitted when
building against Hakunapi as a parent.

## Status and support

This is a community module under `src-community/`. It is built and (automatically)
tested together with the Hakunapi core modules, but it is not actively developed and
is not covered by the support the National Land Survey of Finland provides for core
modules. Maintenance depends on the submitters and other contributors.

See [SUBMITTERS](SUBMITTERS) for authors and [CONTRIBUTING.md](../../CONTRIBUTING.md)
for how to contribute.
