package fi.nls.hakunapi.simple.servlet.jakarta;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CatchAllExceptionMapper implements ExceptionMapper<Exception> {
    
    private static final Logger LOG = LoggerFactory.getLogger(CatchAllExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        LOG.error("Exception occured", exception);
        return ResponseUtil.exception(Status.INTERNAL_SERVER_ERROR, exception.getMessage());
    }

}
