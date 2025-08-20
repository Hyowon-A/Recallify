package com.andy.recallify.mcq;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface McqSetRepository extends JpaRepository<McqSet, Long> {

    Optional<McqSet> findByTitle(String title);

    List<McqSet> findAllByIsPublicTrue();
}
