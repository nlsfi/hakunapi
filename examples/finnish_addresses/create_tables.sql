-- DROP statements commented out to avoid dropping tables user didn't expect to be dropped
--DROP TABLE IF EXISTS suomi_osoitteet;
CREATE TABLE suomi_osoitteet (
  rakennustunnus varchar(10),
  sijaintikunta char(3),
  maakunta char(2),
  kayttotarkoitus char(1),
  pohjoiskoordinaatti integer,
  itakoordinaatti integer,
  osoitenumero smallint,
  kadunnimi_suomi varchar(100),
  kadunnimi_ruotsi varchar(100),
  katunumero varchar(13),
  postinumero char(5),
  aanestysalue char(4),
  aanestysalue_nimi_suomi varchar(50),
  aanestysalue_nimi_ruotsi varchar(50),
  sijaintikiinteisto varchar(20),
  tietojen_poimintapaiva varchar(20)
);

--DROP TABLE IF EXISTS suomi_kunnat;
CREATE TABLE suomi_kunnat (
  sijaintikunta char(3) PRIMARY KEY,
  nimi_suomi varchar(50)
);

--DROP TABLE IF EXISTS simple_addresses;
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
