package com.talentiq.repository;

import com.talentiq.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByUserIdOrderByVersionDesc(Long userId);
    long countByUserId(Long userId);
    List<Resume> findTop10ByOrderByCreatedAtDesc();
}
