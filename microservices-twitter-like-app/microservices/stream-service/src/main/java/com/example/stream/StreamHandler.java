package com.example.stream;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

public class StreamHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;
    private final String corsAllowedOrigin;

    public StreamHandler() {
        this(DynamoDbClient.builder().build(),
                System.getenv().getOrDefault("TABLE_NAME", "PostsTable"),
                System.getenv().getOrDefault("CORS_ALLOWED_ORIGIN", "*"));
    }

    public StreamHandler(DynamoDbClient dynamoDbClient, String tableName, String corsAllowedOrigin) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
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

            ScanResponse scanResponse = dynamoDbClient.scan(ScanRequest.builder().tableName(tableName).build());
            List<Map<String, Object>> posts = new ArrayList<>();

            for (Map<String, AttributeValue> item : scanResponse.items()) {
                posts.add(toPostDto(item));
            }

            posts.sort(Comparator.comparing(post -> post.get("createdAt").toString(), Comparator.reverseOrder()));

            Map<String, Object> streamBody = new HashMap<>();
            streamBody.put("total", posts.size());
            streamBody.put("posts", posts);

            return buildResponse(200, OBJECT_MAPPER.writeValueAsString(streamBody));
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

    private Map<String, Object> toPostDto(Map<String, AttributeValue> item) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", getString(item, "id"));
        dto.put("content", getString(item, "content"));
        dto.put("authorId", getString(item, "authorId"));
        dto.put("authorName", getString(item, "authorName"));
        dto.put("createdAt", getString(item, "createdAt"));
        return dto;
    }

    private String getString(Map<String, AttributeValue> item, String key) {
        AttributeValue value = item.get(key);
        return value == null ? "" : value.s();
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
