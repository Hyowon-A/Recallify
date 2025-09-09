package com.andy.recallify.set.repository;

import com.andy.recallify.set.model.Set;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import com.andy.recallify.user.model.User;

public interface SetRepository extends JpaRepository<Set, Long> {

    Optional<Set> findById(Long id);

    List<Set> findAllByIsPublicTrue();

    boolean existsByTitleAndUser(String title, User user);

    List<Set> findAllByUser(User user);
}
