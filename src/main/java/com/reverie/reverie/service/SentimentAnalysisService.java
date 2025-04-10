package com.reverie.reverie.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SentimentAnalysisService {

    private static final String API_URL = "https://api-inference.huggingface.co/models/j-hartmann/emotion-english-distilroberta-base";

    @Value("${huggingface.api.token}")
    private String huggingFaceToken;

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Data
    public static class SentimentAnalysis {
        private final double score;
        private final String emotion;
    }

    public SentimentAnalysis analyzeSentiment(String text) {
        try {
            String jsonBody = objectMapper.writeValueAsString(new HuggingFaceRequest(text));

            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + huggingFaceToken)
                    .post(RequestBody.create(
                            MediaType.parse("application/json"),
                            jsonBody
                    ))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("Hugging Face API error: " + response.code());
                }

                JsonNode root = objectMapper.readTree(response.body().string());
                JsonNode bestPrediction = root.get(0).get(0); // Top result
                String emotion = bestPrediction.get("label").asText();
                double score = bestPrediction.get("score").asDouble();

                return new SentimentAnalysis(score, emotion);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to analyze sentiment: " + e.getMessage(), e);
        }
    }

    @Data
    static class HuggingFaceRequest {
        private final String inputs;
    }
}