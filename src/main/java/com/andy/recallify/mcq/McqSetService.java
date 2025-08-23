package com.andy.recallify.mcq;

import com.andy.recallify.user.User;
import com.andy.recallify.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class McqSetService {

    private final McqSetRepository mcqSetRepository;
    private final UserRepository userRepository;

    @Autowired
    public McqSetService(McqSetRepository mcqSetRepository, UserRepository userRepository) {
        this.mcqSetRepository = mcqSetRepository;
        this.userRepository = userRepository;
    }

    public void createSet(McqSet mcqSet, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        mcqSet.setUser(user);
        mcqSetRepository.save(mcqSet);
    }

}
