DROP TABLE IF EXISTS finnish_open_address;
CREATE TABLE finnish_open_address (
  permanent_building_identifier varchar(10),
  address_index smallint,
  east integer,
  north integer,
  postal_code char(5),
  address varchar(100),
  address_number varchar(13),
  municipality_number char(3),
  property_identifier varchar(14)
);
COPY finnish_open_address (permanent_building_identifier, address_index, east, north, postal_code, address, address_number, municipality_number, property_identifier)
FROM '/usr/local/share/091.csv'
WITH (FORMAT CSV);

DROP TABLE IF EXISTS suomi_kunnat;
CREATE TABLE suomi_kunnat (
  municipality_number char(3) PRIMARY KEY,
  name_fin varchar(50)
);
COPY suomi_kunnat (municipality_number, name_fin)
FROM '/usr/local/share/suomi_kunnat.csv'
WITH (FORMAT CSV, DELIMITER ';', HEADER);

DROP TABLE IF EXISTS simple_addresses;
CREATE TABLE simple_addresses (
  id varchar(12) PRIMARY KEY,
  geom geometry(Point, 3067) NOT NULL,
  thoroughfare_name varchar(100) NOT NULL,
  postal_descriptor char(5) NOT NULL,
  admin_unit_name_4 varchar(50) NOT NULL,
  address_number varchar(13),
  building varchar(10) NOT NULL,
  parcel varchar(14)
);

INSERT INTO simple_addresses (
  id,
  geom,
  thoroughfare_name,
  postal_descriptor,
  admin_unit_name_4,
  address_number,
  building,
  parcel
)
SELECT
  concat(permanent_building_identifier, '-', address_index),
  ST_Point(east, north, 3067),
  address,
  postal_code,
  name_fin,
  address_number,
  permanent_building_identifier,
  property_identifier
FROM finnish_open_address
JOIN suomi_kunnat USING (municipality_number)
-- TODO Remove these after the original dataset has been improved
WHERE east IS NOT NULL
AND north IS NOT NULL
AND postal_code IS NOT NULL;
