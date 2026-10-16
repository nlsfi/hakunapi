package fi.nls.hakunapi.simple.servlet.jakarta;

import jakarta.ws.rs.core.StreamingOutput;

/** Entity whose bytes already carry the declared Content-Encoding. */
public interface PreEncodedOutput extends StreamingOutput {
}
