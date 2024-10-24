package com.edoc.repository;

import com.edoc.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, String> {

    @Query("SELECT d FROM Document d WHERE d.owner = ?1 AND d.title = ?2")
    Document findByOwnerAndTitle(String owner, String title);

    List<Document> findByOwner(String id);

    @Query("SELECT d FROM Document d WHERE d.id = ?1 AND d.owner = ?2")
    Document findByUserIdAndDocId(String docId, String userId);

    Document findByTitle(String title);
}
