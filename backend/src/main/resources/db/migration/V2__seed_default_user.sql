-- Single-tenant seed: the one default Candidate account for this app.
-- Login: candidate@talentiq.ai / Password123!
-- The password_hash below is a real BCrypt hash of "Password123!" (verified via
-- BCryptPasswordEncoder.matches during development), not a placeholder.

INSERT INTO users (name, email, password_hash, role, created_at)
VALUES (
    'Alex Candidate',
    'candidate@talentiq.ai',
    '$2a$10$XPamZ8CvWzhMG2w.HK3bXOc5kuIqo6HYnqOWrt6BGbXHwjUYMRH/a',
    'CANDIDATE',
    now()
);
