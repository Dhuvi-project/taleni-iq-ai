-- TalentIQ AI schema

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,
    email           VARCHAR(200) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(20) NOT NULL,
    headline        VARCHAR(200),
    experience_json TEXT,
    education_json  TEXT,
    skills_json     TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE resumes (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    file_name     VARCHAR(255) NOT NULL,
    version       INT NOT NULL,
    storage_path  VARCHAR(500) NOT NULL,
    parsed_text   TEXT,
    ats_score     DOUBLE PRECISION,
    created_at    TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_resumes_user_id ON resumes(user_id);

CREATE TABLE skills (
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(150) NOT NULL UNIQUE
);

CREATE TABLE resume_skills (
    id          BIGSERIAL PRIMARY KEY,
    resume_id   BIGINT NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
    skill_id    BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
    matched     BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_resume_skills_resume_id ON resume_skills(resume_id);

CREATE TABLE interview_questions (
    id              BIGSERIAL PRIMARY KEY,
    role            VARCHAR(150) NOT NULL,
    type            VARCHAR(20) NOT NULL,
    question_text   TEXT NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);
