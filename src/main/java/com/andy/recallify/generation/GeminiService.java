package com.andy.recallify.generation;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@Service
public class GeminiService {
    private final String API_KEY;
    private final String API_URL;

    public GeminiService(@Value("${gemini.api-key}") String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Missing GEMINI_API_KEY environment variable!");
        }
        this.API_KEY = apiKey;
        this.API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=" + this.API_KEY;
    }

    public String generateMcqs(String inputText, Long count, String level) throws Exception {
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
    
                   Generate exactly %d high-quality MCQs at the %s difficulty level.
    
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
    
                   Strict requirements:
                   - The number of questions must be exactly %d.
                   - The language of the questions and explanations must match the language of the lecture content.
                   - Only ONE correct answer per question.
                   - The "answer" field must be an integer (1–4) matching the correct option.
                   - Explanations must clearly justify why each option is correct or incorrect.
                   - Questions should be well-structured, non-trivial, and reflect the specified difficulty.
    
                   Do not include anything outside the JSON array.
                   Output only the JSON array.
                """.formatted(inputText, count, level, count);

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

    public String generateFlashcards(String inputText, Long count, String level) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");

        // Prompt
        String prompt = """
                You are a flashcard generation assistant (CONCISE MODE).
                
                Create flashcards from this lecture content:
                %s
                
                Generate exactly %d flashcards at the %s difficulty level.
                
                Output (STRICT — JSON array only):
                [
                  { "front": "prompt/question", "back": "concise, correct answer (≤ 1–2 short sentences)" }
                ]
                
                Rules:
                - Use ONLY the lecture content; do not invent facts.
                - Produce exactly %d items; no duplicates; each card self-contained.
                - Mix (approx.): 40%% Definition-Based, 30%% True or False, 30%% Concept→Application.
                - Length caps: front ≤ 100 chars; back ≤ 160 chars (≤ 25 words). No filler.
                - Vary cues; prefer high-yield concepts.
                
                Style per type:
                1) Definition-Based
                   - front: “What is …?” / “Define …” / “Explain … briefly”
                   - back: single crisp sentence; key terms or formula with symbol meanings.
                
                2) True or False
                   - front starts with “True or False: …”
                   - back is strictly “True — …” or “False — …” (brief justification).
                
                3) Concept→Application
                   - front: apply a concept to a simple case (“Which rule applies when …?”, “How do you compute … given …?”)
                   - back: 1–3 key steps or rule summary; short list allowed (use “;”).
                
                Language:
                - Use the same language as the input text.
                
                IMPORTANT:
                - Output ONLY the JSON array—no headings, notes, or extra text.
                """.formatted(inputText, count, level, count);

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
            throw new RuntimeException("Failed to generate flashcardss: " + conn.getResponseCode() + " - " + conn.getResponseMessage());
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