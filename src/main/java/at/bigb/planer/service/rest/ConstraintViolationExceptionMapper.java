package at.bigb.planer.service.rest;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception mapper for constraint violation exceptions
 */
@Provider
@Slf4j
public class ConstraintViolationExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        log.error("Validation error: {}", exception.getMessage());

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", exception.getMessage());
        errorResponse.put("type", exception.getClass().getSimpleName());
        errorResponse.put("timestamp", System.currentTimeMillis());

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(errorResponse)
                .header("Content-Type", "application/json")
                .build();
    }
}

