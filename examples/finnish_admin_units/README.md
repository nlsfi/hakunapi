# Division into Administrative Areas (vector)

This example project demonstrates how to create an OGC API Features service with hakunapi to provide Finland's administrative boundaries available from the National Land Survey of Finland (Maanmittauslaitos).

The dataset depicts the municipalities, regions, Regional State Administrative Agencies, Wellbeing Services Counties, and the national border of Finland.

## About the Dataset

The Division into Administrative Areas dataset is produced and maintained by the National Land Survey of Finland. It includes comprehensive administrative boundary data for Finland at multiple hierarchical levels and map scales.

The 2025 dataset contains five administrative hierarchy levels: Municipalities (Kunta), Regions (Maakunta), Regional State Administrative Agencies (Aluehallintovirasto), Wellbeing Services Counties (Hyvinvointialue), and Nations (Valtakunta). Each level is available at five map scales: 1:10,000, 1:100,000, 1:250,000, 1:1,000,000, and 1:4,500,000, resulting in 25 total feature collections.

The dataset is updated annually in January to reflect municipal boundary changes. For complete technical specifications, see the [official product description](https://www.maanmittauslaitos.fi/en/maps-and-spatial-data/datasets-and-interfaces/product-descriptions/division-administrative-areas-vector).

## Getting the Data

### Download from Maanmittauslaitos

The GeoPackage files can be downloaded from the National Land Survey of Finland's open data portal:

1. Visit [Maanmittauslaitos Open Data](https://www.maanmittauslaitos.fi/en/maps-and-spatial-data/datasets-and-interfaces)
2. Navigate to the Division into Administrative Areas (vector) dataset
3. Download the zip files for your desired scales
4. Extract each zip file to obtain the GeoPackage files

The extracted GeoPackage files should follow this naming convention:
- `SuomenHallinnollisetKuntajakopohjaisetAluejaot_2025_10k.gpkg`
- `SuomenHallinnollisetKuntajakopohjaisetAluejaot_2025_100k.gpkg`
- `SuomenHallinnollisetKuntajakopohjaisetAluejaot_2025_250k.gpkg`
- `SuomenHallinnollisetKuntajakopohjaisetAluejaot_2025_1000k.gpkg`
- `SuomenHallinnollisetKuntajakopohjaisetAluejaot_2025_4500k.gpkg`

Each GeoPackage file contains all five administrative levels as separate tables (Kunta, Maakunta, Aluehallintovirasto, Hyvinvointialue, Valtakunta).

### License

The dataset is available under the **National Land Survey open data Attribution CC 4.0 licence**.

When using this data, include the attribution:
```
Contains data from Division into administrative areas (vector), National Land Survey of Finland
```

See the [official license](https://www.maanmittauslaitos.fi/en/opendata-licence-cc40) for details.

## Setting Up the Data

This example uses GeoPackage files directly, so no database setup is required.

1. Place the downloaded GeoPackage files in the `examples/finnish_admin_units/` directory (or update the paths in `administrative_areas.properties`)

2. Each GeoPackage file contains tables for all five administrative levels:
   - Kunta (Municipalities)
   - Maakunta (Regions)
   - Aluehallintovirasto (Regional State Administrative Agencies)
   - Hyvinvointialue (Wellbeing Services Counties)
   - Valtakunta (Nations)

## Configuring and running hakunapi with tomcat

hakunapi by default builds into a war package called `features.war`. For the following we'll assume you have a tomcat instance running on localhost on port 8080.

First build the project with `mvn clean package` and give the built war to your tomcat instance to load.

Now we need to tell hakunapi where to look for the configuration file. First, create a directory to contain the configuration files. We will create ours in `/app/features_admin_areas/`.
```
mkdir -p /app/features_admin_areas
```

and copy the example config file there
```
cp examples/finnish_admin_units/administrative_areas.properties /app/features_admin_areas/
```

If you placed your GeoPackage files somewhere other than the example directory, update the `collections.*.db` paths in `administrative_areas.properties` to point to the correct locations.

On startup hakunapi will look for system property `{context_path}.hakuna.config.path` (e.g. `features.hakuna.config.path`) followed by `hakuna.config.path` if the first one didn't work. This property is used to control where hakunapi searches for the main configuration file of the hakunapi instance. In our case we'll need to add `-Dfeatures.hakuna.config.path=/app/features_admin_areas/administrative_areas.properties` to the tomcat launch options.

Now hakunapi can find the configuration file and the GeoPackage data files.

Restart your tomcat instance and navigate to http://localhost:8080/features

The service provides 25 feature collections (5 administrative levels × 5 scales each). Browse the collections at http://localhost:8080/features/collections

## Preparing for Production

### Configure Contact Information

Edit `administrative_areas.properties` and update the contact information:

```properties
api.contact.name=Your Name or Organization
api.contact.email=contact@example.com
api.contact.url=https://your-organization.com
```

### Configure Server Endpoints

Update the server URL configuration to match your production environment:

```properties
servers=production
servers.production.url=https://your-domain.com/features
servers.production.description=Production server
```

### Output Formats

The service provides three output formats by default:
- **GeoJSON** (`f=json`) - Default format for spatial data
- **HTML** (`f=html`) - Human-readable web interface
- **CSV** (`f=csv`) - Tabular data export with geometries as WKT (Well-Known Text)

### API Limits

Configure request limits in `administrative_areas.properties`:

```properties
getfeatures.limit.default=1000
getfeatures.limit.max=10000
```
