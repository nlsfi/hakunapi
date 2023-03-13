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

First let's setup a database and a user for our (or you can use pre-existing db and user if you want and skip this section).

```
CREATE DATABASE address_fin ENCODING = 'UTF8';
\c address_fin
CREATE EXTENSION postgis;
CREATE USER address_reader PASSWORD 'test';
GRANT ALL ON DATABASE address_fin TO address_reader;
```

### Load the data into the database

Create the tables to represent the raw data and load the data in
```
psql -h localhost -d address_fin -U address_reader -f create_tables.sql

psql -h localhost -d address_fin -U address_reader -c "\copy suomi_osoitteet (
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

psql -h localhost -d address_fin -U address_reader -c "\copy suomi_kunnat (sijaintikunta, nimi_suomi) FROM 'suomi_kunnat.csv' WITH (FORMAT CSV, DELIMITER ';')"
```

### Transform raw data

* Create unique primary key index by combining rakennustunnus with osoitenumero
* Select thoroughfarename from kadunnimi_suomi, kadunnimi_ruotsi
* Copy finnish municipality name over
* Filter out rows with no finnish or swedish thoroughfarenames

```
psql -h localhost -d address_fin -U address_reader -f insert_simple_address.sql
```

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

