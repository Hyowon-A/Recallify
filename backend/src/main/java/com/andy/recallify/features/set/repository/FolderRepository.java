package com.andy.recallify.features.set.repository;

import com.andy.recallify.features.set.model.Folder;
import com.andy.recallify.features.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    boolean existsByTitleAndUser(String title, User user);

    List<Folder> findAllByUserOrderByCreatedAtDesc(User user);

    Optional<Folder> findByIdAndUser(Long id, User user);

    @Modifying
    @Query("DELETE FROM Folder f WHERE f.id = :folderId AND f.user.id = :userId")
    int deleteOwnedFolder(@Param("folderId") Long folderId, @Param("userId") Long userId);
}
