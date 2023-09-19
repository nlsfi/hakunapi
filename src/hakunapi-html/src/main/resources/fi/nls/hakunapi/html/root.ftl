<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-eOJMYsd53ii+scO/bJGFsiCZc+5NDVN2yr8+0RDqr0Ql0h+rP48ckxlpbzKgwra6" crossorigin="anonymous">
  <title>${model.title!""} - Home</title>
</head>
<body>
<main>
  <div class="container-lg py-4">
    <nav aria-label="breadcrumb">
      <ol class="breadcrumb">
        <li class="breadcrumb-item active" aria-current="page">Home</li>
      </ol>
    </nav>

    <header class="pb-2 mb-2">
      <h1>${model.title!"Home"}</h1>
      <p>${model.description!"This is an OGC API Features service"}</p>
    </header>
    
    <div class="row">
      <h2>Collections</h2>
      <p><a href="${service.currentServerURL}/collections">Access the data</a></p>
    </div>

    <div class="row">
      <h2>API Information</h2>
      <p><a href="${service.currentServerURL}/api.json">OpenAPI 3.0 definition</a></p>
      <p><a href="${service.currentServerURL}/api.html">Documentation</a></p>
    </div>
    
    <div class="row">
      <h2>Conformance</h2>
      <p><a href="${service.currentServerURL}/conformance">OGC API conformance classes implemented by this server</a></p>
    </div>
    
    <footer class="pt-3 mt-4 text-muted border-top">Powered by hakunapi &copy; 2023</footer>
  </div>
</main>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta3/dist/js/bootstrap.bundle.min.js" integrity="sha384-JEW9xMcG8R+pH31jmWH6WWP0WintQrMb4s7ZOdauHnUtxwoG2vI5DkLtS3qm9Ekf" crossorigin="anonymous"></script>
</body>
</html>