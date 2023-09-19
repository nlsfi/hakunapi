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
  <title>${(featureType.title)!(featureType.name)} - Items</title>
</head>
<body>
<main>
  <div class="container-lg py-4">
    <nav aria-label="breadcrumb">
      <ol class="breadcrumb">
        <li class="breadcrumb-item"><a class="d-flex align-items-center text-dark text-decoration-none" href="../../">Home</a></li>
        <li class="breadcrumb-item"><a class="d-flex align-items-center text-dark text-decoration-none" href="../../collections">Collections</a></li>
        <li class="breadcrumb-item"><a class="d-flex align-items-center text-dark text-decoration-none" href="../../collections/${featureType.name}">${(featureType.title)!(featureType.name)}</a></li>
        <li class="breadcrumb-item active" aria-current="page">Items</li>
      </ol>
    </nav>

    <header class="pb-2 mb-2">
      <h1>${featureType.title!featureType.name}</h1>
      <#if featureType.description??><p>${featureType.description}</p></#if>
      <label for="crs">Switch CRS:</label>
      <select name="crs" id="crs" onchange="window.location.search = 'crs=' + this.options[this.selectedIndex].value">
        <#list featureType.geom.srid as availableSRID>
        <#if availableSRID == 84>
            <#if srid == 84>
            <option value="http://www.opengis.net/def/crs/OGC/1.3/CRS84" selected>CRS84</option>
            <#else>
            <option value="http://www.opengis.net/def/crs/OGC/1.3/CRS84">CRS84</option>
            </#if>
        <#else>
            <#if availableSRID == srid>
            <option value="http://www.opengis.net/def/crs/EPSG/0/${availableSRID?c}" selected>EPSG:${availableSRID?c}</option>
            <#else>
            <option value="http://www.opengis.net/def/crs/EPSG/0/${availableSRID?c}">EPSG:${availableSRID?c}</option>
            </#if>
        </#if>
        </#list>
      </select>
    </header>
    
    <div id="map" class="border pb-3 mb-3" style="height: 360px;">
      <div class="leaflet-bottom leaflet-left">
        <button class="leaflet-control" onClick="bboxToMapBounds()">Set bbox to current view</button>
      </div>
    </div>
    
    <h2>Features</h2>
    <#if features?has_content>
    <div class="table-responsive">
      <table class="table table-striped table-sm">
        <#assign firstFeature = features[0]>
        <thead>
          <tr>
            <th>id</th>
            <#list firstFeature.properties?keys as key>
            <th>${key}</th>
            </#list>
          </tr>
        </thead>
        <tbody>
          <#list features as feature>
          <tr>
            <td><a href="./items/${feature.id}?crs=http%3A%2F%2Fwww.opengis.net%2Fdef%2Fcrs%2FEPSG%2F0%2F3067" class="text-decoration-none">${feature.id}</a></td>
            <#list feature.properties?values as value>
            <td>${value!""}</td>
            </#list>
          </tr>
          </#list>
        </tbody>
      </table>
    </div>
    <#else>
    <p>No features found!</p>
    </#if>
    
    <nav aria-label="Pagination">
      <ul class="pagination">
        <#list links as link>
        <#if link.rel == "prev" && link.type == "text/html">
        <li class="page-item"><a class="page-link text-dark text-decoration-none" href="${link.href}">${link.title!"Previous page"}</a></li>
        </#if>
        </#list>
        <#list links as link>
        <#if link.rel == "next" && link.type == "text/html">
        <li class="page-item"><a class="page-link text-dark text-decoration-none" href="${link.href}">${link.title!"Next page"}</a></li>
        </#if>
        </#list>
      </ul>
    </nav>

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

var data = [
<#list features as feature>
<#if feature.geometry??>
{
  "type": "Feature",
  "id": "${feature.id}",
  "geometry": ${feature.geometry}
},
</#if>
</#list>
];

var layer = L.geoJSON(data, {
  coordsToLatLng: function(coords) {
    var point = L.point(coords[0], coords[1]);
    return crs.projection.unproject(point);
  },
  onEachFeature: function (feature, layer) {
    layer.bindPopup('<a href="./items/' + feature.id + '?crs=http%3A%2F%2Fwww.opengis.net%2Fdef%2Fcrs%2FEPSG%2F0%2F3067">' + feature.id + '</a>');
  }
}).addTo(map);

const urlParams = new URLSearchParams(window.location.search);
if (urlParams.has('bbox')) {
  const bbox = urlParams.get('bbox');
  const bboxCrs = urlParams.get('bbox-crs');
  const parts = bbox.split(",").map(parseFloat);
  const topL = crs.projection.unproject(L.point(parts[0], parts[1]));
  const botL = crs.projection.unproject(L.point(parts[2], parts[1]));
  const topR = crs.projection.unproject(L.point(parts[0], parts[1]));
  const botR = crs.projection.unproject(L.point(parts[2], parts[3]));
  const x1 = Math.min(topL.lng, botL.lng);
  const x2 = Math.max(topR.lng, botR.lng);
  const y1 = Math.min(botL.lat, botR.lat);
  const y2 = Math.max(topL.lat, topR.lat);
  map.fitBounds([
    [y1, x1],
    [y2, x2]
  ]);
} else {
  map.fitBounds(layer.getBounds());
}

function bboxToMapBounds() {
  const urlParams = new URLSearchParams(window.location.search);
  const bounds = map.getBounds();
  const topL = crs.projection.project(bounds.getNorthWest());
  const botL = crs.projection.project(bounds.getSouthWest());
  const topR = crs.projection.project(bounds.getNorthEast());
  const botR = crs.projection.project(bounds.getSouthEast());
  const x1 = Math.min(topL.x, botL.x);
  const x2 = Math.max(topR.x, botR.x);
  const y1 = Math.min(botL.y, botR.y);
  const y2 = Math.max(topL.y, topR.y);
  urlParams.set("bbox", x1 + ',' + y1 + ',' + x2 + ',' + y2);
  urlParams.set("bbox-crs", 'http://www.opengis.net/def/crs/EPSG/0/${srid?c}'); 
  window.location.search = urlParams.toString();
}
</script>
</body>
</html>