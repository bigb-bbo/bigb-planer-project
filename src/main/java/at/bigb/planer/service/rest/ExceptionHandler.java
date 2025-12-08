package at.bigb.planer.service.rest;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST API
 * Converts exceptions to JSON error responses
 */
@Provider
@Slf4j
public class ExceptionHandler implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        log.error("Exception occurred: {}", exception.getMessage(), exception);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", exception.getMessage());
        errorResponse.put("timestamp", System.currentTimeMillis());

        if (exception instanceof BadRequestException) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorResponse)
                    .header("Content-Type", "application/json")
                    .build();
        } else if (exception instanceof InternalServerErrorException) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errorResponse)
                    .header("Content-Type", "application/json")
                    .build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errorResponse)
                    .header("Content-Type", "application/json")
                    .build();
        }
    }
}

