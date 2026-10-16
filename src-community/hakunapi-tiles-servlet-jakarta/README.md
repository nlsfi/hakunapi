# hakunapi-tiles-servlet-jakarta

Community module serving [OGC API - Tiles](https://ogcapi.ogc.org/tiles/) over
HTTP: the JAX-RS resources, and the `ApiExtension` that plugs them into
`hakunapi-simple-servlet-jakarta`.

The extension is found through `ServiceLoader`, so adding the module to the
classpath is all a deployment declares. With no tile layer configured it
publishes nothing and the service stays a plain OGC API - Features service.
Tile sources are separate modules that implement the SPIs of
[hakunapi-tiles-core](../hakunapi-tiles-core/README.md), which also documents
the `tiles.*` configuration.

## Resources

| path | what |
| --- | --- |
| `/tileMatrixSets`, `/tileMatrixSets/{tileMatrixSetId}` | tile matrix sets |
| `/tiles`, `/tiles/{tileMatrixSetId}/{tileMatrix}/{tileRow}/{tileCol}` | dataset vector tilesets and tiles |
| `/collections/{collectionId}/tiles`, `.../{tileMatrixSetId}/{tileMatrix}/{tileRow}/{tileCol}` | vector tilesets and tiles of a collection |
| `/collections/{collectionId}/map/tiles`, `.../{tileMatrixSetId}/{tileMatrix}/{tileRow}/{tileCol}` | map tilesets and tiles of a collection |

Resources are registered only for the kinds of layer configured. Each map tile
layer is published as a collection of its own, with no `items`.

## Status and support

Community module under `src-community/`: built and tested with the core
modules, not covered by the support the National Land Survey of Finland
provides for core modules. See [SUBMITTERS](SUBMITTERS) and
[CONTRIBUTING.md](../../CONTRIBUTING.md).
