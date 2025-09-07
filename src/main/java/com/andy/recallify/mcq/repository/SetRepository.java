package com.andy.recallify.mcq.repository;

import com.andy.recallify.mcq.Set;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import com.andy.recallify.user.User;

public interface SetRepository extends JpaRepository<Set, Long> {

    Optional<Set> findById(Long id);

    List<Set> findAllByIsPublicTrue();

    boolean existsByTitleAndUser(String title, User user);

    List<Set> findAllByUser(User user);
}
