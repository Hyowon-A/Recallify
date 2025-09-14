package com.andy.recallify.set.service;

import com.andy.recallify.set.dto.PublicSetDto;
import com.andy.recallify.set.dto.SetStatsDto;
import com.andy.recallify.set.model.*;
import com.andy.recallify.set.dto.EditSetRequest;
import com.andy.recallify.set.dto.SetDto;
import com.andy.recallify.set.repository.*;
import com.andy.recallify.user.model.User;
import com.andy.recallify.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SetService {

    private final SetRepository setRepository;
    private final UserRepository userRepository;
    private final McqRepository mcqRepository;
    private final FlashcardRepository flashcardRepository;
    private final FlashcardSRSRepository flashcardSRSRepository;
    private final McqSRSRepository mcqSRSRepository;

    @Autowired
    public SetService(SetRepository setRepository, UserRepository userRepository, McqRepository mcqRepository, FlashcardRepository flashcardRepository, FlashcardSRSRepository flashcardSRSRepository, McqSRSRepository mcqSRSRepository) {
        this.setRepository = setRepository;
        this.userRepository = userRepository;
        this.mcqRepository = mcqRepository;
        this.flashcardRepository = flashcardRepository;
        this.flashcardSRSRepository = flashcardSRSRepository;
        this.mcqSRSRepository = mcqSRSRepository;
    }

    public Long createSet(String mcqSetTitle, boolean isPublic, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (setRepository.existsByTitleAndUser(mcqSetTitle, user)) {
            throw new IllegalArgumentException("You already have a set with this title.");
        }
        Set set = new Set();
        set.setUser(user);
        set.setTitle(mcqSetTitle);
        set.setPublic(isPublic);
        setRepository.save(set);
        return set.getId();
    }

    public List<SetDto> getMySets(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Set> sets = setRepository.findAllByUser(user);
        List<Long> setIds = sets.stream().map(Set::getId).toList();

        Map<Long, Long> mcqCounts = mcqRepository.countMcqsGroupedBySetIds(setIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        Map<Long, Long> flCounts = flashcardRepository.countFlashcardsGroupedBySetIds(setIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        Map<Long, SetStatsDto> flashStats = flashcardSRSRepository.countFlashcardStatsGrouped(setIds).stream()
                .filter(s -> s.setId() != null)
                .collect(Collectors.toMap(SetStatsDto::setId, s -> s, (s1, s2) -> s1));

        Map<Long, SetStatsDto> mcqStats = mcqSRSRepository.countMcqStatsGrouped(setIds).stream()
                .filter(s -> s.setId() != null)
                .collect(Collectors.toMap(SetStatsDto::setId, s -> s, (s1, s2) -> s1));

        return sets.stream()
                .map(set -> toSetDto(
                        set,
                        true,
                        mcqCounts.get(set.getId()),
                        flCounts.get(set.getId()),
                        mcqStats.get(set.getId()),
                        flashStats.get(set.getId())))
                .toList();
    }

    public List<SetDto> getPublicSets(String email) {
        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found")).getId();

        List<Set> sets = setRepository.findAllByIsPublicTrue();
        List<Long> setIds = sets.stream().map(Set::getId).toList();

        Map<Long, Long> mcqCounts = mcqRepository.countMcqsGroupedBySetIds(setIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        Map<Long, Long> flCounts = flashcardRepository.countFlashcardsGroupedBySetIds(setIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        Map<Long, SetStatsDto> flashStats = flashcardSRSRepository.countFlashcardStatsGrouped(setIds).stream()
                .collect(Collectors.toMap(SetStatsDto::setId, s -> s));

        Map<Long, SetStatsDto> mcqStats = mcqSRSRepository.countMcqStatsGrouped(setIds).stream()
                .collect(Collectors.toMap(SetStatsDto::setId, s -> s));

        return sets.stream()
                .map(set -> toSetDto(
                        set,
                        set.getUser().getId().equals(userId),
                        mcqCounts.get(set.getId()),
                        flCounts.get(set.getId()),
                        mcqStats.get(set.getId()),
                        flashStats.get(set.getId())))
                .toList();
    }

    public SetDto getSetById(Long id) {
        Set set = setRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Set not found"));

        Long setId = set.getId();

        long mcq = mcqRepository.countMcqsBySetId(setId);
        long fl = flashcardRepository.countFlashcardsBySetId(setId);

        SetStatsDto mcqStat = mcqSRSRepository.countMcqStats(setId);
        SetStatsDto flStat = flashcardSRSRepository.countFlashcardStats(setId);

        return toSetDto(set, true, mcq, fl, mcqStat, flStat);
    }

    public void deleteSetById(Long id) {
        setRepository.deleteById(id);
    }

    @Transactional
    public void editSet(EditSetRequest req) {
        Set set = setRepository.findById(req.setId())
                .orElseThrow(() -> new IllegalArgumentException("Set not found"));

        // Conditionally update only if value is provided
        if (req.title() != null && !req.title().trim().isEmpty()) {
            set.setTitle(req.title().trim());
        }

        if (req.isPublic() != null) {
            set.setPublic(req.isPublic());
        }

        if (req.deletedIds() != null) {
            for (Long qid : req.deletedIds()) {
                if (req.type().equals("MCQ")) {
                    mcqRepository.deleteById(qid);
                }
                if (req.type().equals("FLASHCARD")) {
                    flashcardRepository.deleteById(qid);
                }
            }
        }
    }

    @Transactional
    public Long copySet(Long sourceSetId, String email) {
        Set sourceSet = setRepository.findById(sourceSetId)
                .orElseThrow(() -> new IllegalArgumentException("Set not found"));

        User user =  userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Create a new set for the user
        Set copy = new Set();
        copy.setUser(user);
        copy.setTitle(sourceSet.getTitle() + " (Copy)");
        copy.setPublic(false);
        setRepository.save(copy);

        if (sourceSet.getMcqs() != null && !sourceSet.getMcqs().isEmpty()) {
            sourceSet.getMcqs().forEach(mcq -> {
                Mcq newMcq = new Mcq();
                newMcq.setQuestion(mcq.getQuestion());
                newMcq.setOption1(mcq.getOption1());
                newMcq.setExplanation1(mcq.getExplanation1());
                newMcq.setOption2(mcq.getOption2());
                newMcq.setExplanation2(mcq.getExplanation2());
                newMcq.setOption3(mcq.getOption3());
                newMcq.setExplanation3(mcq.getExplanation3());
                newMcq.setOption4(mcq.getOption4());
                newMcq.setExplanation4(mcq.getExplanation4());
                newMcq.setAnswer(mcq.getAnswer());
                newMcq.setSet(copy);
                mcqRepository.save(newMcq);

                // also init SRS row
                McqSRS srs = new McqSRS(newMcq, 0, 0, (float) 2.5, null, null);
                mcqSRSRepository.save(srs);
            });
        }

        if (sourceSet.getFlashcards() != null && !sourceSet.getFlashcards().isEmpty()) {
            sourceSet.getFlashcards().forEach(card -> {
                Flashcard newCard = new Flashcard();
                newCard.setFront(card.getFront());
                newCard.setBack(card.getBack());
                newCard.setSet(copy);
                flashcardRepository.save(newCard);

                // also init SRS row
                FlashcardSRS srs = new FlashcardSRS(newCard, 0, 0, (float) 2.5, null, null);
                flashcardSRSRepository.save(srs);
            });
        }

        return copy.getId();
    }

    private SetDto toSetDto(
            Set set,
            boolean isOwner,
            Long mcqCount,
            Long flashcardCount,
            SetStatsDto mcqStats,
            SetStatsDto flashStats
    ) {
        Long id = set.getId();
        int mcq = Math.toIntExact(mcqCount != null ? mcqCount : 0);
        int fl = Math.toIntExact(flashcardCount != null ? flashcardCount : 0);

        String type = (mcq >= fl) ? "MCQ" : "FLASHCARD"; // tie → MCQ
        int count = type.equals("MCQ") ? mcq : fl;

        SetStatsDto stats = type.equals("MCQ") ? mcqStats : flashStats;
        int newC = stats != null ? Math.toIntExact(stats.newC()) : 0;
        int learn = stats != null ? Math.toIntExact(stats.learn()) : 0;
        int due = stats != null ? Math.toIntExact(stats.due()) : 0;

        return new SetDto(id, set.getTitle(), set.isPublic(), count, type, isOwner, newC, learn, due);
    }

}
