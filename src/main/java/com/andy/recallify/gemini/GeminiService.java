package com.andy.recallify.gemini;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@Service
public class GeminiService {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String API_KEY = dotenv.get("GEMINI_API_KEY");
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=" + API_KEY;

    public String generateMcqs(String inputText) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");

        // Prompt
        String prompt = """
                You are a question generation assistant.

                Your task is to create multiple-choice questions (MCQs) based on the following lecture content:

                %s

                Generate 5 high-quality MCQs.

                Each question must follow this exact JSON format:

                [
                  {
                    "question": "What is ...?",
                    "option1": "Option A",
                    "explanation1": "Because ...",
                    "option2": "Option B",
                    "explanation2": "Because ...",
                    "option3": "Option C",
                    "explanation3": "Because ...",
                    "option4": "Option D",
                    "explanation4": "Because ...",
                    "answer": 2
                  }
                ]

                Requirements:
                - Only ONE correct answer per question.
                - Explanations must clearly justify why each option is right or wrong.
                - "answer" field must be an integer (1–4) that matches the correct option.

                Do not add anything outside the JSON.
                Output only the JSON array.
                """.formatted(inputText);

        // Gemini HTTP body
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "role", "user",
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                )
        );

        // Send request
        try (OutputStream os = conn.getOutputStream()) {
            new ObjectMapper().writeValue(os, requestBody);
        }

        // Read response
        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed to generate MCQs: " + conn.getResponseCode() + " - " + conn.getResponseMessage());
        }

        String response = new Scanner(conn.getInputStream()).useDelimiter("\\A").next();
        conn.getInputStream().close();

        // Parse the response JSON
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> responseMap = mapper.readValue(response, Map.class);

        // Navigate the response safely
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseMap.get("candidates");
        if (candidates == null || candidates.isEmpty()) {
            throw new RuntimeException("No candidates returned by Gemini");
        }

        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        if (content == null || content.get("parts") == null) {
            throw new RuntimeException("No content or parts in Gemini response");
        }

        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        if (parts.isEmpty() || !parts.get(0).containsKey("text")) {
            throw new RuntimeException("No text found in Gemini response parts");
        }

        String rawMcqJson = parts.get(0).get("text").toString();

        // Remove Markdown code block (```json ... ```)
        String cleanedJson = rawMcqJson
                .replaceAll("^```json\\s*", "")  // Remove starting ```json
                .replaceAll("\\s*```$", "");     // Remove ending ```

        return cleanedJson.trim();


    }
}