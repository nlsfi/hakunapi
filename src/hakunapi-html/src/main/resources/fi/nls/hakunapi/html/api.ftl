<!DOCTYPE html>
<html lang="${lang!'en'}">
<head>
<meta charset="UTF-8">
<title>Swagger UI</title>
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/swagger-ui/5.29.1/swagger-ui.css" integrity="sha512-eZpfl9qlKnbDvlJ2brfdx3nhlP1FMsA23w65motxKdYsUcfMcdO2bcLPr7mXhvyzmDZwuzYCJKrl/sEo1ditVQ==" crossorigin="anonymous" />
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
<script src="https://cdnjs.cloudflare.com/ajax/libs/swagger-ui/5.29.1/swagger-ui-bundle.js" integrity="sha512-iDdi6WwSTimAFh1NhPyFZpHJWr16m/PupHztElqQL+gQoQDavUATsP9hcvgs9Yci+EUA//WvZYZsCTDhRDKc3g==" crossorigin="anonymous"></script>
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