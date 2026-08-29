package com.example.posts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

class PostsHandlerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void getPostsReturnsSortedPosts() throws Exception {
        DynamoDbClient dynamoDbClient = mock(DynamoDbClient.class);
        when(dynamoDbClient.scan(any(ScanRequest.class))).thenReturn(ScanResponse.builder()
                .items(
                        Map.of(
                                "id", AttributeValue.fromS("1"),
                                "content", AttributeValue.fromS("older"),
                                "authorId", AttributeValue.fromS("auth0|1"),
                                "authorName", AttributeValue.fromS("Ana"),
                                "createdAt", AttributeValue.fromS("2026-04-20T10:00:00Z")
                        ),
                        Map.of(
                                "id", AttributeValue.fromS("2"),
                                "content", AttributeValue.fromS("newer"),
                                "authorId", AttributeValue.fromS("auth0|2"),
                                "authorName", AttributeValue.fromS("Luis"),
                                "createdAt", AttributeValue.fromS("2026-04-20T11:00:00Z")
                        )
                )
                .build());

        PostsHandler handler = new PostsHandler(dynamoDbClient, "PostsTable", "http://localhost:5173");

        var response = handler.handleRequest(event("GET", null, Map.of()), null);

        assertEquals(200, response.getStatusCode());
        List<Map<String, Object>> posts = OBJECT_MAPPER.readValue(
                response.getBody(),
                new TypeReference<List<Map<String, Object>>>() {
                });
        assertEquals(2, posts.size());
        assertEquals("2", posts.getFirst().get("id"));
        assertEquals("1", posts.get(1).get("id"));
    }

    @Test
    void createPostReturns401WhenJwtContextIsMissing() {
        DynamoDbClient dynamoDbClient = mock(DynamoDbClient.class);
        PostsHandler handler = new PostsHandler(dynamoDbClient, "PostsTable", "*");

        var response = handler.handleRequest(event("POST", "{\"content\":\"hola\"}", Map.of()), null);

        assertEquals(401, response.getStatusCode());
        assertTrue(response.getBody().contains("Missing authenticated user context"));
        verify(dynamoDbClient, never()).putItem(any(PutItemRequest.class));
    }

    @Test
    void createPostPersistsPostAndReturns201() throws Exception {
        DynamoDbClient dynamoDbClient = mock(DynamoDbClient.class);
        PostsHandler handler = new PostsHandler(dynamoDbClient, "PostsTable", "*");

        var response = handler.handleRequest(event(
                "POST",
                "{\"content\":\"hola lambda\"}",
                Map.of("sub", "auth0|123", "name", "Juan Manuel")
        ), null);

        assertEquals(201, response.getStatusCode());
        Map<String, Object> body = OBJECT_MAPPER.readValue(
                response.getBody(),
                new TypeReference<Map<String, Object>>() {
                });
        assertEquals("hola lambda", body.get("content"));
        assertEquals("auth0|123", body.get("authorId"));
        assertEquals("Juan Manuel", body.get("authorName"));
        verify(dynamoDbClient).putItem(any(PutItemRequest.class));
    }

    private APIGatewayV2HTTPEvent event(String method, String body, Map<String, String> claims) {
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
                .withBody(body)
                .withRequestContext(requestContext)
                .build();
    }
}
