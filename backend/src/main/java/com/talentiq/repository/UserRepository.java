package com.talentiq.repository;

import com.talentiq.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByAuthUserId(UUID authUserId);
}
