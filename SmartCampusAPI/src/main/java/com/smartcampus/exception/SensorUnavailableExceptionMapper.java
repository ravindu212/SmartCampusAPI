package com.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {
    @Override
    public Response toResponse(SensorUnavailableException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Forbidden");
        error.put("message", e.getMessage());
        return Response.status(403)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}