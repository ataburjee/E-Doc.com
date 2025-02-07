package com.edoc.repository;

import com.edoc.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    @Transactional
    @Query("SELECT u FROM User u WHERE u.username = ?1")
    User findByUsername(String username);

    boolean existsByUsername(String username);

}
