package com.example.user;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class UserHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String corsAllowedOrigin;

    public UserHandler() {
        this(System.getenv().getOrDefault("CORS_ALLOWED_ORIGIN", "*"));
    }

    public UserHandler(String corsAllowedOrigin) {
        this.corsAllowedOrigin = corsAllowedOrigin;
    }

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        try {
            String method = resolveMethod(event);

            if ("OPTIONS".equals(method)) {
                return buildResponse(200, "");
            }

            if (!"GET".equals(method)) {
                return buildError(405, "Method not allowed");
            }

            Map<String, String> claims = extractClaims(event);
            String subject = claims.get("sub");

            if (subject == null || subject.isBlank()) {
                return buildError(401, "Missing authenticated user context");
            }

            Map<String, Object> body = new HashMap<>();
            body.put("auth0UserId", subject);
            body.put("displayName", resolveDisplayName(claims, subject));
            body.put("email", claims.getOrDefault("email", ""));
            body.put("lastSeenAt", Instant.now().toString());

            return buildResponse(200, OBJECT_MAPPER.writeValueAsString(body));
        } catch (Exception ex) {
            return buildError(500, "Unexpected server error");
        }
    }

    private String resolveMethod(APIGatewayV2HTTPEvent event) {
        if (event == null
                || event.getRequestContext() == null
                || event.getRequestContext().getHttp() == null
                || event.getRequestContext().getHttp().getMethod() == null) {
            return "";
        }
        return event.getRequestContext().getHttp().getMethod().toUpperCase();
    }

    private Map<String, String> extractClaims(APIGatewayV2HTTPEvent event) {
        if (event == null
                || event.getRequestContext() == null
                || event.getRequestContext().getAuthorizer() == null
                || event.getRequestContext().getAuthorizer().getJwt() == null
                || event.getRequestContext().getAuthorizer().getJwt().getClaims() == null) {
            return Map.of();
        }

        return event.getRequestContext().getAuthorizer().getJwt().getClaims();
    }

    private String resolveDisplayName(Map<String, String> claims, String fallback) {
        String name = claims.get("name");
        if (name != null && !name.isBlank()) {
            return name;
        }

        String preferredUsername = claims.get("preferred_username");
        if (preferredUsername != null && !preferredUsername.isBlank()) {
            return preferredUsername;
        }

        String email = claims.get("email");
        if (email != null && !email.isBlank()) {
            return email;
        }

        return fallback;
    }

    private APIGatewayV2HTTPResponse buildError(int statusCode, String message) {
        try {
            return buildResponse(statusCode, OBJECT_MAPPER.writeValueAsString(Map.of("error", message)));
        } catch (JsonProcessingException ex) {
            return buildResponse(statusCode, "{\"error\":\"Unexpected error\"}");
        }
    }

    private APIGatewayV2HTTPResponse buildResponse(int statusCode, String body) {
        APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
        response.setStatusCode(statusCode);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", corsAllowedOrigin);
        headers.put("Access-Control-Allow-Headers", "Content-Type,Authorization");
        headers.put("Access-Control-Allow-Methods", "GET,OPTIONS");
        response.setHeaders(headers);

        response.setBody(body);
        return response;
    }
}
