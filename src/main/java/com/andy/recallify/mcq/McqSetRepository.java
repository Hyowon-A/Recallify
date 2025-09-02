package com.andy.recallify.mcq;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import com.andy.recallify.user.User;

public interface McqSetRepository extends JpaRepository<McqSet, Long> {

    Optional<McqSet> findById(Long id);

    List<McqSet> findAllByIsPublicTrue();

    boolean existsByTitleAndUser(String title, User user);

    List<McqSet> findAllByUser(User user);
}
