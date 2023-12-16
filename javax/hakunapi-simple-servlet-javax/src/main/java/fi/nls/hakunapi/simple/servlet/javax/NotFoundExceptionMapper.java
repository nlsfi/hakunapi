package fi.nls.hakunapi.simple.servlet.javax;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    public Response toResponse(NotFoundException exception) {
        return ResponseUtil.exception(Status.NOT_FOUND, exception.getMessage());
    }

}
