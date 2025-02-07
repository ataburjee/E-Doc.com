package com.edoc.repository;

import com.edoc.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, String> {

    @Query("SELECT d FROM Document d WHERE d.userId = :userId AND d.title = :title")
    Document findByUserIdAndTitle(@Param("userId") String userId, @Param("title") String title);

    List<Document> findByUserId(String id);

    @Query("SELECT d FROM Document d WHERE d.id = ?1 AND d.userId = ?2")
    Document findByUserIdAndDocId(String docId, String userId);

//    Document findByTitle(String title);
}
