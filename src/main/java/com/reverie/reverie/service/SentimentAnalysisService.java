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

    @Value("${nlp_service.api_key}")
    private String nlpServiceApiKey;

    @Value("${nlp_service.api_host}")
    private String nlpServiceApiHost;

    @Value("${nlp_service.api_scheme}")
    private String nlpServiceApiScheme;

    @Value("${nlp_service.api_port}")
    private String nlpServiceApiPort;

    private final String API_URL = nlpServiceApiScheme + "://" + nlpServiceApiHost + ":" + nlpServiceApiPort
            + "/analyze";

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Data
    public static class SentimentEmotionResult {
        private final String label;//injsonb
        private final double score;
    }

    @Data
    public static class TextAnalysis {
        private final SentimentEmotionResult sentiment;
        private final SentimentEmotionResult[] emotions;
        private final String[] keywords;
    }

    public TextAnalysis analyzeSentiment(String text) {
        try {
            String jsonBody = objectMapper.writeValueAsString(new TextAnalysisRequest(text));

            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + nlpServiceApiKey)
                    .post(RequestBody.create(
                            MediaType.parse("application/json"),
                            jsonBody))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("NLP service API error: " + response.code() + " " + response.message());
                }

                String responseBody = response.body().string();
                JsonNode root = objectMapper.readTree(responseBody);

                // Parse sentiment
                JsonNode sentimentNode = root.path("sentiment");
                SentimentEmotionResult sentiment = new SentimentEmotionResult(
                        sentimentNode.path("label").asText(),
                        sentimentNode.path("score").asDouble());

                // Parse emotions
                JsonNode emotionsNode = root.path("emotions");
                SentimentEmotionResult[] emotions = new SentimentEmotionResult[emotionsNode.size()];
                for (int i = 0; i < emotionsNode.size(); i++) {
                    JsonNode emotionNode = emotionsNode.get(i);
                    emotions[i] = new SentimentEmotionResult(
                            emotionNode.path("label").asText(),
                            emotionNode.path("score").asDouble());
                }

                // Parse keywords
                JsonNode keywordsNode = root.path("keywords");
                String[] keywords = new String[keywordsNode.size()];
                for (int i = 0; i < keywordsNode.size(); i++) {
                    keywords[i] = keywordsNode.get(i).asText();
                }

                return new TextAnalysis(sentiment, emotions, keywords);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to analyze sentiment: " + e.getMessage(), e);
        }
    }

    @Data
    static class TextAnalysisRequest {
        private final String text;
    }
}