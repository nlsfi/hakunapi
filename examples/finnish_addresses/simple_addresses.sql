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
