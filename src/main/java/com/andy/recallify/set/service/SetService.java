package com.andy.recallify.set.service;

import com.andy.recallify.set.model.Set;
import com.andy.recallify.set.dto.EditSetRequest;
import com.andy.recallify.set.dto.SetDto;
import com.andy.recallify.set.repository.FlashcardRepository;
import com.andy.recallify.set.repository.McqRepository;
import com.andy.recallify.set.repository.SetRepository;
import com.andy.recallify.user.model.User;
import com.andy.recallify.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SetService {

    private final SetRepository setRepository;
    private final UserRepository userRepository;
    private final McqRepository mcqRepository;
    private final FlashcardRepository flashcardRepository;

    @Autowired
    public SetService(SetRepository setRepository, UserRepository userRepository, McqRepository mcqRepository, FlashcardRepository flashcardRepository) {
        this.setRepository = setRepository;
        this.userRepository = userRepository;
        this.mcqRepository = mcqRepository;
        this.flashcardRepository = flashcardRepository;
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

        return sets.stream().map(set -> {
            int mcq = (set.getMcqs() == null) ? 0 : set.getMcqs().size();
            int fl  = (set.getFlashcards() == null) ? 0 : set.getFlashcards().size();

            // Only MCQ or FLASHCARD
            String type  = (mcq >= fl) ? "MCQ" : "FLASHCARD"; // tie→MCQ
            int count   = type.equals("MCQ") ? mcq : fl;

            return new SetDto(set.getId(), set.getTitle(), set.isPublic(), count, type, true);
        }).toList();
    }

    public List<SetDto> getPublicSets(String email) {
        List<Set> sets = setRepository.findAllByIsPublicTrue();
        Long me = userRepository.findByEmail(email).get().getId();

        return sets.stream().map(set -> {
            int mcq = (set.getMcqs() == null) ? 0 : set.getMcqs().size();
            int fl  = (set.getFlashcards() == null) ? 0 : set.getFlashcards().size();

            // Only MCQ or FLASHCARD
            String type  = (mcq >= fl) ? "MCQ" : "FLASHCARD"; // tie→MCQ
            int count   = type.equals("MCQ") ? mcq : fl;

            boolean isOwner = set.getUser().getId().equals(me);

            return new SetDto(set.getId(), set.getTitle(), set.isPublic(), count, type, isOwner);
        }).toList();
    }

    public SetDto getSetById(Long id) {
        Set set = setRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MCQ Set not found: " + id));

        int mcq = (set.getMcqs() == null) ? 0 : set.getMcqs().size();
        int fl  = (set.getFlashcards() == null) ? 0 : set.getFlashcards().size();

        // Only MCQ or FLASHCARD
        String type  = (mcq >= fl) ? "MCQ" : "FLASHCARD"; // tie→MCQ
        int count   = type.equals("MCQ") ? mcq : fl;

        // map entity → DTO
        return new SetDto(
                set.getId(),
                set.getTitle(),
                set.isPublic(),
                count,
                type,
                true
        );
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
}
