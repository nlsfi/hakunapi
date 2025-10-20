<img src="hakunapi_logo_v1.png" alt="hakunapi logo" width="350"/>

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

hakunapi-simple-servlet module builds into a "off-the-shelf" OGC API Features server that you drop into your servlet
container and configure with an outside configuration file.

hakunapi can also be used as a "framework" for building your own customized implementation (for example if you want to
support Complex Features). The framework is modular and the modules should fit together well, you are free to
mix-and-match.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md)

## Example

Check out [Finnish addresses](examples/finnish_addresses)

## Quick Start

### Docker

To quickly get started with hakunapi, you can use the provided docker compose file found under `docker`.

This is broken down into two parts

#### Database (db)

This is a postgis database (as of now using postgres 18) with example data and tables.

- Username: address_reader
- Password: test

#### Hakunapi server (hakunapi)

This is the hakunapi server running in a tomcat container (tomcat 10).

- URL: http://localhost:8080/features

By default, this will be exposed on port 8080.
You can also remotely connect to it via JVM debug using port 41467.

#### Building and running

To build and run the docker containers, navigate to the `docker` folder and run

```shell
cd docker
docker compose up -d
```

If you need to rebuild the images (for example if you have made changes to the code), run

```shell
cd docker
docker compose up -d --build
```

You can also modify the docker compose to mount the features.war from your local build if you want to test changes without rebuilding the image.

```yaml
    volumes:
      # mount the working example config dir so hakunapi can read addresses.properties (read-only)
      - ./cfg:/app/config:ro 
      - ../webapp-jakarta/hakunapi-simple-webapp-jakarta/target/features.war:/usr/local/tomcat/webapps/features.war:ro
```

### Docker - Hotreload development

There is another docker compose file that will set up the image for hot reaload using [hotswap agent](https://hotswapagent.org/index.html).

#### Dockerfile-hotreload

A multi-stage Dockerfile is provided to build hakunapi and run it on Tomcat.

Because of this some potential oddities

- This is pulling a new jdk and not using the provided 21 image.
    - This is because the default jdk does not support dvcem needed for hot reload of classes.
- It is downloading hotswap agent from github releases.
    - This will allow hot reload of classes when debugging remotely. (along with the updated JDK)

#### Building and running

In order to get the full benefit of hot reloading, you need to do a `mvn clean install -DskipTests` to build the
classes.

Then you can build and run the docker container with

```shell
cd docker
docker compose -f docker-compose-hot-reload.yml up -d --build
```

Once everything is up and running, you can access the server at http://localhost:8080/features.

To hot reload
 - You must connect to the container from your IDE, this will trigger the changes to be reflected in the running container.
   - IntelliJ IDEA: Run -> Edit Configurations -> Remote JVM Debug
     - Make sure to set port to "41467", don't use the default "5005" (or edit the docker compose file to match)
   - Eclipse: Run -> Debug Configurations -> Remote Java Application (untested)
 - After connecting, you can make changes to the code and recompile (usually Ctrl+F9 or Cmd+F9) and the changes should be reflected in the running container.