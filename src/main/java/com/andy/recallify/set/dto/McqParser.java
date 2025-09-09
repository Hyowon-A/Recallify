package com.andy.recallify.set.dto;

import com.andy.recallify.set.model.Mcq;
import com.andy.recallify.set.model.Set;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class McqParser {
    public List<Mcq> parse(String rawJson, Set set) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        List<Mcq> dtos = mapper.readValue(rawJson, new TypeReference<>() {});
        return dtos.stream()
                .map(dto -> {
                    Mcq mcq = new Mcq();
                    mcq.setMcqSet(set);
                    mcq.setQuestion(dto.getQuestion());
                    mcq.setOption1(dto.getOption1());
                    mcq.setExplanation1(dto.getExplanation1());
                    mcq.setOption2(dto.getOption2());
                    mcq.setExplanation2(dto.getExplanation2());
                    mcq.setOption3(dto.getOption3());
                    mcq.setExplanation3(dto.getExplanation3());
                    mcq.setOption4(dto.getOption4());
                    mcq.setExplanation4(dto.getExplanation4());
                    mcq.setAnswer(dto.getAnswer());
                    return mcq;
                })
                .toList();
    }
}
