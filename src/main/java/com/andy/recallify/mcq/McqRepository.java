package com.andy.recallify.mcq;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface McqRepository extends JpaRepository<Mcq, Long> {

    long countByMcqSetId(Long mcqSetId);

    List<Mcq> findByMcqSetId(Long mcqSetId);
}
