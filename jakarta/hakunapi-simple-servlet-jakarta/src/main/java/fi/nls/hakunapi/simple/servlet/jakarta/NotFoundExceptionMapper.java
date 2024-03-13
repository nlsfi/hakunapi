package fi.nls.hakunapi.simple.servlet.jakarta;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;

public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    public Response toResponse(NotFoundException exception) {
        return ResponseUtil.exception(Status.NOT_FOUND, exception.getMessage());
    }

}
