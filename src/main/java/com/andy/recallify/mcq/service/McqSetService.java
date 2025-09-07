package com.andy.recallify.mcq.service;

import com.andy.recallify.mcq.McqSet;
import com.andy.recallify.mcq.dto.EditMcqSetRequest;
import com.andy.recallify.mcq.dto.McqSetDto;
import com.andy.recallify.mcq.repository.McqRepository;
import com.andy.recallify.mcq.repository.McqSetRepository;
import com.andy.recallify.user.User;
import com.andy.recallify.user.UserRepository;
import jakarta.transaction.TransactionScoped;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class McqSetService {

    private final McqSetRepository mcqSetRepository;
    private final UserRepository userRepository;
    private final McqRepository mcqRepository;

    @Autowired
    public McqSetService(McqSetRepository mcqSetRepository, UserRepository userRepository, McqRepository mcqRepository) {
        this.mcqSetRepository = mcqSetRepository;
        this.userRepository = userRepository;
        this.mcqRepository = mcqRepository;
    }

    public Long createSet(String mcqSetTitle, boolean isPublic, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (mcqSetRepository.existsByTitleAndUser(mcqSetTitle, user)) {
            throw new IllegalArgumentException("You already have a set with this title.");
        }
        McqSet mcqSet = new McqSet();
        mcqSet.setUser(user);
        mcqSet.setTitle(mcqSetTitle);
        mcqSet.setPublic(isPublic);
        mcqSetRepository.save(mcqSet);
        return mcqSet.getId();
    }

    public List<McqSetDto> getMyMcqSets(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<McqSet> sets = mcqSetRepository.findAllByUser(user);

        return sets.stream()
                .map(set -> new McqSetDto(
                        set.getId(),
                        set.getTitle(),
                        set.isPublic(),
                        set.getMcqs().size()
                ))
                .toList();
    }

    public List<McqSetDto> getPublicMcqSets() {
        List<McqSet> sets = mcqSetRepository.findAllByIsPublicTrue();

        return sets.stream()
                .map(set -> new McqSetDto(
                        set.getId(),
                        set.getTitle(),
                        set.isPublic(),
                        set.getMcqs().size()
                ))
                .toList();
    }

    public McqSetDto getMcqSetById(Long id) {
        McqSet set = mcqSetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MCQ Set not found: " + id));

        // map entity → DTO
        return new McqSetDto(
                set.getId(),
                set.getTitle(),
                set.isPublic(),
                set.getMcqs() != null ? set.getMcqs().size() : 0
        );
    }

    public void deleteMcqSetById(Long id) {
        mcqSetRepository.deleteById(id);
    }

    @Transactional
    public void editMcqSet(EditMcqSetRequest req) {
        McqSet set = mcqSetRepository.findById(req.setId())
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
