package com.andy.recallify.set.service;

import com.andy.recallify.set.model.Set;
import com.andy.recallify.set.dto.EditMcqSetRequest;
import com.andy.recallify.set.dto.SetDto;
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

    @Autowired
    public SetService(SetRepository setRepository, UserRepository userRepository, McqRepository mcqRepository) {
        this.setRepository = setRepository;
        this.userRepository = userRepository;
        this.mcqRepository = mcqRepository;
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

    public List<SetDto> getMyMcqSets(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Set> sets = setRepository.findAllByUser(user);

        return sets.stream()
                .map(set -> new SetDto(
                        set.getId(),
                        set.getTitle(),
                        set.isPublic(),
                        set.getMcqs().size()
                ))
                .toList();
    }

    public List<SetDto> getPublicMcqSets() {
        List<Set> sets = setRepository.findAllByIsPublicTrue();

        return sets.stream()
                .map(set -> new SetDto(
                        set.getId(),
                        set.getTitle(),
                        set.isPublic(),
                        set.getMcqs().size()
                ))
                .toList();
    }

    public SetDto getMcqSetById(Long id) {
        Set set = setRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MCQ Set not found: " + id));

        // map entity → DTO
        return new SetDto(
                set.getId(),
                set.getTitle(),
                set.isPublic(),
                set.getMcqs() != null ? set.getMcqs().size() : 0
        );
    }

    public void deleteMcqSetById(Long id) {
        setRepository.deleteById(id);
    }

    @Transactional
    public void editMcqSet(EditMcqSetRequest req) {
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
                mcqRepository.deleteById(qid);
            }
        }
    }
}
