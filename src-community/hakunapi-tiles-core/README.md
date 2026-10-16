# hakunapi-tiles-core

Community module providing the [OGC API - Tiles](https://ogcapi.ogc.org/tiles/)
model for Hakunapi: tile layers, tile matrix sets, the response schemas, and the
configuration parser shared by every tile source.

This module has no tile source of its own. It defines the two SPIs a source
implements, and a deployment adds the source modules it needs:

- `MapTileSource` — pre-baked **map** (raster) tiles, served under
  `/collections/{collectionId}/map/tiles`
- `VectorTileSource` — **vector** tiles rendered from a feature collection,
  served under `/collections/{collectionId}/tiles`

Sources are discovered at runtime via `ServiceLoader`, so a source is available
simply by being on the classpath.

## Configuration

Tile configuration lives in the same `.properties` file as the rest of the
service. Layers are listed by id, and each names the source type that backs it:

```
tiles.layers=my_collection,my_basemap

tiles.layers.my_collection.type=vectortile-generator
tiles.layers.my_collection.title=My Collection Tiles
tiles.layers.my_collection.tileMatrixSets=WebMercatorQuad

tiles.layers.my_basemap.type=gpkg
tiles.layers.my_basemap.title=My Basemap
tiles.layers.my_basemap.tileMatrixSets=WebMercatorQuad
```

The `type` may be omitted when exactly one source of that kind is on the
classpath. A service-wide default applies to layers that do not set their own:

```
default.tiles.layers.type=gpkg
default.tiles.layers.tileMatrixSets=WebMercatorQuad
```

### Publishing vector tiles for every collection

`tiles.vector=true` publishes a vector tile layer for every feature collection
that has a geometry, sharing the collection id, with no per-collection
configuration. Explicit `tiles.layers.<id>` definitions take precedence. It
requires exactly one `VectorTileSource` on the classpath.

### Tile matrix sets

`WebMercatorQuad` is built in. Further schemes are defined compactly as a CRS
plus an extent and a resolutions array (ground units per pixel, one entry per
zoom level):

```
tileMatrixSets=ETRS-TM35FIN
tileMatrixSets.ETRS-TM35FIN.title=ETRS-TM35FIN
tileMatrixSets.ETRS-TM35FIN.srid=3067
tileMatrixSets.ETRS-TM35FIN.bbox=-548576,6291456,1548576,8388608
tileMatrixSets.ETRS-TM35FIN.resolutions=8192,4096,2048,1024,512,256,128,64,32,16,8,4,2,1
```

Per level the cell size is the resolution, the scale denominator is
`resolution / 0.00028` (OGC 17-083r4 §6.2), and `matrixWidth`/`matrixHeight` are
derived from the extent — the extent must be a whole number of tiles at every
level. Tile matrix ids default to the level index; override with `matrixIds`.

## Usage

This module is a dependency of every tile source, so it is rarely added
directly. Serving tiles over HTTP additionally needs
`hakunapi-tiles-servlet-jakarta`.

```xml
<dependency>
    <groupId>fi.nls.hakunapi</groupId>
    <artifactId>hakunapi-tiles-core</artifactId>
</dependency>
```

The version is managed by the Hakunapi root POM, so it may be omitted when
building against Hakunapi as a parent.

## Status and support

This is a community module under `src-community/`. It is built and
(automatically) tested together with the Hakunapi core modules, but it is not
covered by the support the National Land Survey of Finland provides for core
modules.

See [SUBMITTERS](SUBMITTERS) for authors and
[CONTRIBUTING.md](../../CONTRIBUTING.md) for how to contribute.
