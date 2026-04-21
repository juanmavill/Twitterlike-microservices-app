package com.example.stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

class StreamHandlerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void getStreamReturnsWrappedSortedPosts() throws Exception {
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

        StreamHandler handler = new StreamHandler(dynamoDbClient, "PostsTable", "http://localhost:5173");

        var response = handler.handleRequest(event("GET"), null);

        assertEquals(200, response.getStatusCode());
        Map<String, Object> body = OBJECT_MAPPER.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {
        });
        assertEquals(2, body.get("total"));
        @SuppressWarnings("unchecked")
        var posts = (java.util.List<Map<String, Object>>) body.get("posts");
        assertEquals("2", posts.getFirst().get("id"));
    }

    @Test
    void postMethodReturns405() {
        DynamoDbClient dynamoDbClient = mock(DynamoDbClient.class);
        StreamHandler handler = new StreamHandler(dynamoDbClient, "PostsTable", "*");

        var response = handler.handleRequest(event("POST"), null);

        assertEquals(405, response.getStatusCode());
        assertTrue(response.getBody().contains("Method not allowed"));
    }

    private APIGatewayV2HTTPEvent event(String method) {
        APIGatewayV2HTTPEvent.RequestContext.Http http = APIGatewayV2HTTPEvent.RequestContext.Http.builder()
                .withMethod(method)
                .build();

        APIGatewayV2HTTPEvent.RequestContext requestContext = APIGatewayV2HTTPEvent.RequestContext.builder()
                .withHttp(http)
                .build();

        return APIGatewayV2HTTPEvent.builder()
                .withRequestContext(requestContext)
                .build();
    }
}
