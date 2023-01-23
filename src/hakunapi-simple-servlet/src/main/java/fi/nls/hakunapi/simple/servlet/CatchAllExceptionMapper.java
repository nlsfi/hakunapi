package fi.nls.hakunapi.simple.servlet;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

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
