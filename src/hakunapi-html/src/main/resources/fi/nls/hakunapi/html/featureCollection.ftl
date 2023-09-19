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
            <td><a href="./items/${feature.id}" class="text-decoration-none">${feature.id}</a></td>
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
var map = L.map('map');
L.tileLayer('https://avoin-karttakuva.maanmittauslaitos.fi/avoin/wmts/1.0.0/taustakartta/default/WGS84_Pseudo-Mercator/{z}/{y}/{x}.png?api-key=7cd2ddae-9f2e-481c-99d0-404e7bc7a0b2', {
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
  onEachFeature: function (feature, layer) {
    layer.bindPopup('<a href="./items/' + feature.id + '">' + feature.id + '</a>');
  }
}).addTo(map);

const urlParams = new URLSearchParams(window.location.search);
if (urlParams.has('bbox')) {
  var bbox = urlParams.get('bbox');
  var parts = bbox.split(",");
  map.fitBounds([
    [parts[1], parts[0]],
    [parts[3], parts[2]]
  ]);
} else {
  map.fitBounds(layer.getBounds());
}

function bboxToMapBounds() {
  const urlParams = new URLSearchParams(window.location.search);
  urlParams.set("bbox", map.getBounds().toBBoxString());
  window.location.search = urlParams.toString();
}
</script>
</body>
</html>