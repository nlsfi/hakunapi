<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-eOJMYsd53ii+scO/bJGFsiCZc+5NDVN2yr8+0RDqr0Ql0h+rP48ckxlpbzKgwra6" crossorigin="anonymous">
  <title>${model.title!""} - Home</title>
  <base href="${basePathTrailingSlash}">
</head>
<body>
<main>
  <div class="container-lg py-4">
    <nav class="nav justify-content-between" aria-label="breadcrumb">
      <ol class="breadcrumb">
        <li class="breadcrumb-item active" aria-current="page">Home</li>
      </ol>
      <ul class="nav">
        <li class="nav-item">
          <a class="navbar-brand" id="json-link" target="_blank">JSON</a>
        </li>
        <li class="nav-item">
          <a href="https://github.com/nlsfi/hakunapi" target="_blank">
            <svg width="32" height="32" viewBox="0 0 98 96" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M48.854 0C21.839 0 0 22 0 49.217c0 21.756 13.993 40.172 33.405 46.69 2.427.49 3.316-1.059 3.316-2.362 0-1.141-.08-5.052-.08-9.127-13.59 2.934-16.42-5.867-16.42-5.867-2.184-5.704-5.42-7.17-5.42-7.17-4.448-3.015.324-3.015.324-3.015 4.934.326 7.523 5.052 7.523 5.052 4.367 7.496 11.404 5.378 14.235 4.074.404-3.178 1.699-5.378 3.074-6.6-10.839-1.141-22.243-5.378-22.243-24.283 0-5.378 1.94-9.778 5.014-13.2-.485-1.222-2.184-6.275.486-13.038 0 0 4.125-1.304 13.426 5.052a46.97 46.97 0 0 1 12.214-1.63c4.125 0 8.33.571 12.213 1.63 9.302-6.356 13.427-5.052 13.427-5.052 2.67 6.763.97 11.816.485 13.038 3.155 3.422 5.015 7.822 5.015 13.2 0 18.905-11.404 23.06-22.324 24.283 1.78 1.548 3.316 4.481 3.316 9.126 0 6.6-.08 11.897-.08 13.526 0 1.304.89 2.853 3.316 2.364 19.412-6.52 33.405-24.935 33.405-46.691C97.707 22 75.788 0 48.854 0z" fill="#24292f"/></svg>
          </a>
        </li>
      </ul>
    </nav>

    <header class="pb-2 mb-2">
      <h1>${model.title!"Home"}</h1>
      <p>${model.description!"This is an OGC API Features service"}</p>
    </header>
    
    <div class="row">
      <h2>Collections</h2>
      <p><a href="collections">Access the data</a></p>
    </div>

    <div class="row">
      <h2>API Information</h2>
      <p><a href="api.json">OpenAPI 3.0 definition</a></p>
      <p><a href="api.html">Documentation</a></p>
    </div>
    
    <div class="row">
      <h2>Conformance</h2>
      <p><a href="conformance">OGC API conformance classes implemented by this server</a></p>
    </div>
    
    <footer class="pt-3 mt-4 text-muted border-top">Powered by hakunapi</footer>
  </div>
</main>
<script>
const url = new URL(window.location.href);
url.searchParams.set('f', 'json');
document.getElementById("json-link").href = url.toString();
</script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta3/dist/js/bootstrap.bundle.min.js" integrity="sha384-JEW9xMcG8R+pH31jmWH6WWP0WintQrMb4s7ZOdauHnUtxwoG2vI5DkLtS3qm9Ekf" crossorigin="anonymous"></script>
</body>
</html>