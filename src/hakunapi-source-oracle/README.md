WIP Proof-of-Concept

Sample configuration

```
api.title=KHRTP
api.version=1.0
api.description=POC Oracle SDO
api.contact.name=Technical support for APIs
api.contact.email=sovellustuki@maanmittauslaitos.fi
api.contact.url=https://www.maanmittauslaitos.fi/tietoa-maanmittauslaitoksesta/yhteystiedot/verkkopalveluiden-tuki
api.license.name=Data accessed from the API is property of National Land Survey (test data)
api.license.url=https://maanmittauslaitos.fi

servers=features

servers.features.url=http://localhost:8080/features
servers.features.description=KHRTP


formats=json,html
formats.json.type=json
formats.html.type=html


getfeatures.limit.default=10
getfeatures.limit.max=100


db.classes=fi.nls.hakunapi.simple.postgis.PostGISSimpleSource,fi.nls.hakunapi.simple.sdo.SDOSimpleSource
db=khrtp

collections=kiinteistonluovutus
collections.kiinteistonluovutus.type=ora
collections.kiinteistonluovutus.table=KIINTEISTONLUOVUTUS
collections.kiinteistonluovutus.schema=KHRTP
collections.kiinteistonluovutus.db=khrtp
collections.kiinteistonluovutus.srid.storage=3067
collections.kiinteistonluovutus.extent.spatial=15.053785270822842,58.60745650071967,33.993537468175056,70.26415661214813
collections.kiinteistonluovutus.srid=3067
collections.kiinteistonluovutus.writeNulls=false
collections.kiinteistonluovutus.geometryDimension=XY
collections.kiinteistonluovutus.id.mapping=ID
collections.kiinteistonluovutus.geometry.mapping=SIJAINTI
collections.kiinteistonluovutus.properties=luovutustunnus,luovutuspvm,paivityspvm,lakkaamispvm,sijainninsijoitustapa
collections.kiinteistonluovutus.parameters=luovutustunnus
collections.kiinteistonluovutus.properties.luovutustunnus.mapping=LUOVUTUSTUNNUS
collections.kiinteistonluovutus.properties.luovutuspvm.mapping=LUOVUTUSPVM
collections.kiinteistonluovutus.properties.paivityspvm.mapping=PAIVITYSPVM
collections.kiinteistonluovutus.properties.lakkaamispvm.mapping=LAKKAAMISPVM
collections.kiinteistonluovutus.properties.sijainninsijoitustapa.mapping=SIJAINNINSIJOITUSTAPA
#collections.kiinteistonluovutus.proj=fi.nls.hakunapi.proj.nls.NLSProjectionTransformerFactory


db.khrtp.jdbcUrl=<ORACLE-DB-URL>
db.khrtp.driverClassName=oracle.jdbc.OracleDriver
db.khrtp.dataSource.user=<ORACLE-DB-USER>
db.khrtp.dataSource.password=<ORACLE-DB-PASSWORD>
db.khrtp.minimumIdle=1
db.khrtp.maximumPoolSize=1
db.khrtp.readOnly=true
db.khrtp.autoCommit=false
#db.khrtp.dataSource.preparedStatementCacheQueries=0



```