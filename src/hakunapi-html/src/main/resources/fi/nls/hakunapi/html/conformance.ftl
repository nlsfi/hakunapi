<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-eOJMYsd53ii+scO/bJGFsiCZc+5NDVN2yr8+0RDqr0Ql0h+rP48ckxlpbzKgwra6" crossorigin="anonymous">
  <title>${service.title!""} - Conformance</title>
</head>
<body>
<main>
  <div class="container-lg py-4">
    <nav aria-label="breadcrumb">
      <ol class="breadcrumb">
        <li class="breadcrumb-item"><a class="d-flex align-items-center text-dark text-decoration-none" href="${service.currentServerURL}">Home</a></li>
        <li class="breadcrumb-item active" aria-current="page">Conformance</li>
      </ol>
    </nav>

    <header class="pb-2 mb-2">
      <h1>Conformance</h1>
      <p>This service conforms to following conformance classes</p>
    </header>
    
    <ul>
    <#list model.conformsTo as conformance>
    <li><a href="${conformance}">${conformance}</a></li>
    </#list>
    </ul>
    
    <footer class="pt-3 mt-4 text-muted border-top">Powered by hakunapi &copy; 2023</footer>
  </div>
</main>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta3/dist/js/bootstrap.bundle.min.js" integrity="sha384-JEW9xMcG8R+pH31jmWH6WWP0WintQrMb4s7ZOdauHnUtxwoG2vI5DkLtS3qm9Ekf" crossorigin="anonymous"></script>
</body>
</html>