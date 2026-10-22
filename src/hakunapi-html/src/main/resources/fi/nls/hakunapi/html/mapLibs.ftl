<#-- Leaflet and proj4 imports shared by featureCollection.ftl and feature.ftl. Needs bgProj4 from backgroundMap.ftl. -->
  <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" integrity="sha512-Zcn6bjR/8RZbLEpLIeOwNtzREBAJnUKESxces60Mpoj+2okopSAcSUIUOseddDm0cxnGQzxIR7vJgsLZbdLE3w==" crossorigin=""/>
  <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js" integrity="sha512-BwHfrr4c9kmRkLw6iXFdzcdWV/PGkVgiIyIWLLlTSXzWQzxuSg4DiQUCpauz/EWjgk5TYQqX/kvn9pG1NpYfqg==" crossorigin=""></script>
  <#if bgProj4?has_content>
  <script src="https://cdn.jsdelivr.net/npm/proj4@2.22.0/dist/proj4.js" integrity="sha512-cswjNP3FhtQaZ0fd7gsRpoBMFRyOBDvbK40A/GsF9QOlE75rERLj5WMf6mA5IdacAMXfTS258pSgJYj2C1hAXA==" crossorigin=""></script>
  <script src="https://cdn.jsdelivr.net/npm/proj4leaflet@1.0.2/src/proj4leaflet.js" integrity="sha512-NA+0vegFw6AiYXT23H+hk43LxqwpJkkviK/vHTESeu+jUpvWssfQRWWqSR07aUhGSKtfpkdlE8YFrVaHsnb3jw==" crossorigin=""></script>
  </#if>
