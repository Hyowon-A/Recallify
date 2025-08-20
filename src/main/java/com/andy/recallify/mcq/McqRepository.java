package com.andy.recallify.mcq;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface McqRepository extends JpaRepository<Mcq, Long> {

    long countByMcqSetId(Long mcqSetId);

}
