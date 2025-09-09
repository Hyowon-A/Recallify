package com.andy.recallify.set.dto;

import com.andy.recallify.set.model.Flashcard;
import com.andy.recallify.set.model.Set;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FlashcardParser {
    public List<Flashcard> parse(String rawJson, Set set) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        List<Flashcard> dtos = mapper.readValue(rawJson, new TypeReference<>() {});
        return dtos.stream()
                .map(dto -> {
                    Flashcard flashcard = new Flashcard();
                    flashcard.setSet(set);
                    flashcard.setFront(dto.getFront());
                    flashcard.setBack(dto.getBack());
                    return flashcard;
                })
                .toList();
    }
}
