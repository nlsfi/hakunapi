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

DROP TABLE IF EXISTS suomi_kunnat;
CREATE TABLE suomi_kunnat (
  municipality_number char(3) PRIMARY KEY,
  name_fin varchar(50)
);

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
