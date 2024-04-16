package org.wiremock.extensions.template.extensions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformerV2;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;

import java.io.IOException;
import java.util.Map;

public class StubResponseTransformerWithParams implements ResponseTransformerV2 {

    @Override
    public Response transform(Response response, ServeEvent serveEvent) {
        Parameters parameters = serveEvent.getTransformerParameters();
        byte[] body = response.getBody();
        for (Map.Entry<String, Object> a : parameters.entrySet()) {
            body = updateJSONValue(body, a.getKey(), a.getValue());
        }
        return Response.Builder.like(response)
            .but().body(body)
            .build();
    }

    @Override
    public String getName() {
        return "proxy-response-transformer";
    }

    private byte[] updateJSONValue(byte[] jsonData, String path, Object newValue) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonData);

            String[] pathComponents = path.split("\\.");

            updateJsonNode(jsonNode, pathComponents, newValue, 0);

            return objectMapper.writeValueAsBytes(jsonNode);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void updateJsonNode(JsonNode jsonNode, String[] pathComponents, Object newValue, int index) {
        if (index == pathComponents.length - 1) {
            ((com.fasterxml.jackson.databind.node.ObjectNode) jsonNode).put(pathComponents[index], String.valueOf(newValue));
        } else {
            String currentPathComponent = pathComponents[index];
            if (currentPathComponent.equals("*")) {
                // If the current component is "*", traverse all elements in the list
                if (jsonNode.isArray()) {
                    for (JsonNode node : jsonNode) {
                        updateJsonNode(node, pathComponents, newValue, index + 1);
                    }
                }
            } else {
                int arrayIndex = -1;
                if (currentPathComponent.matches("\\d+")) {
                    // If the current component is a number, it's an index of an array
                    arrayIndex = Integer.parseInt(currentPathComponent);
                }
                if (arrayIndex >= 0 && jsonNode.isArray() && arrayIndex < jsonNode.size()) {
                    // If it's a valid array index, traverse that element of the array
                    updateJsonNode(jsonNode.get(arrayIndex), pathComponents, newValue, index + 1);
                } else {
                    // If it's not an array index, traverse the property
                    JsonNode nextNode = jsonNode.get(currentPathComponent);
                    if (nextNode != null) {
                        updateJsonNode(nextNode, pathComponents, newValue, index + 1);
                    }
                }
            }
        }
    }
}
