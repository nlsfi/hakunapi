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
  concat(rakennustunnus, '-', osoitenumero),
  ST_SetSRID(ST_MakePoint(itakoordinaatti, pohjoiskoordinaatti), 3067),
  COALESCE(kadunnimi_suomi, kadunnimi_ruotsi),
  postinumero,
  nimi_suomi,
  katunumero,
  rakennustunnus,
  sijaintikiinteisto
FROM suomi_osoitteet
JOIN suomi_kunnat USING (sijaintikunta)
WHERE kadunnimi_suomi IS NOT NULL OR kadunnimi_ruotsi IS NOT NULL
