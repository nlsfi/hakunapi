<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<title>Swagger UI</title>
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/swagger-ui/3.45.1/swagger-ui.css" integrity="sha512-bnx7V/XrEk9agZpJrkTelwhjx/r53sx2pFAVIRGPt/2TkunsGYiXs0RetrU22ttk74IHNTY2atj77/NsKAXo1w==" crossorigin="anonymous" />
<style>
html
{
box-sizing: border-box;
overflow: -moz-scrollbars-vertical;
overflow-y: scroll;
}
*,
*:before,
*:after
{
box-sizing: inherit;
}
body {
margin:0;
background: #fafafa;
}
img[alt="Swagger UI"] { display: none; }
.topbar { background-color: transparent !important; }
</style>
</head>
<body>
<div id="swagger-ui"></div>
<script src="https://cdnjs.cloudflare.com/ajax/libs/swagger-ui/3.45.1/swagger-ui-bundle.js" integrity="sha512-bEDewjT/ufwN2J2lPn+3dUcuWJOr2CrxSx31L4V72Ovux/GvoLTgCC/rkdmw6H84aTWfOhXe4uMCrp0E4y+OVw==" crossorigin="anonymous"></script>
<script>
window.onload = function() {
window.ui = SwaggerUIBundle({
dom_id: '#swagger-ui',
url: "${basePathTrailingSlash}api.json",
deepLinking: true,
presets: [SwaggerUIBundle.presets.apis],
plugins: [],
layout: "BaseLayout"
})
}
</script>
</body>
</html>