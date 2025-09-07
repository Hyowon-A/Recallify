package com.andy.recallify.mcq.repository;

import com.andy.recallify.mcq.Mcq;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface McqRepository extends JpaRepository<Mcq, Long> {

    List<Mcq> findBySetId(Long setId);
}
