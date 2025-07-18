## Introduction

### Main features

`Hakunapi` is a server implementation of the [OGC API Features](https://ogcapi.ogc.org/features/) standard supporting:
* OGC API - Features - Part 1: Core
* OGC API - Features - Part 2: Coordinate Reference Systems by Reference
* OGC API - Features - Part 3: Filtering and Common Query Language (CQL2)

This document describes key capabilities of Hakunapi and Java modules implementing them.

Hakunapi supports following data stores and output formats:
- Data stores
  - PostgreSQL / [PostGIS](https://postgis.net/)
  - [GeoPackage (GPKG)](https://www.geopackage.org/)
- Output writers for geospatial features
  - [GeoJSON](https://geojson.org)​
  - [HTML 5](https://html.spec.whatwg.org/)​
  - [GeoPackage (GPKG)](https://www.geopackage.org/)
  - CSV / [Well-known text representation of geometry (WKT)](https://en.wikipedia.org/wiki/Well-known_text_representation_of_geometry)
  - Other formats, not fully supported​
    - [OGC Features and Geometries JSON (JSON-FG)](https://github.com/opengeospatial/ogc-feat-geo-json)
    - GML (3.1.1/WFS 1.1.0)​
    - ElasticSearch Bulk API JSON​
    - Jackson Smile (GeoJSON)

### Environment

Services implemented with Hakunapi can be deployed both as [Jakarta EE](https://jakarta.ee/) and [Java EE](https://www.oracle.com/java/technologies/java-ee-glance.html) based servlet web applications. There are `hakunapi-simple-servlet` modules for both cases allowing deploying "off-the-shelf" servlets by just adding geospatial data and feature type specific configuration. Alternatively you can also extend Hakunapi base servlet implementations with custom Java code.

Miniminum requirements for the deployment servlet environment:
* webapps based on modules under `webapp-jakarta`: Java 17+ with a servlet container like [Apache Tomcat](https://tomcat.apache.org/) version 10+ or [Eclipse Jetty](https://jetty.org/) 12+.
* webapps based on modules under `webapp-javax`: Java 11+ with a servlet container like [Apache Tomcat](https://tomcat.apache.org/) version 9. 

It's also possible to embed such Hakunapi services on a containerized environment, like Docker, however the support for this is not yet documented.

### Roadmap

See also [issues](https://github.com/nlsfi/hakunapi/issues/139) and the latest [Hakunapi roadmap](https://github.com/nlsfi/hakunapi/issues/139) (2025).

### Examples

[Finnish addresses](../../examples/finnish_addresses)
* based on the `hakunapi-simple-webapp` servlet, with feature service defined by configuration
* uses Finnish addresses of the buildings as geospatial data
* both PostGIS and GeoPackage data sources demonstrated

## Core features

### Hakunapi-core

The `hakunapi-core` module provides the essential building blocks for implementing [OGC API Features](https://ogcapi.ogc.org/features/) compliant services in Java. It handles HTTP request routing, feature collection management, and serialization to supported output formats such as GeoJSON, GeoPackage and GML.

The module abstracts data access, allowing integration with various spatial databases like PostGIS, Oracle, and GeoPackage. It supports filtering and querying features using CQL2, and enables coordinate reference system transformations. The core also manages API metadata, conformance classes, and error handling.

Telemetry and logging are integrated for monitoring and diagnostics. Comprehensive test utilities are included to facilitate robust API development.

### OGC API Features support

[OGC API Features](https://ogcapi.ogc.org/features/) developed by the [Open Geospatial Consortium (OGC)](https://www.ogc.org/) is an open standard for serving, querying, and accessing vector geospatial data (features) over the web using RESTful APIs and standard web technologies.  

Most important standard parts (approved as stable by OGC and these are also supported by Hakunapi):
- **Part 1 (Core):** Specifies the base HTTP API for listing feature collections, retrieving features, and accessing individual feature items; defines standard resource paths, basic queries (such as bounding box and property filtering), and JSON and GeoJSON output formats.
- **Part 2 (Coordinate Reference Systems by Reference):** Adds mechanisms to declare and request features in different coordinate reference systems (CRS) by referencing EPSG codes or similar identifiers in requests and responses.
- **Part 3 (Filtering & Queries):** Defines advanced filtering capabilities, including CQL2 query support, enabling expressive property, spatial, and temporal filters in API requests for more granular data access.

The support in Hakunapi for these standard parts is implemented mostly in the `hakunapi-core` module. Output formats are implemented in separate modules, and introduced later on this document. Part 3 also uses the  CQL2 query lanuage that is described in the next section.

Hakunapi has also initial partial support for the following draft standard part (enhancing support is identified in [Hakunapi roadmap 2025](https://github.com/nlsfi/hakunapi/issues/139)):

- **Part 5 (Schemas):** Standardizes how data schemas are described and made available via the API, ensuring clients can discover and understand the structure and attributes of feature data, which facilitates interoperability and dynamic client integration.

### CQL2 support

OGC [Common Query Language 2 (CQL2)](https://www.ogc.org/standards/cql2/) is an open standard for expressing queries against geospatial data and services. It offers a modern, flexible syntax that supports filtering, sorting, and complex spatial or temporal operations. Human-readable text format and a concise JSON encoding are defined syntax choices.

The `hakunapi-cql2` module implements support for the CQL2, enabling advanced filtering and querying of spatial and non-spatial data. It provides a parser and evaluator for CQL2 expressions, allowing clients to perform complex queries using standard syntax. The module integrates seamlessly with the core, ensuring that filters are efficiently applied to underlying data sources.

The `hakunapi-cql2-functions` module extends CQL2 capabilities by providing a comprehensive set of built-in and custom functions. These functions support spatial, temporal, and attribute-based operations, enhancing the expressiveness of queries. The module is designed for extensibility, allowing developers to register additional functions as needed for specific use cases.
  
## Data sources

### PostGIS

[PostGIS](https://postgis.net/) is an open-source spatial database extension for PostgreSQL that enables advanced geospatial data storage, querying, and analysis directly in the database. It supports rich geometry types, spatial indexing, high-performance spatial operations, standards-compliant functions, and seamless integration with GIS tools for scalable, efficient management of geographic information.

The `hakunapi-source-postgis` module adds [PostGIS](https://postgis.net/) support to Hakunapi. You can generate feature types by mapping tables/views using Hakunapi configuration file or by application specific customized Java code. Attribute mapping, geometry type selection, and exposing selected columns are supported. Performance is optimized via spatial indexes.

Main features:
- Semi-automatic feature type generation from tables/views
- Manual schema definition for full control
- Geometry column and type mapping
- Attribute filtering and mapping
- Custom SQL for advanced feature types
- Schema overrides and customizations
- Performance optimizations using spatial indexes

Please note that semi-automatic feature type generation for attributes is enabled when configured with `"properties": "*"` or left empty. You must still provide basic configuration for ID, geometry, and the table/view. Full automation (no config file at all) is not currently available, but most of the schema can be inferred.

### GeoPackage

[GeoPackage (GPKG)](https://www.geopackage.org/) is an open, standards-based, SQLite-backed file format for storing and transferring geospatial data—including vector features, tile imagery, and attributes—in a single portable container.

The `hakunapi-source-gpgk` module adds support for [GeoPackage (GPKG)](https://www.geopackage.org/) as a data source in Hakunapi. It allows serving geospatial features from GPKG files, with attribute and geometry mapping similar to other Hakunapi source modules. You can configure which tables/layers to expose, control attributes, and optimize queries for spatial access.

Configuration requires specifying the GPKG file and layers to expose. Semi-automatic attribute mapping is supported, but some minimal setup is needed for IDs and geometry columns.

Key differences between PostGIS and GeoPackage support in Hakunapi:

| Capability                    | PostGIS                                                | GeoPackage                                        |
|-------------------------------|-------------------------------------------------------|---------------------------------------------------|
| Backend Type                  | Live PostgreSQL/PostGIS database (JDBC)               | Local file-based GeoPackage (SQLite)              |
| Configuration                 | DB connection, schema, credentials                    | File path, layer/table names                      |
| Schema Discovery              | Semi-automatic, needs ID/geometry config              | Reads layers/columns from file, minimal mapping   |
| Spatial Index Handling        | Uses DB-level spatial indexes                         | Uses file-based spatial indexes if present        |
| SQL Customization             | Supports custom SQL, joins, calculations              | Limited to SQLite queries, less join support      |
| Multi-table/Join Support      | Advanced multi-table joins supported                  | Generally single-layer/table per feature type     |
| Transaction/Update Semantics  | Supports transactions and live data updates           | Read-oriented, file updates require replacement   |
| SRID/Geometry Handling        | Flexible SRID, multiple geometry types per schema     | Layer-based geometry, SRID fixed per layer        |
| Performance & Scalability     | High (large datasets, concurrency)                    | Limited by file access, best for small datasets   |
| Deployment & Integration      | Server/multi-user, scalable                          | Portable/offline, single-user scenarios           |

### Oracle

The `hakunapi-source-oracle` module adds support for [Oracle Spatial databases](https://www.oracle.com/database/spatial-database/) in Hakunapi. It enables serving geospatial API features from Oracle tables and views, with functionality similar to the PostGIS module. You can map attributes, select geometry columns, and define feature types via configuration or custom Java code.

## Output data formats (stable)

### GeoJSON

[GeoJSON](https://geojson.org)​ is a widely used open standard format for encoding geographic data structures, including geospatial features and geometries, in JSON. By default it supports only WGS 84 geographic coordinates (longitude, latitude), and optionally a third coordinate representing an elevation (when given it must be related to the WGS 84 reference ellipsoid).

[OGC API Features](https://ogcapi.ogc.org/features/) Part 2 specifies the role of GeoJSON as the default output format and how WGS 84 data is serialized. The part 2 specifies mechanisms how queries, responses and metadata may contain coordinates and geometries in other coordinate systems too. 

The `hakunapi-geojson` module implements output writer for this format (the media type `application/geo+json`) as specified by GeoJSON and OGC API Features standards. All GeoJSON compliant data structures including features, feature collections, properties and geometries (Point, LineString, Polygon, MultiPoint, MultiLineString, MultiPolygon and GeometryCollection) are supported. Coordinate support for alternative (other than WGS84) coordinate systems is provided too.

The output writer produces also OGC API Features specific metadata fields (like next and alternative encoding links, number of features, etc.) under GeoJSON Feature collection element in a GeoJSON file.

Classes `OutputFormatGeoJSONTextSeq` and `OutputFormatNDGeoJSON` implemented by the module add support also to [GeoJSON Text Sequences](https://datatracker.ietf.org/doc/html/rfc8142) and [Newline-delimited GeoJSON](https://stevage.github.io/ndgeojson/) variants.

### HTML

As specified by the [OGC API Features](https://ogcapi.ogc.org/features/) Part 1, it's recommened for feature services to provide also supoort for [HTML 5](https://html.spec.whatwg.org/) encoding (the media type `text/html`) as an output format.

This support is implemented by the `hakunapi-html` module and it can be enabled in a Hakunapi feature service using a configuration line like `formats=geojson,html`. An user receives HTML content when the `f=html` URL parameter or the `Accept` header with `text/html` is available in a request.

The HTML content produced is Hakunapi specific according to structure, layout and styling. Anyway for feature collection responses, it should produce same feature items as GeoJSON request would return, but formatted as HTML. If application specific HTML content styling is required this module should be extended or rewritten based on application needs. 

### GeoPackage

The [GeoPackage (GPKG)](https://www.geopackage.org/) output format (the media type `application/geopackage+vnd.sqlite3`) is supported by the `hakunapi-gpgk` module. When needed it must be enabled using a configuration like `formats=geojson,gpgk`. The implementation supports outputting same feature, feature collection, property and geometry objects (Point, LineString, Polygon, MultiPoint, MultiLineString, MultiPolygon and GeometryCollection) as GeoJSON writer too. 

Currently [OGC API Features](https://ogcapi.ogc.org/features/) does not specify anything related to GPGK. Some feature service metadata (like next links) cannot be written in content payload like in the case of the default GeoJSON output. Such metadata should be provided as HTTP headers (an optional capability specified by OGC API Features), but support in Hakunapi is still an [upcoming feature](https://github.com/nlsfi/hakunapi/issues/91).

### CSV / WKT

The `hakunapi-csv` module implements an output format writer that generates a CSV text document (the media type `text/csv; charset=UTF-8"`). Geometries are written as specified by [Well-known text representation of geometry (WKT)](https://en.wikipedia.org/wiki/Well-known_text_representation_of_geometry).

Use a configuration like `formats=geojson,csv` to enable the format in your feature service.

## Output data formats (not fully supported)

### JSON-FG

[OGC Features and Geometries JSON (JSON-FG)](https://github.com/opengeospatial/ogc-feat-geo-json) is a draft specification by OGC that aims to enhance GeoJSON with richer semantics, better support for coordinate reference systems (CRS), multi-feature and multi-geometry handling, additional geometry types, and the ability to encode additional metadata and properties directly within the JSON structure.

In Hakunapi the `hakunapi-jsonfg` module extends the basic GeoJSON support (as provided by `hakunapi-geojson`) with initial and partial support for JSON-FG.

However the implementation is not final yet as the draft standard and there has been changes on it also in 2025 (see JSON-FG issue [#136 Path to 1.0](https://github.com/opengeospatial/ogc-feat-geo-json/issues/136)).


### GML (3.1.1/WFS 1.1.0)​

A partial and unsupported capability for the Geography Markup Language (GML), version 3.1.1, is provided too (the media type `text/xml; subtype=3.1.1`). Only Point, LineString and Polygon geometries are supported. See the module `hakunapi-gml` for more information and use the `gml` id in the format configuration to enable if needed in your feature service.

### ElasticSearch Bulk API JSON

[ElasticSearch Bulk API](https://www.elastic.co/docs/api/doc/elasticsearch/operation/operation-bulk) expects newline-delimited JSON (NDJSON). Hakunapi has support for this variant, and it uses the media type `application/x-ndjson` when writing such content. See the module `hakunapi-esbulk` for details. The id `esbulk` can be used to enable this format in a feature service.

Common geometry types (Point, LineString, Polygon, MultiPoint, MultiLineString and MultiPolygon) are supported, but GeometryCollection is not.

### Jackson Smile (GeoJSON)

The `hakunapi-smile` module provides an unofficial support for the Jackson Smile (GeoJSON) output format (the media type `application/x-jackson-smile`). Use a configuration like `formats=geojson,smile` to enable this format.

It's based on the [Jackson Smile](https://en.wikipedia.org/wiki/Smile_(data_interchange_format)) data interchange format, that according to the source *can also be considered a binary serialization of the generic JSON data model, which means tools that operate on JSON may be used with Smile as well, as long as a proper encoder/decoder exists for the tool*.

The Hakunapi implementation writes GeoJSON objects (feature collections, features, geometries, properties) in the Jackson Smile encoding as described above.

## Additional features

### Coordinate transformations

The `hakunapi-core` module defines the `ProjectionTransformer` interface as a coordinate transformation abstraction.

The two modules implementing this interface are shortly described below.

`hakunapi-proj-gt`
- the implementation is based on [GeoTools - The Open Source Java GIS Toolkit](https://geotools.org/), at least following classes are utilized:
  - [org.geotools.referencing.CRS](https://docs.geotools.org/stable/javadocs/org/geotools/referencing/CRS.html)
  - [org.geotools.api.referencing.crs.CoordinateReferenceSystem](https://docs.geotools.org/latest/javadocs/org/geotools/api/referencing/crs/CoordinateReferenceSystem.html)
  - [org.geotools.api.referencing.operation.MathTransform](https://docs.geotools.org/latest/javadocs/org/geotools/api/referencing/operation/MathTransform.html)
- see GeoTools documentation about [CRS](https://docs.geotools.org/latest/userguide/library/referencing/crs.html) for reference
- GeoTools also uses [The EPSG dataset](https://epsg.org/), see related GeoTools document about [EPSG database usage](https://docs.geotools.org/stable/userguide/library/referencing/epsg.html) in coordinate transformations  
- Hakunapi depends on the `gt-epsg-hsql` module of GeoTools 
- in Hakunapi when using this module, coordinate transformations are fully based on GeoTools, but there are some logic coded to optimize how GeoTools transformations are called (for example related to CRS84 (WGS84) and ETRS89 geographic coordinates)

`hakunapi-proj-jhe`
- custom coordinate tranformations with all code bundled on this module without external dependencies
- supports EUREF-FIN based coordinate refence system commonly used in Finland, and transformations to/from the WGS84 reference too
  - EPSG:4258 (EUREF-FIN)
  - EPSG:3067 (ETRS-TM35FIN)
  - EPSG:3046-3048 (ETRS-TMnn)
  - EPSG:3873-3885 (ETRS-GKnn)
  - EPSG:4326 (WGS84 geographic coordinates)
  - EPSG:3857 (WGS84 Web Mercator)

### Telemetry

See modules `hakunapi-telemetry` and `hakunapi-telemetry-opentelemetry`.

## Webapps and servlets

### Java versions

Hakunapi code modules has the minimum requirement of Java 11, and Hakunapi officially supports Java 11, Java 17 and 21 versions. Other Java versions are not currently tested.

The support for Java 8 was dropped in 2023, see issue [#17](https://github.com/nlsfi/hakunapi/issues/17).

### About servlet frameworks

Hakunapi provides servlet webapp support both for [Jakarta EE](https://jakarta.ee/) (`jakarta.*`) and [Java EE](https://www.oracle.com/java/technologies/java-ee-glance.html) (`javax.*`) based webapps and servlet containers. See Hakunapi issue [#16](https://github.com/nlsfi/hakunapi/issues/16) for background.

In summary Jakarta EE is the future of enterprise Java; it is actively developed, widely supported, and future-proof for new and modernized applications. Java EE is now legacy, but still widely used in many systems.

Main differences on these frameworks, as analyzed without relation to Hakunapi:

| Aspect           | Jakarta EE                           |              Javax EE        |
|------------------|--------------------------------------|-------------------------------------|
| **Namespace**    | Uses `jakarta.*` package prefix      | Uses `javax.*` package prefix       |
| **Standardization & Ownership** | Managed by Eclipse Foundation, open specification process | Formerly managed by Oracle, closed process; now legacy |
| **API Evolution**| Actively developed: new features, bugfixes, and specs | No new features; only maintenance for legacy systems |
| **Servlet Containers & Frameworks** | Modern containers (Tomcat 10+, Jetty 11+, Payara 6+, WildFly 27+) require Jakarta APIs | Older containers (Tomcat 9, Jetty 9/10, GlassFish 5, WildFly <26) use Javax APIs |
| **Compatibility & Migration** | Requires source code changes (jakarta.* imports) when upgrading from Javax | Legacy code remains compatible but cannot use new Jakarta features |
| **Maintainability** | Preferred for new projects; easier to get support and updates | Increasing technical debt; harder to maintain as ecosystem evolves |
| **Future-proofing** | Roadmap includes evolution of Jakarta EE, MicroProfile, new cloud-native features | No roadmap for Javax EE; only bugfixes or security patches for legacy |
| **Community & Ecosystem** | Vibrant, growing community and tooling | Shrinking community; focus shifting to Jakarta EE |
| **Vendor Support** | Most vendors moving to Jakarta APIs; long-term support planned | Support waning, likely to end as Javax becomes obsolete |
| **Best Practice** | Use Jakarta EE for all new and actively maintained apps | Use Javax EE only for maintaining legacy apps that cannot migrate |

### Hakunapi webapp and servlet modules

There are multiple modules in Hakunapi for both [Jakarta EE](https://jakarta.ee/) and [Java EE](https://www.oracle.com/java/technologies/java-ee-glance.html) frameworks.

These modules can be categorized as:
* `hakunapi-simple-servlet`: core servlet classes, used for implementing customized web applications with logic written in Java code
* `hakunapi-simple-webapp`: ready-to-deploy web application, add only configuration
* `hakunapi-simple-webapp-test`: testing for the simple webapp
* `hakunapi-oracle-webapp`: tailored web application for Oracle Spatial databases
* `hakunapi-telemetry-webapp`: telemetry and monitoring capabilities

See details in following sections.

### Jakarta EE webapps

As described in the official [Javax to Jakarta](https://jakarta.ee/blogs/javax-jakartaee-namespace-ecosystem-progress/) namespace migration design document, Jakarta EE 8 was still fully compatible with the Java EE 8 specification. Jakarta EE 9 introduced the new namespace (*.jakarta) and that was completed in the Jakarta EE 10 specification.  

Suggested minimum requirements for [Jakarta EE](https://jakarta.ee/) based Hakunapi web applications are Java 17 and Jakarta EE 10. Use Jakarta EE 10+ compliant servlet containers like [Apache Tomcat](https://tomcat.apache.org/) version 10+ (see also a more detailed summary of [Tomcat versions](https://tomcat.apache.org/whichversion.html)) or [Eclipse Jetty](https://jetty.org/) 12+.

Hakunapi modules (under `webapp-jakarta`) supporting Jakarta EE are described below.

`hakunapi-simple-servlet-jakarta`:
Provides the basic servlet implementation and reusable servlet classes that power the simple webapp, abstracting common HTTP request handling, routing, and response generation for OGC API Features. Developers use this module to extend, customize, or embed Hakunapi’s geospatial API logic into their own Jakarta servlet containers.

`hakunapi-simple-webapp-jakarta`:
This module provides a lightweight, ready-to-deploy Jakarta Servlet-based web application for serving OGC API Features using Hakunapi's core capabilities. It is designed for rapid prototyping, demonstration, or small-scale production services where simplicity and minimal configuration are desired. Use this as a starting point for custom geospatial API deployments with little overhead.

`hakunapi-simple-webapp-test-jakarta`:
A companion module focused on integration and functional testing for the simple webapp, including sample datasets, mock configurations, and test utilities.

`hakunapi-telemetry-webapp-jakarta`:
Adds telemetry and monitoring capabilities to Hakunapi Jakarta webapps, enabling collection of usage statistics, performance metrics, and service health data. This module is valuable for operational monitoring, reporting, and debugging in both development and production environments. It supports integration with external monitoring systems for enhanced visibility.

`hakunapi-oracle-webapp-jakarta`:
Supplies an application variant of the Hakunapi Jakarta webapp tailored for Oracle Database backends, implementing optimized feature type mapping, connection management, and SQL handling compatible with Oracle spatial extensions. Use this module when building OGC API services over Oracle databases, leveraging full Hakunapi and Jakarta Servlet integration.

In summary these modules are intended to be assembled for building, testing, and operating Jakarta Servlet-based OGC API services with Hakunapi. The `simple-servlet` module supplies core servlet classes; `simple-webapp` wraps them into a complete, runnable webapp; `simple-webapp-test` enables thorough testing of that webapp; `telemetry-webapp` adds monitoring features; and `oracle-webapp` provides specialized support for Oracle Database deployments. Together, they streamline geospatial API development from prototype through production, with extensibility for different backend databases and operational needs.

### Java EE webapps

Minimum requirements for [Java EE](https://www.oracle.com/java/technologies/java-ee-glance.html) based Hakunapi web applications are Java 11 and Java EE 8. Use Java EE compliant servlet containers like [Apache Tomcat](https://tomcat.apache.org/) version 9.

Hakunapi modules (under `webapp-javax`) supporting Java EE are listed below:
* `hakunapi-simple-servlet-javax`
* `hakunapi-simple-webapp-javax`
* `hakunapi-simple-webapp-test-javax`
* `hakunapi-telemetry-webapp-javax`
* `hakunapi-oracle-webapp-javax`

Purposes and features of each module are similar to those introduced in the previous section related to Jakarta EE.

## Testing

### Configuration tests

See the module `hakunapi-config-test`.

