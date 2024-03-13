package fi.nls.hakunapi.simple.servlet.javax;

import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

public class NotAcceptableExceptionMapper implements ExceptionMapper<NotAcceptableException> {

    public Response toResponse(NotAcceptableException exception) {
        return ResponseUtil.exception(Status.NOT_ACCEPTABLE, exception.getMessage());
    }

}
