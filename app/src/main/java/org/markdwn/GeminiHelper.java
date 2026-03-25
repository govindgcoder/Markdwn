package org.markdwn;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class GeminiHelper {

    // Usage:
    // new Thread(() -> {
    //    String result = helper.getResponse("Explain gravity").get();
    //    Platform.runLater(() -> label.setText(result)); Or do something similar (update the ui when done) cuz .get() is blocking
    //}).start();


    // Base Gemini API url
    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent";

    // API key in .env
    private final String apiKey;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    public GeminiHelper(String apiKey) {
        this.apiKey = apiKey;
    }

    // Main function 
    public CompletableFuture<String> getResponse(String prompt) {
        String body = buildRequestBody(prompt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        // this nested format is to get the data from the json
        // {
        // "candidates": [
        //     {
        //     "content": {
        //         "parts": [
        //         { "text": "The actual response text here" }
        //         ]
        //     }
        //     }
        // ]
        // }
        // This is how it looks. Now read the code and understand
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> gson.fromJson(response.body(), JsonObject.class))
                .thenApply(json -> json
                        .getAsJsonArray("candidates")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("content")
                        .getAsJsonArray("parts")
                        .get(0).getAsJsonObject()
                        .get("text").getAsString()
                );
    }

    private String buildRequestBody(String prompt) {
        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", prompt);

        JsonArray parts = new JsonArray();
        parts.add(textPart);

        JsonObject content = new JsonObject();
        content.add("parts", parts);

        JsonArray contents = new JsonArray();
        contents.add(content);

        JsonObject body = new JsonObject();
        body.add("contents", contents);

        // This code generates this o/p 
        // {
        // "contents": [
        //     {
        //     "parts": [
        //         { "text": "prompt" }
        //     ]
        //     }
        // ]
        // }

        return gson.toJson(body);
    }
}
