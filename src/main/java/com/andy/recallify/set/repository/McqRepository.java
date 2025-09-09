package com.andy.recallify.set.repository;

import com.andy.recallify.set.model.Mcq;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface McqRepository extends JpaRepository<Mcq, Long> {

    List<Mcq> findBySetId(Long setId);
}
