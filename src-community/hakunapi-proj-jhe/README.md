# hakunapi-proj-jhe

Community module providing an alternative implementation of the Hakunapi
`ProjectionTransformer` coordinate transformation abstraction defined in
`hakunapi-core`.

Custom coordinate transformations with all code bundled in this module, without
external dependencies. It supports the EUREF-FIN based coordinate reference systems
commonly used in Finland, and transformations to and from the WGS84 reference:

- EPSG:4258 (EUREF-FIN)
- EPSG:3067 (ETRS-TM35FIN)
- EPSG:3046-3048 (ETRS-TMnn)
- EPSG:3873-3885 (ETRS-GKnn)
- EPSG:4326 (WGS84 geographic coordinates)

The core alternative is `hakunapi-proj-gt`, which is based on GeoTools, covers the
full EPSG dataset, and is the implementation bundled by default.

## Usage

The implementation is selected by class name in the feature service configuration:

```
proj=fi.nls.hakunapi.proj.jhe.JHeProjectionTransformerFactory
```

When the `proj` key is omitted, Hakunapi defaults to
`fi.nls.hakunapi.proj.gt.GeoToolsProjectionTransformerFactory`.

Because the factory is loaded reflectively from this configuration value, the class
and package names of this module must remain stable.

Add it explicitly to your own deployment:

```xml
<dependency>
    <groupId>fi.nls.hakunapi</groupId>
    <artifactId>hakunapi-proj-jhe</artifactId>
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
