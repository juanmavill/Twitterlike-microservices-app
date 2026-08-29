package com.example.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;

class UserHandlerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void getMeReturns401WhenJwtContextIsMissing() {
        UserHandler handler = new UserHandler("*");

        var response = handler.handleRequest(event("GET", Map.of()), null);

        assertEquals(401, response.getStatusCode());
        assertTrue(response.getBody().contains("Missing authenticated user context"));
    }

    @Test
    void getMeReturnsProfileFromClaims() throws Exception {
        UserHandler handler = new UserHandler("http://localhost:5173");

        var response = handler.handleRequest(event("GET", Map.of(
                "sub", "auth0|abc",
                "name", "Juan Manuel",
                "email", "juan@example.com"
        )), null);

        assertEquals(200, response.getStatusCode());
        Map<String, Object> body = OBJECT_MAPPER.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {
        });
        assertEquals("auth0|abc", body.get("auth0UserId"));
        assertEquals("Juan Manuel", body.get("displayName"));
        assertEquals("juan@example.com", body.get("email"));
    }

    private APIGatewayV2HTTPEvent event(String method, Map<String, String> claims) {
        APIGatewayV2HTTPEvent.RequestContext.Http http = APIGatewayV2HTTPEvent.RequestContext.Http.builder()
                .withMethod(method)
                .build();

        APIGatewayV2HTTPEvent.RequestContext.Authorizer.JWT jwt =
                APIGatewayV2HTTPEvent.RequestContext.Authorizer.JWT.builder()
                        .withClaims(claims)
                        .build();

        APIGatewayV2HTTPEvent.RequestContext.Authorizer authorizer =
                APIGatewayV2HTTPEvent.RequestContext.Authorizer.builder()
                        .withJwt(jwt)
                        .build();

        APIGatewayV2HTTPEvent.RequestContext requestContext = APIGatewayV2HTTPEvent.RequestContext.builder()
                .withHttp(http)
                .withAuthorizer(authorizer)
                .build();

        return APIGatewayV2HTTPEvent.builder()
                .withRequestContext(requestContext)
                .build();
    }
}
