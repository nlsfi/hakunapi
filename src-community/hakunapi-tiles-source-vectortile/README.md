# hakunapi-tiles-source-vectortile

Community module rendering [Mapbox Vector Tiles](https://protomaps.com/docs/mvt)
from Hakunapi feature collections on the fly, served as OGC API - Tiles vector
tiles under `/collections/{collectionId}/tiles`.

No tiles are pre-baked and nothing is cached: a tile is produced per request
straight from the feature source, streamed out as it is encoded.

The MVT pipeline is written in-tree and pulls in **no third-party dependency** —
the protobuf envelope encoder, the geometry clipping and simplification, and the
layer encoder are all plain Java. Geometry is read through
`NavigableHakunaGeometry`, whose bulk coordinate reads let the world-to-tile
transform be fused into a single pass with no intermediate arrays.

## Configuration

The source registers as type `vectortile-generator`. A layer names the feature
collections it renders (defaulting to the layer id) and the generator to use:

```
tiles.layers=my_collection
tiles.layers.my_collection.type=vectortile-generator
tiles.layers.my_collection.title=My Collection Tiles
tiles.layers.my_collection.tileMatrixSets=WebMercatorQuad
tiles.layers.my_collection.vectortile.generator=hakunapi
```

| key | meaning |
| --- | --- |
| `vectortile.generator` | `hakunapi` (the in-JVM MVT generator) or `empty` |
| `vectortile.collections` | feature collections to render, default: the layer id |
| `vectortile.format` | media type advertised, default `application/vnd.mapbox-vector-tile` |
| `vectortile.passthroughParams` | inbound query params forwarded to the feature query |

The generator may be set service-wide instead of per layer:

```
default.tiles.layers.vectortile.generator=hakunapi
```

`empty` produces a valid but empty tile. It exists for testing a deployment's
routing, conformance and metadata without exercising the encoder.

Combined with `tiles.vector=true` (see
[hakunapi-tiles-core](../hakunapi-tiles-core/README.md)), every geometry
collection is published as vector tiles with no per-collection configuration.

## Geometry

A ring is clipped in float coordinates and snapped to the MVT grid afterwards.
Because snapping can move a vertex across an edge and so reverse a ring, each
hole's winding is verified against its exterior's and the hole reversed when
snapping flipped it.

## Usage

```xml
<dependency>
    <groupId>fi.nls.hakunapi</groupId>
    <artifactId>hakunapi-tiles-source-vectortile</artifactId>
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
