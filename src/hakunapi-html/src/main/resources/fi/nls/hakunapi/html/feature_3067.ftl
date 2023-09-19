<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
<#list links as link>
  <link rel="${link.rel}" type="${link.type}" title="${link.title}" href="${link.href}"/>
</#list>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-eOJMYsd53ii+scO/bJGFsiCZc+5NDVN2yr8+0RDqr0Ql0h+rP48ckxlpbzKgwra6" crossorigin="anonymous">
  <link rel="stylesheet" href="https://unpkg.com/leaflet@1.7.1/dist/leaflet.css" integrity="sha512-xodZBNTC5n17Xt2atTPuE1HxjVMSvLVW9ocqUKLsCC5CXdbqCmblAshOMAS6/keqq/sMZMZ19scR4PsZChSR7A==" crossorigin=""/>
  <script src="https://unpkg.com/leaflet@1.7.1/dist/leaflet.js" integrity="sha512-XQoYMqMTK8LvdxXYG3nZ448hOEQiglfqkJs1NOQV44cWnUrBc8PkAOcXy20w0vlaXaVUearIOBhiXZ5V3ynxwA==" crossorigin=""></script>
  <script src="https://cdn.jsdelivr.net/npm/proj4@2.7.5/dist/proj4-src.min.js" crossorigin=""/></script>
  <script src="https://cdn.jsdelivr.net/npm/proj4leaflet@1.0.2/src/proj4leaflet.min.js" crossorigin=""/></script>
  <title>${featureType.name} - ${id}</title>
  <style>
  </style>
</head>
<body>
<main>
  <div class="container py-4">
    <nav aria-label="breadcrumb">
      <ol class="breadcrumb">
        <li class="breadcrumb-item"><a class="d-flex align-items-center text-dark text-decoration-none" href="../../../">Home</a></li>
        <li class="breadcrumb-item"><a class="d-flex align-items-center text-dark text-decoration-none" href="../../../collections">Collections</a></li>
        <li class="breadcrumb-item"><a class="d-flex align-items-center text-dark text-decoration-none" href="../../../collections/${featureType.name}">${(featureType.title)!(featureType.name)}</a></li>
        <li class="breadcrumb-item"><a class="d-flex align-items-center text-dark text-decoration-none" href="../../../collections/${featureType.name}/items?crs=http%3A%2F%2Fwww.opengis.net%2Fdef%2Fcrs%2FEPSG%2F0%2F3067">Items</a></li>
        <li class="breadcrumb-item active" aria-current="page">${id}</li>
      </ol>
    </nav>

    <header class="pb-2 mb-2">
      <h1>${featureType.title!featureType.name} / ${id}</h1>
      <#if featureType.description??><p>${featureType.description}</p></#if>
    </header>
    
    <div id="map" class="border pb-3 mb-3" style="height: 360px;"></div>
    
    <h2>Properties</h2>
    <table class="table">
      <tbody>
        <tr>
          <td>id</td>
          <td>${id}</td>
        </tr>
        <#list properties as key, value>
        <tr>
          <td>${key}</td>
          <td>${value!""}</td>
        </tr>
        </#list>
      </tbody>
    </table>
       
    <footer class="pt-3 mt-4 text-muted border-top">Powered by hakunapi &copy; 2023</footer>
  </div>
</main>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta3/dist/js/bootstrap.bundle.min.js" integrity="sha384-JEW9xMcG8R+pH31jmWH6WWP0WintQrMb4s7ZOdauHnUtxwoG2vI5DkLtS3qm9Ekf" crossorigin="anonymous"></script>
<script>
var crs = new L.Proj.CRS("EPSG:3067",
  "+proj=utm +zone=35 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs",
  {
    resolutions: [
      8192, 4096, 2048, 1024, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1, 0.5, 0.25
    ],
    origin: [-548576.000000, 8388608.000000]
  })

var map = L.map('map', {
    crs: crs
});
L.tileLayer('https://avoin-karttakuva.maanmittauslaitos.fi/avoin/wmts/1.0.0/taustakartta/default/ETRS-TM35FIN/{z}/{y}/{x}.png?api-key=7cd2ddae-9f2e-481c-99d0-404e7bc7a0b2', {
    minZoom: 0,
    maxZoom: 15,
    attribution: '&copy; <a href="https://www.nls.fi">National Land Survey of Finland</a>'
}).addTo(map);


<#if geometry??>
var data = {
  "type": "Feature",
  "id": "${id}",
  "geometry": ${geometry}
};
var layer = L.geoJSON(data, {
  coordsToLatLng: function(coords) {
    var point = L.point(coords[0], coords[1]);
    return crs.projection.unproject(point);
  },
  onEachFeature: function (feature, layer) {
    layer.bindPopup(feature.id);
  }
}).addTo(map);
map.fitBounds(layer.getBounds());
</#if>
</script>
</body>
</html>