# Hakunapi OGC API Features Server

A server implementation of OGC API Features 
* OGC API - Features - Part 1: Core
* OGC API - Features - Part 2: Coordinate Reference Systems by Reference
* OGC API - Features - Part 3: Filtering and Common Query Language (CQL2)

A server implementation of OGC API Features
(follow development @ https://ogcapi.ogc.org/features/)

Focus on:
* Simple features backed by a PostGIS database
* High performance (achieved with streaming and by reducing the amount of garbage created)  

hakunapi-simple-servlet module builds into a "off-the-shelf" OGC API Features server that you drop into your servlet container and configure with an outside configuration file.

hakunapi can also be used as a "framework" for building your own customized implementation (for example if you want to support Complex Features). The framework is modular and the modules should fit together well, you are free to mix-and-match.  

