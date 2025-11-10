package com.reducess.trinketstore.repository;

import com.reducess.trinketstore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByAuthId(UUID authId);
    boolean existsByAuthId(UUID authId);
}

