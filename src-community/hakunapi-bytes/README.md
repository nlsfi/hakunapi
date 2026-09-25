# hakunapi-bytes

Community module: one layer that file-backed sources read their bytes through,
from a local file, over HTTP Range, or from an entry of a SOZip archive. It has
no users yet; FlatGeobuf, GeoPackage over the SQLite VFS and PMTiles are meant
to move onto it.

Requires Java 22+ (the FFM API): it is built only via the `ffm` profile in
`src-community/pom.xml`, so a Java 21 build skips it. Dependencies: `slf4j-api`.

## The contract

- `ByteSource`: the file. Shared by every stream reading it, thread-safe.
- `ByteReader`: one stream's access, from `ByteSource.open()`. Not thread-safe.

`read(off, dst, dstOff, len)` copies into a buffer the caller owns, a segment
or a heap array, and returns the number of bytes read: less than `len` only at
the end of the file, `0` at or past it.

`fetch(off, len)` is zero-copy, offered only by `MappedFile` and
`InMemoryFile`: the whole file as one segment, valid until the source is
closed. Every other source returns
`null`, and the caller falls back to `read`.

State per stream (read-ahead, staging buffers) lives in the reader. Caches are
shared by every reader on the source, and bounded.

## Opening a location

`ByteSources.open(location, cacheBytes, mode)` takes a location in a subset of
GDAL's virtual file syntax:

| Location | Means |
| --- | --- |
| `/data/foo.gpkg` | local file |
| `https://h/foo.gpkg`, `/vsicurl/https://h/foo.gpkg` | HTTP Range |
| `/vsizip//data/foo.zip/foo.gpkg` | entry of a local archive |
| `/vsizip//vsicurl/https://h/foo.zip/foo.gpkg` | entry of a remote archive |
| `/vsizip/{/vsicurl/https://h/foo.zip?sig}/foo.gpkg` | archive with a query (a presigned URL) |
| `/vsizip//data/foo.zip` | the archive's only entry |

The archive ends after the first path segment ending in `.zip`. Any other
`/vsi` prefix is refused.

There is at most one cache, on top:

| | plain file | SOZip entry |
| --- | --- | --- |
| local | none (the OS page cache) | chunk blocks, no read-ahead |
| HTTP | 4 KiB blocks, read-ahead up to 512 KiB | chunk blocks, read-ahead up to 512 KiB |

`cacheBytes` `0` means no cache. `mode` is `PREAD`, `MMAP` (a local file
mapped; `PREAD` over HTTP) or `MEMORY` (the file or entry read whole into
memory at open, with no cache).

## Sources

| Class | Reads |
| --- | --- |
| `PreadFile` | positional reads on one shared `FileChannel`, the default |
| `MappedFile` | a whole-file read-only mapping, opt-in |
| `InMemoryFile` | any source copied whole into native memory at open |
| `HttpRangeReader` | one HTTP Range request per read |
| `SOZipEntry` | inflates the chunks a read covers |
| `CachingByteSource` | shared LRU of blocks with per-reader adaptive read-ahead |

- For `PreadFile`, pass native memory as `dst`, or the JDK stages every read
  through a temporary direct buffer.
- An interrupt closes a `FileChannel` for every thread sharing it. `PreadFile`
  reopens it for the others.
- `HttpRangeReader` reuses sockets through the JDK's keep-alive pool, which
  keeps at most `http.maxConnections` (default 5) idle connections per host.
- `CachingByteSource.hits()` and `fetches()` show whether the cache evicts.

## SOZip

`SOZip.open(archive, entry)`, in `fi.nls.hakunapi.bytes.sozip`, opens a
Deflate entry of a ZIP archive, read through any source, over its uncompressed
bytes (https://github.com/sozip/sozip-spec).

- The entry needs a SOZip index, unless it is at most 1 MiB, which is inflated
  whole. A larger entry without an index, or an invalid index, is refused.
- The index is found under the spec's name `dir/.name.sozip.idx` or GDAL's,
  `a/.b/c.sozip.idx` for `a/b/c`.
- Only Deflate; stored and encrypted entries and multi-disk archives are
  refused. Chunks must be below 100 MiB.
- Closing the entry closes the archive.

