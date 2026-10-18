# hakunapi-tiles-generator

Community module: a command-line tool that renders a vector tile pyramid offline
into a [GeoPackage](https://www.geopackage.org/), from the feature sources a
normal hakunapi service configuration describes.

Reading such a pyramid back is already implemented — `hakunapi-tiles-source-gpkg`
serves a precomputed tile table — so a generated file can be served with no
feature database in the request path.

Tiles are encoded by the same `VectorTileGenerator` the server renders on-the-fly
tiles with, so this tool is a driver rather than a second rendering
implementation.

## Usage

```
hakunapi-tiles-generator [options] <config.properties> <control.properties> <output.gpkg>

  --min-zoom <n>    coarsest zoom to write (default: the control file's minimum)
  --max-zoom <n>    finest zoom to write (default: the control file's maximum)
  --bbox <minx,miny,maxx,maxy>
                    limit to this extent, in the tiling scheme's CRS
  --threads <n>     parallel render workers (default: available processors)
  --generator <id>  vector tile generator type (default: hakunapi)
```

The system property `-Dhakunapi.tiles.block=<n>` sets the side, in tiles, of the square blocks rendered together (default 4; 1 disables blocks).

The module is not bundled in the reference webapp. Build it and run its
`jar-with-dependencies`, adding the feature source and projection factory your
configuration names:

```
mvn -pl src-community/hakunapi-tiles-generator -am install
java -cp target/hakunapi-tiles-generator.jar:<source>.jar:<proj>.jar \
    fi.nls.hakunapi.tiles.generator.TilesGenerator service.properties control.properties tiles.gpkg
```

## The two configuration files

The **service properties file** is an ordinary hakunapi configuration and says
what the data *is*: `db.classes`, `collections.*`, and the `tileMatrixSets.*`
block defining the tiling scheme. `tiles.layers.*` is **not** read — a run's
content comes from the control file.

The **control file** defines the run:

```properties
tileMatrixSet = ETRS-TM35FIN
ranges = overview, mid, detail

overview.zoom = 0-7
overview.collections = maastoalue, tieviiva
overview.maastoalue.filter = kohdeluokka IN (36200, 36313)
overview.tieviiva.filter = kohdeluokka <= 12132

mid.zoom = 8-11
mid.collections = maastoalue, tieviiva, rakennus
mid.tieviiva.filter = kohdeluokka <= 12141

detail.zoom = 12-14
detail.collections = maastoalue, tieviiva, rakennus, korkeuskayra
```

| key | meaning |
| --- | --- |
| `tileMatrixSet` | tiling scheme to render into, by id; its definition is read from the service file's `tileMatrixSets.*` block |
| `ranges` | the zoom ranges, by name |
| `<range>.zoom` | inclusive zoom range, `n` or `n-m` |
| `<range>.collections` | collection ids rendered at these zooms, one MVT layer each |
| `<range>.<collection>.filter` | CQL2 text, parsed with the API's own parser |
| `sources` | output tables, by source id, comma-separated; default: one table per rendered collection |
| `<source>.collections` | collection ids in this source, comma-separated; each rendered collection belongs to exactly one source |

Ranges must cover the rendered zooms exactly: a gap or an overlap is a
configuration error. The whole file — collection ids and filters included — is
validated before any rendering starts, so a typo cannot fail a long run an hour
in.

**Sources** group collections into output tables. Without `sources`, each
rendered collection is its own table, named by its id. With `sources`, each
source id is one table, named by that id, and the layers of its collections
share each tile. The control file is rejected if a source is listed twice, has
no collections, lists a collection that another source also lists, names a
collection that no zoom range renders, or leaves a rendered collection in no
source. Source ids are table names as given, so the [Output](#output) rules
apply to them.

**Why a separate file.** Rendering the same source data across a dozen zoom
levels is not what anyone wants: at small scales you select different
collections, filter by attribute, and read a pre-generalised geometry. The server
has no notion of this and deliberately does not gain one — an offline pyramid is
where "what is drawn at what scale" is decided. Pre-generalised geometry needs no
syntax of its own: configure it as its own collection in the service file, and
name that collection for the coarse zooms.

## How the pyramid is built

**Every zoom level is rendered from features.** Deriving a coarse tile by scaling
its four children's MVT geometry would be cheaper but is wrong twice over:
halving the coordinates keeps the finer level's point density instead of
generalising at the coarser resolution, and the children are clipped at seams
that are interior to the parent, so a feature crossing one arrives as two
fragments whose seam stays visible.

An empty tile writes no row — the GeoPackage tiles model has no empty-tile
marker, and a reader signals "no tile" by a missing row. An empty coarse tile
does **not** prune its subtree: a coarse zoom may render a filtered subset of a
different collection than its children, so "empty here" says nothing about
"empty below".

Rendering runs on a worker pool; writing is single-threaded, because SQLite
takes one writer at a time. Tiles are written in whichever order they finish
rendering — waiting for the next address in traversal order instead left a slow
tile holding every finished tile behind it, and the workers idle.

## Output

A GeoPackage may hold several pyramids, so the rules are per *table*:

| output file | table | behaviour |
| --- | --- | --- |
| absent | — | create file and table |
| present | absent | create the table in it |
| present | present | error: refuses to overwrite |

A run that is interrupted leaves whatever it had committed. That is a partial
pyramid with holes in it, not a prefix of the traversal, so it cannot be
continued — rerun into a fresh output.
