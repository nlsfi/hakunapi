package fi.nls.hakunapi.simple.servlet;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import fi.nls.hakunapi.core.schemas.FeaturesException;

public class ResponseUtil {

    public static Response exception(Status status, String message) {
        return Response
                .status(status)
                .entity(new FeaturesException(status.getStatusCode(), message))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

}
