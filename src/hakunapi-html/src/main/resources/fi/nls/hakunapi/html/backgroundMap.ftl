<#-- Background map decision shared by featureCollection.ftl and feature.ftl.
     Geographic CRSs and EPSG:3857 are drawn by Leaflet itself and get the configured tile layer (OpenStreetMap by default).
     Other projected CRSs get a tile layer only when one is configured for them (OpenStreetMap does not serve them). -->
<#assign bgWebMercator = (srid == 3857)>
<#assign bgLeafletCrs = (sridCode?? && sridCode.degrees) || bgWebMercator>
<#assign bgProj4 = "">
<#if !bgLeafletCrs && sridCode?? && sridCode.proj4??><#assign bgProj4 = sridCode.proj4></#if>
<#assign bgProjected = bgProj4?has_content || bgWebMercator>
<#assign bgTileUrl = "">
<#if settings.tileUrl?? && (bgLeafletCrs || settings.tileUrl != "https://tile.openstreetmap.org/{z}/{x}/{y}.png")><#assign bgTileUrl = settings.tileUrl></#if>
