package com.andy.recallify.mcq;

import com.andy.recallify.user.User;
import com.andy.recallify.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class McqSetService {

    private final McqSetRepository mcqSetRepository;
    private final UserRepository userRepository;

    @Autowired
    public McqSetService(McqSetRepository mcqSetRepository, UserRepository userRepository) {
        this.mcqSetRepository = mcqSetRepository;
        this.userRepository = userRepository;
    }

    public Long createSet(String mcqSetTitle, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (mcqSetRepository.existsByTitleAndUser(mcqSetTitle, user)) {
            throw new IllegalArgumentException("You already have a set with this title.");
        }
        McqSet mcqSet = new McqSet();
        mcqSet.setUser(user);
        mcqSet.setTitle(mcqSetTitle);
        mcqSetRepository.save(mcqSet);
        return mcqSet.getId();
    }

    public List<McqSetListInfoDto> getMyMcqSets(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<McqSet> sets = mcqSetRepository.findAllByUser(user);

        return sets.stream()
                .map(set -> new McqSetListInfoDto(
                        set.getId(),
                        set.getTitle(),
                        set.isPublic(),
                        set.getMcqs().size()
                ))
                .toList();
    }

    public List<McqSetListInfoDto> getPublicMcqSets() {
        List<McqSet> sets = mcqSetRepository.findAllByIsPublicTrue();

        return sets.stream()
                .map(set -> new McqSetListInfoDto(
                        set.getId(),
                        set.getTitle(),
                        set.isPublic(),
                        set.getMcqs().size()
                ))
                .toList();
    }

}
