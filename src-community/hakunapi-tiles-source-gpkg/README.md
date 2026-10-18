# hakunapi-tiles-source-gpkg

Community module serving pre-baked tiles from a
[GeoPackage](https://www.geopackage.org/) tile pyramid. Two sources, one per
OGC `dataType`:

- **`gpkg`** — map (raster) tiles, under `/collections/{collectionId}/map/tiles`
- **`gpkg-vector`** — vector tiles, under `/collections/{collectionId}/tiles`,
  the same path a rendering source serves. This is the read side of
  `hakunapi-tiles-generator`: it publishes a pyramid generated offline, so a
  collection can switch from rendering on every request to serving precomputed
  tiles without its tiles moving to a different URL.

A GeoPackage does not record its tile format, and MVT has no magic bytes, so
`gpkg-vector` declares `application/vnd.mapbox-vector-tile` rather than sniffing
it; `gpkg` sniffs PNG/JPEG/WEBP as before. Both share the tile-domain half
(`GpkgTiles`): opening the file, validating the pyramid table, and mapping tile
matrices to `zoom_level` by ground resolution.

Tiles are read straight out of the GeoPackage's tile table and relayed verbatim.
The tile matrix set the layer is served in is matched against the GeoPackage's
own `gpkg_tile_matrix` rows by pixel size, so the file's zoom levels line up
with the scheme's.

## Configuration

The source registers as type `gpkg`.

```
tiles.layers=basemap
tiles.layers.basemap.type=gpkg
tiles.layers.basemap.title=Basemap
tiles.layers.basemap.tileMatrixSets=WebMercatorQuad

tiles.layers.basemap.gpkg.path=/data/basemap.gpkg
```

| key | meaning |
| --- | --- |
| `gpkg.path` | path to the GeoPackage file (required) |
| `gpkg.table` | tile table name, default: the layer id |
| `gpkg.format` | media type advertised; sniffed from the stored tiles when unset |
| `gpkg.resolutionTolerance` | relative tolerance matching the scheme's resolutions against `gpkg_tile_matrix` pixel sizes |
| `gpkg.contentEncoding` | `gzip` when the stored tiles are gzip-compressed and cannot be detected by their magic bytes |

The media type is detected from the tile bytes themselves when `gpkg.format` is
not given, so a PNG or JPEG pyramid needs no configuration.

Note that GeoPackage tile rows are numbered from the top down, matching
WebMercatorQuad, so no row flipping is applied.

## Dependencies

This module uses `sqlite-jdbc` to read the GeoPackage, the same driver
`hakunapi-source-gpkg` uses. It is declared here rather than in
`hakunapi-tiles-core`, so a deployment serving only WMTS or PMTiles does not
pull it in.

## Usage

```xml
<dependency>
    <groupId>fi.nls.hakunapi</groupId>
    <artifactId>hakunapi-tiles-source-gpkg</artifactId>
</dependency>
```

The source is discovered at runtime via `ServiceLoader`, so it is enough for the
module to be on the classpath. Serving the tiles over HTTP additionally needs
`hakunapi-tiles-servlet-jakarta`.

## Status and support

This is a community module under `src-community/`. It is built and
(automatically) tested together with the Hakunapi core modules, but it is not
covered by the support the National Land Survey of Finland provides for core
modules.

See [SUBMITTERS](SUBMITTERS) for authors and
[CONTRIBUTING.md](../../CONTRIBUTING.md) for how to contribute.
