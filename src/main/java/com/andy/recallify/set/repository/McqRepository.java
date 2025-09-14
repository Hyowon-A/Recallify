package com.andy.recallify.set.repository;

import com.andy.recallify.set.model.Mcq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface McqRepository extends JpaRepository<Mcq, Long> {

    List<Mcq> findBySetId(Long setId);

    @Query("SELECT COUNT(m) FROM Mcq m WHERE m.set.id = :setId")
    int countMcqsBySetId(@Param("setId") Long setId);

    @Query("SELECT m.set.id, COUNT(m) FROM Mcq m WHERE m.set.id IN :setIds GROUP BY m.set.id")
    List<Object[]> countMcqsGroupedBySetIds(@Param("setIds") List<Long> setIds);
}
