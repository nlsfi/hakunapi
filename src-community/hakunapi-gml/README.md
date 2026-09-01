# hakunapi-gml

Community module providing a partial and unsupported
[Geography Markup Language (GML)](https://www.ogc.org/standard/gml/) version 3.1.1
output format for Hakunapi, in the style of WFS 1.1.0, using the media type
`text/xml; subtype=3.1.1`.

## This is not general GML support

GML is a large and highly extensible standard, and this module implements only one
very specific slice of it: the single GML 3.1.1 encoding that was needed for one
WFS 1.1.0 style use case. It serves that use case, and is used for it - but it is
**not** a general purpose GML writer and it is not a WFS implementation. Do not expect
it to fit a different GML or WFS requirement without extending it.

In particular:

- It writes one fixed output shape - a `wfs:FeatureCollection` containing
  `gml:featureMember` entries - against hardcoded GML 3.1.1 and WFS 1.1.0 namespaces
  and schema locations. There is no support for other GML versions (2.x, 3.2, or the
  GML Simple Features profiles), for other application schemas, or for configuring
  the emitted namespaces, prefixes or element names.
- No GML application schema is published for the output, so the documents cannot be
  validated by consumers and no `DescribeFeatureType` equivalent exists.
- Coordinate reference systems are written as `EPSG:<srid>` in the `srsName`
  attribute. Many WFS 1.1.0 clients instead expect the URN form
  (`urn:ogc:def:crs:EPSG::<srid>`) and axis order handling that this module does not
  implement.
- Only flat simple features are supported; there is no support for the complex
  features, nested properties or xlink references that are a large part of why GML is
  used in practice.

## Limitations

- Only Point, LineString and Polygon geometries are supported. MultiPoint,
  MultiLineString, MultiPolygon and GeometryCollection are recognised but throw
  `IllegalArgumentException("Not yet implemented")` at write time, so a collection
  containing one will fail the request rather than skip the feature.
- **The module is currently not loadable.** Unlike the other output format modules
  it does not ship a
  `META-INF/services/fi.nls.hakunapi.core.OutputFormatFactorySpi` resource, so the
  `gml` format is not discovered by `ServiceLoader` and cannot be enabled through
  the `formats` configuration even when the module is on the classpath. Registering
  the SPI is a prerequisite for making the format usable again; see
  [issue #173](https://github.com/nlsfi/hakunapi/issues/173).

## Usage

Once the SPI registration above is in place, the format would be enabled with a
configuration like:

```
formats=geojson,gml
```

Add it explicitly to your own deployment:

```xml
<dependency>
    <groupId>fi.nls.hakunapi</groupId>
    <artifactId>hakunapi-gml</artifactId>
</dependency>
```

The version is managed by the Hakunapi root POM, so it may be omitted when
building against Hakunapi as a parent.

## Status and support

**This is a legacy module with no active submitters.** Unlike the other community
modules it has no one maintaining it: the names in [SUBMITTERS](SUBMITTERS) are the
original authors from the version history, not people who have taken on
responsibility for the module. Nobody should be expected to answer questions, fix
bugs or review changes to it.

Legacy here means unmaintained and narrow in scope, not necessarily dead. The module
still does the one job it was written for, and it is in use for that single use case,
so it is kept in `src-community/` and kept building. It is built and (automatically) tested
together with the Hakunapi core modules, but it is not developed further and is not
covered by the support the National Land Survey of Finland provides for core modules.

Contributions are welcome. If you are willing to take the module on, add yourself to
[SUBMITTERS](SUBMITTERS) in the same Pull Request; see
[CONTRIBUTING.md](../../CONTRIBUTING.md) for how to contribute.
