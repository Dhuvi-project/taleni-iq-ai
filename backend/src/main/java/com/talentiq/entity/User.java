package com.talentiq.entity;

import com.talentiq.entity.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The Supabase Auth user id ({@code sub} claim of the verified JWT) this local profile belongs to. */
    @Column(name = "auth_user_id", nullable = false, unique = true)
    private UUID authUserId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 200)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(length = 200)
    private String headline;

    @Column(name = "experience_json", columnDefinition = "TEXT")
    private String experienceJson;

    @Column(name = "education_json", columnDefinition = "TEXT")
    private String educationJson;

    @Column(name = "skills_json", columnDefinition = "TEXT")
    private String skillsJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
