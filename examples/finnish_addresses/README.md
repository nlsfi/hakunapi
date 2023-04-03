# Simple addresses of Buildings of Finland

The goal of this example project is to create an OGC API Features service with hakunapi to provide nationwide addresses of the buildings in Finland available freely from https://www.avoindata.fi/data/fi/dataset/rakennusten-osoitetiedot-koko-suomi.

## Processing the data

### Getting the data

Begin by downloading the .7z file containing all required data from https://www.avoindata.fi/data/fi/dataset/rakennusten-osoitetiedot-koko-suomi

Extract the file the 7zip file using your favourite tool and convert the file from latin-1 to utf-8 encoding, for example

```
7z x suomi_osoitteet_2023-02-13.7z
iconv -f ISO-8859-1 -t UTF-8 Suomi_osoitteet_2023-02-13.OPT > suomi_osoitteet.csv
```

### Setting up the database

For the following we will assume you to have a PostgreSQL/PostGIS instance running on localhost port 5432. Any modern (9.6+) version of PostgreSQL/PostGIS should work.

First let's setup a database (or you can use pre-existing db and user if you want and skip this section).

```
CREATE DATABASE address_fin ENCODING = 'UTF8';
\c address_fin
CREATE EXTENSION postgis;
```

### Load the data into the database

Create the tables to represent the raw data and load the data in
```
psql -d address_fin -f create_tables.sql

psql -d address_fin -c "\copy suomi_osoitteet (
rakennustunnus,
sijaintikunta,
maakunta,
kayttotarkoitus,
pohjoiskoordinaatti,
itakoordinaatti,
osoitenumero,
kadunnimi_suomi,
kadunnimi_ruotsi,
katunumero,
postinumero,
aanestysalue,
aanestysalue_nimi_suomi,
aanestysalue_nimi_ruotsi,
sijaintikiinteisto,
tietojen_poimintapaiva
) FROM 'suomi_osoitteet.csv' WITH (FORMAT CSV, DELIMITER ';')"

psql -d address_fin -c "\copy suomi_kunnat (sijaintikunta, nimi_suomi) FROM 'suomi_kunnat.csv' WITH (FORMAT CSV, DELIMITER ';', HEADER)"
```

### Transform raw data

* Create unique primary key index by combining rakennustunnus with osoitenumero
* Select thoroughfarename from kadunnimi_suomi, kadunnimi_ruotsi
* Copy finnish municipality name over
* Filter out rows with no finnish or swedish thoroughfarenames

```
psql -d address_fin -f simple_addresses.sql
```

### Add read-only user for publishing application

psql -d address_fin -c "CREATE USER address_reader PASSWORD 'test'"
psql -d address_fin -c "GRANT SELECT ON ALL TABLES IN SCHEMA public TO address_reader"

## Configuring and running hakunapi

hakunapi by default builds into a war package called `features.war`. For the following we'll assume you have a tomcat instance running on localhost on port 8080.

First build the project with `mvn clean package` and extract the built war from `hakunapi-simple-webapp/target/features.war` and give that to your tomcat instance to load.

Now we need to tell hakunapi where to look for the configuration file. First, create a directory to contain the configuration files. We will create ours in `/app/features_addresses/`.
```
mkdir -p /app/features_addresses
```

and copy the example config files from `cfg` there
```
cp cfg/* /app/features_addresses
```

On startup hakunapi will look for system property `{context_path}.hakuna.config.path` (e.g. `features.hakuna.config.path`) followed by `hakuna.config.path` if the first one didn't work. This property is used to control where hakunapi searches for the main configuration file of the hakunapi instance. In our case we'll need to add `-Dfeatures.hakuna.config.path=/app/features_addresses/addresses.properties` to the tomcat launch options.

Now hakunapi can find the configration file and the database connection configuration file.

Restart your tomcat instance and navigate to http://localhost:8080/features

## Preparing for production

### Configure endpoint servers

In order for hakunapi to work properly it needs to know the URL the user is using to access it. Currently hakunapi only supports setting this manually in the configuration (meaning there's no support for extracting the information from http headers for example), see `addresses.properties` lines 12-17:

```
servers=dev
servers.dev.url=http://localhost:8080/features
servers.dev.description=Development server
```

expected configuration for a production installation accessible via `https://www.company.com/gis-services/features`

```
servers=production
servers.production.url=https://www.company.com/gis-services/features
servers.production.description=Production
```

### Configure DB pool size

By default, one hakunapi installation only handles 10 concurrent requests (that require information from database) at a time (or more specifically, 10 per database). This is the default configuration for a HikariCP connection pool which can be configured in `db.properties` by setting `dataSource.maximumPoolSize=20`.

If you have large servers consider feel free to increase the pool size to core_count * 2-3, please read the excellent documentation on the topic at   https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing

### Tuning hakunapi

In general, hakunapi isn't that resource hungry and adding more resources to hakunapi usually isn't too beneficial if the database (or the connection to the database) can't keep up. This is due to the way hakunapi works: Instead of the usual way of mapping the db result set to a list of model objects and then serializing them hakunapi relies more on "fully streaming" the response from the db result set. This creates very shortly lived objects which the current GC algorithms seem to handle with little trouble. hakunapi is able to generate up to 200MB/s of GeoJSON per thread from a local PostGIS server so the outbound network connection is usually the first true limiting factor.

Heap size of 256M-1GB is usually plenty for an installation. Feel free to play with other GC options, currently there isn't any suggestions which would be preferable.
