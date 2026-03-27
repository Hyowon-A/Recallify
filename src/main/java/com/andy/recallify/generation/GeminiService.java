package com.andy.recallify.generation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class GeminiService {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Analyzer LUCENE_ANALYZER = new EnglishAnalyzer();
    private static final double DUPLICATE_SIMILARITY_THRESHOLD = 0.80;

    private final String apiKey;
    private final String apiUrl;
    private final int batchSize;
    private final int maxBatches;
    private final int maxQuestions;

    public GeminiService(
            @Value("${gemini.api-key}") String apiKey,
            @Value("${generation.mcq.batch-size:5}") int batchSize,
            @Value("${generation.mcq.max-batches:6}") int maxBatches,
            @Value("${generation.mcq.max-questions:30}") int maxQuestions
    ) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Missing GEMINI_API_KEY environment variable!");
        }
        if (batchSize < 1 || maxBatches < 1 || maxQuestions < 1) {
            throw new IllegalStateException("MCQ generation batch settings must be positive.");
        }
        this.apiKey = apiKey;
        this.apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;
        this.batchSize = batchSize;
        this.maxBatches = maxBatches;
        this.maxQuestions = maxQuestions;
    }

    public String generateMcqs(String inputText, Long count, String level) throws Exception {
        int targetCount = normalizeTargetCount(count);
        int batchCount = Math.min(maxBatches, (int) Math.ceil((double) targetCount / batchSize));
        List<String> topics = extractTopics(inputText, batchCount);
        if (topics.isEmpty()) {
            topics = Collections.nCopies(batchCount, "General coverage");
        }

        LinkedHashMap<String, Map<String, Object>> unique = new LinkedHashMap<>();
        List<LexicalSignature> lexicalSignatures = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(batchCount);
        try {
            List<CompletableFuture<List<Map<String, Object>>>> futures = new ArrayList<>();
            int remaining = targetCount;
            for (int i = 0; i < batchCount && remaining > 0; i++) {
                int requestedInBatch = Math.min(batchSize, remaining);
                int batchNumber = i + 1;
                String topic = topics.get(i % topics.size());
                remaining -= requestedInBatch;

                futures.add(CompletableFuture.supplyAsync(() -> {
                    try {
                        String batchJson = generateMcqsForTopic(inputText, requestedInBatch, level, topic, batchNumber, batchCount);
                        return parseJsonArray(batchJson);
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    }
                }, executor));
            }

            for (CompletableFuture<List<Map<String, Object>>> future : futures) {
                for (Map<String, Object> mcq : future.join()) {
                    tryAddUniqueMcq(mcq, unique, lexicalSignatures);
                }
            }
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception ex) {
                throw ex;
            }
            throw e;
        } finally {
            executor.shutdown();
        }

        int missing = targetCount - unique.size();
        if (missing > 0) {
            for (int i = 0; i < missing; i++) {
                String topic = topics.get(i % topics.size()) + " (new angle)";
                String refillJson = generateMcqsForTopic(inputText, 1, level, topic, i + 1, missing);
                for (Map<String, Object> mcq : parseJsonArray(refillJson)) {
                    tryAddUniqueMcq(mcq, unique, lexicalSignatures);
                }
                if (unique.size() >= targetCount) {
                    break;
                }
            }
        }

        if (unique.size() < targetCount) {
            throw new RuntimeException("Could not generate enough unique MCQs. Needed " + targetCount + ", got " + unique.size());
        }

        List<Map<String, Object>> ordered = new ArrayList<>(unique.values());
        return MAPPER.writeValueAsString(ordered.subList(0, targetCount));
    }

    public String generateFlashcards(String inputText, Long count, String level) throws Exception {
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

        return callGemini(prompt, "Failed to generate flashcards");
    }

    private int normalizeTargetCount(Long count) {
        if (count == null || count < 1) {
            throw new IllegalArgumentException("Question count must be at least 1.");
        }
        if (count > maxQuestions) {
            throw new IllegalArgumentException("Question count exceeds maximum allowed (" + maxQuestions + ").");
        }
        return count.intValue();
    }

    private List<String> extractTopics(String inputText, int topicCount) {
        try {
            String prompt = """
                    Extract exactly %d distinct, high-level topics from the lecture content.
                    Return a JSON array of plain strings only.
                    No markdown, no extra text.
                
                    Lecture content:
                    %s
                    """.formatted(topicCount, inputText);

            String response = callGemini(prompt, "Failed to extract topics");
            JsonNode node = MAPPER.readTree(response);
            if (!node.isArray() || node.isEmpty()) {
                return fallbackTopics(inputText, topicCount);
            }

            List<String> topics = new ArrayList<>();
            for (JsonNode child : node) {
                if (child.isTextual() && !child.asText().isBlank()) {
                    topics.add(child.asText().trim());
                }
            }

            if (topics.size() < topicCount) {
                topics.addAll(fallbackTopics(inputText, topicCount - topics.size()));
            }
            return topics.subList(0, Math.min(topicCount, topics.size()));
        } catch (Exception e) {
            return fallbackTopics(inputText, topicCount);
        }
    }

    private List<String> fallbackTopics(String inputText, int topicCount) {
        String[] chunks = inputText.replaceAll("\\s+", " ").split("[\\.;:\\n]");
        List<String> topics = new ArrayList<>();

        for (String chunk : chunks) {
            String topic = chunk.trim();
            if (!topic.isEmpty()) {
                topics.add(topic.length() > 80 ? topic.substring(0, 80) : topic);
            }
            if (topics.size() >= topicCount) {
                break;
            }
        }

        while (topics.size() < topicCount) {
            topics.add("General subtopic " + (topics.size() + 1));
        }
        return topics;
    }

    private String generateMcqsForTopic(
            String inputText,
            int count,
            String level,
            String topic,
            int batchNumber,
            int totalBatches
    ) throws Exception {
        String levelGuidance = difficultyGuidance(level);
        String prompt = """
                You are a question generation assistant.
                
                Generate exactly %d high-quality MCQs at the %s difficulty level from the lecture content below.
                This is batch %d of %d.
                
                Focus this batch on topic:
                %s
                
                Output MUST be a JSON array only, no markdown.
                Use this exact shape per item:
                {
                  "question": "...",
                  "option1": "...",
                  "explanation1": "...",
                  "option2": "...",
                  "explanation2": "...",
                  "option3": "...",
                  "explanation3": "...",
                  "option4": "...",
                  "explanation4": "...",
                  "answer": 1
                }
                
                Strict requirements:
                - Exactly %d items.
                - Only one correct answer per question.
                - answer must be integer 1..4.
                - Avoid repeating question stems or concepts within this batch.
                - Match lecture language.
                - Do not copy sentences from the lecture verbatim into the question stem.
                - Prioritize applied understanding over direct lookup.
                - At least 70%% of questions should require inference/application (scenario, tradeoff, diagnosis, or choosing best approach).
                - Keep pure definition/recall questions to at most 30%%.
                - Distractors must be plausible and same-domain (no obviously silly options).
                - Distractors should be close enough that learner must reason, not guess by elimination.
                - Use common misconceptions and near-miss alternatives as distractors.
                - Explanations must clarify why the correct option is best and why each incorrect option is tempting but wrong.
                - Distribute correct answer indices (1,2,3,4) as evenly as possible across this batch.
                - The difference between most-used and least-used answer index in the batch should be at most 1.
                - Avoid obvious answer-position patterns (e.g., all 2s, or strict repeating sequence).
                
                Difficulty guidance for this level:
                %s
                
                Lecture content:
                %s
                """.formatted(count, level, batchNumber, totalBatches, topic, count, levelGuidance, inputText);

        return callGemini(prompt, "Failed to generate MCQs");
    }

    private String difficultyGuidance(String level) {
        String normalized = level == null ? "" : level.trim().toLowerCase();
        return switch (normalized) {
            case "easy" -> "- Mostly comprehension-level, but avoid trivial wording.\n- Use short scenarios with one reasoning step.";
            case "hard" -> "- Use multi-step reasoning, edge cases, and nuanced tradeoffs.\n- Distractors should reflect expert-level misconceptions.";
            default -> "- Medium level: blend concept understanding with practical application.\n"
                    + "- Use brief real-world/problem-context stems and require selecting best method, diagnosis, or tradeoff.\n"
                    + "- Avoid direct 'what is X' stems unless needed for one or two foundation checks.";
        };
    }

    private List<Map<String, Object>> parseJsonArray(String rawJson) throws Exception {
        JsonNode node = MAPPER.readTree(rawJson);
        if (!node.isArray()) {
            throw new RuntimeException("Model output is not a JSON array");
        }
        return MAPPER.convertValue(node, new TypeReference<>() {});
    }

    private String fingerprintMcq(Map<String, Object> mcq) {
        return normalize(mcq.get("question"))
                + "|" + normalize(mcq.get("option1"))
                + "|" + normalize(mcq.get("option2"))
                + "|" + normalize(mcq.get("option3"))
                + "|" + normalize(mcq.get("option4"));
    }

    private void tryAddUniqueMcq(
            Map<String, Object> mcq,
            LinkedHashMap<String, Map<String, Object>> unique,
            List<LexicalSignature> lexicalSignatures
    ) {
        String exactKey = fingerprintMcq(mcq);
        if (unique.containsKey(exactKey)) {
            return;
        }
        LexicalSignature candidate = buildLexicalSignature(mcq);
        if (isSemanticallyDuplicate(candidate, lexicalSignatures)) {
            return;
        }
        unique.put(exactKey, mcq);
        lexicalSignatures.add(candidate);
    }

    private boolean isSemanticallyDuplicate(LexicalSignature candidate, List<LexicalSignature> existing) {
        for (LexicalSignature current : existing) {
            double jaccard = jaccard(candidate.tokens(), current.tokens());
            double cosine = cosine(candidate.termFrequency(), candidate.norm(), current.termFrequency(), current.norm());
            if (jaccard >= DUPLICATE_SIMILARITY_THRESHOLD || cosine >= DUPLICATE_SIMILARITY_THRESHOLD) {
                return true;
            }
        }
        return false;
    }

    private LexicalSignature buildLexicalSignature(Map<String, Object> mcq) {
        String text = String.join(" ",
                normalize(mcq.get("question")),
                normalize(mcq.get("option1")),
                normalize(mcq.get("option2")),
                normalize(mcq.get("option3")),
                normalize(mcq.get("option4"))
        );
        List<String> tokens = tokenize(text);
        Map<String, Integer> termFrequency = new HashMap<>();
        for (String token : tokens) {
            termFrequency.merge(token, 1, Integer::sum);
        }
        Set<String> tokenSet = new HashSet<>(tokens);
        double norm = Math.sqrt(termFrequency.values().stream()
                .mapToDouble(v -> (double) v * v)
                .sum());
        return new LexicalSignature(tokenSet, termFrequency, norm);
    }

    private List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        try (TokenStream tokenStream = LUCENE_ANALYZER.tokenStream("mcq", text)) {
            CharTermAttribute term = tokenStream.addAttribute(CharTermAttribute.class);
            List<String> tokens = new ArrayList<>();
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                String token = term.toString();
                if (token.length() > 1) {
                    tokens.add(token);
                }
            }
            tokenStream.end();
            return tokens;
        } catch (IOException e) {
            throw new RuntimeException("Failed to tokenize text for lexical dedupe", e);
        }
    }

    private double jaccard(Set<String> a, Set<String> b) {
        if (a.isEmpty() && b.isEmpty()) {
            return 1.0;
        }
        Set<String> intersection = new HashSet<>(a);
        intersection.retainAll(b);
        Set<String> union = new HashSet<>(a);
        union.addAll(b);
        if (union.isEmpty()) {
            return 0.0;
        }
        return (double) intersection.size() / union.size();
    }

    private double cosine(Map<String, Integer> tfA, double normA, Map<String, Integer> tfB, double normB) {
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }
        Map<String, Integer> small = tfA.size() <= tfB.size() ? tfA : tfB;
        Map<String, Integer> large = tfA.size() <= tfB.size() ? tfB : tfA;
        double dot = 0.0;
        for (Map.Entry<String, Integer> entry : small.entrySet()) {
            Integer other = large.get(entry.getKey());
            if (other != null) {
                dot += (double) entry.getValue() * other;
            }
        }
        return dot / (normA * normB);
    }

    private String normalize(Object value) {
        return value == null ? "" : value.toString().toLowerCase().replaceAll("\\s+", " ").trim();
    }

    private String callGemini(String prompt, String errorPrefix) throws Exception {
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("x-goog-api-key", apiKey);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "role", "user",
                                "parts", List.of(Map.of("text", prompt))
                        )
                )
        );

        try (OutputStream os = conn.getOutputStream()) {
            MAPPER.writeValue(os, requestBody);
        }

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException(errorPrefix + ": " + conn.getResponseCode() + " - " + conn.getResponseMessage());
        }

        String response = new Scanner(conn.getInputStream()).useDelimiter("\\A").next();
        conn.getInputStream().close();

        Map<String, Object> responseMap = MAPPER.readValue(response, Map.class);
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

        return parts.get(0).get("text").toString()
                .replaceAll("^```json\\s*", "")
                .replaceAll("\\s*```$", "")
                .trim();
    }

    private record LexicalSignature(Set<String> tokens, Map<String, Integer> termFrequency, double norm) {
    }
}
