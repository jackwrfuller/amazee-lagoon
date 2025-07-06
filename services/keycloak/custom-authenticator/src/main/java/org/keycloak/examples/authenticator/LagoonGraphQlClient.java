package org.keycloak.examples.authenticator;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LagoonGraphQlClient {

    private static final String GRAPHQL_ENDPOINT = "https://lagoon-api.172.18.0.240.nip.io/graphql";
    private static final String BEARER_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiYWRtaW4iLCJpc3MiOiJsb2NhbGFkbWluIiwiYXVkIjoiYXBpLmRldiIsInN1YiI6ImxvY2FsYWRtaW4ifQ.3teqNDfchVMqMkDfWwbsmMBoeexTI08feKyAMH4AOrs";
    private static final ObjectMapper mapper = new ObjectMapper();

    public static JsonNode getEnvironmentByRoute(String route) throws Exception {
        String query = "query GetEnvironmentByRoute($route: String!) { " +
           "environmentByRoute(route: $route) { " +
           "id " +
           "name " +
           "} " +
           "}";

        // Build JSON payload with query and variables
        String jsonPayload = mapper.createObjectNode()
            .put("query", query)
            .set("variables", mapper.createObjectNode().put("route", route))
            .toString();

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(GRAPHQL_ENDPOINT))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + BEARER_TOKEN)
            .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to fetch data: HTTP " + response.statusCode());
        }

        JsonNode jsonResponse = mapper.readTree(response.body());

        if (jsonResponse.has("errors")) {
            throw new RuntimeException("GraphQL errors: " + jsonResponse.get("errors").toString());
        }

        return jsonResponse.get("data").get("environmentByRoute");
    }
}


