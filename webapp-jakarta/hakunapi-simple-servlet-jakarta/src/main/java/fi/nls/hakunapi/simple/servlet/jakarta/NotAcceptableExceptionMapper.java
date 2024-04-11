package fi.nls.hakunapi.simple.servlet.jakarta;

import jakarta.ws.rs.NotAcceptableException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;

public class NotAcceptableExceptionMapper implements ExceptionMapper<NotAcceptableException> {

    public Response toResponse(NotAcceptableException exception) {
        return ResponseUtil.exception(Status.NOT_ACCEPTABLE, exception.getMessage());
    }

}
