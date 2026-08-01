# TalentIQ AI — Backend

Single-tenant, Candidate-only Spring Boot backend for TalentIQ AI: an AI-powered resume
screening and career-growth assistant for one candidate. Java 17, Spring Boot 3, PostgreSQL,
Flyway, JWT auth, and a real AI engine backed by the Groq API (pluggable behind the `AiService`
interface) for resume analysis, job matching, interview question generation, skill-gap analysis,
AI-recommended job openings, and a conversational AI assistant.

## Tech stack

- Java 17, Spring Boot 3.2.5, Maven
- Spring Web, Spring Data JPA, Spring Security (JWT via jjwt)
- Bean Validation, Lombok, MapStruct
- PostgreSQL 16 + Flyway migrations
- Apache PDFBox (PDF parsing) and Apache POI (DOCX parsing)
- Groq API (`chat/completions`, OpenAI-compatible) for all AI features, called via `RestTemplate` + Jackson
- springdoc-openapi (Swagger UI)
- JUnit 5 + Mockito + AssertJ

## Running locally (without Docker)

### 1. Start PostgreSQL

Create a database and user matching the defaults (or override via env vars):

```sql
CREATE USER talentiq WITH PASSWORD 'talentiq';
CREATE DATABASE talentiq OWNER talentiq;
```

### 2. Run the app

```bash
cd backend
mvn spring-boot:run
```

Flyway automatically creates the schema and seeds a single default Candidate account on first
startup (see **Accounts** below) — there is no self-registration in this app.

The API will be available at `http://localhost:8080/api`, and Swagger UI at
`http://localhost:8080/swagger-ui.html`.

### Environment variables

| Variable | Default | Description |
|---|---|---|
| `GROQ_API_KEY` | *(none — required)* | Groq API key powering every AI feature (resume analysis, matching, interview generation, skill gap, recommended jobs, chat assistant). Get a **free** key (no credit card required) from the [Groq console](https://console.groq.com/keys). The app **fails fast at startup** with a clear error if this is unset or blank. |
| `GROQ_MODEL` | `llama-3.3-70b-versatile` | Groq model name used for `chat/completions` calls |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/talentiq` | JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `talentiq` | DB username |
| `SPRING_DATASOURCE_PASSWORD` | `talentiq` | DB password |
| `JWT_SECRET` | dev-only insecure default | HMAC signing secret — **override in production** |
| `JWT_EXPIRATION_MS` | `86400000` (24h) | Token expiry |
| `APP_CORS_ORIGIN` | `http://localhost:5173,http://localhost` | Comma-separated allowed CORS origins |
| `APP_STORAGE_PATH` | `./uploads` | Local disk path for uploaded resumes |
| `SERVER_PORT` | `8080` | HTTP port |

### Accounts

This app is single-tenant: there is no self-registration, only one seeded default Candidate
account, created by the `V2__seed_default_user.sql` Flyway migration:

| Email | Password |
|---|---|
| `candidate@talentiq.ai` | `Password123!` |

Log in with `POST /api/auth/login` using these credentials to get a JWT.

## Running with Docker

```bash
cd backend
docker build -t talentiq-backend .
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/talentiq \
  -e SPRING_DATASOURCE_USERNAME=talentiq \
  -e SPRING_DATASOURCE_PASSWORD=talentiq \
  -e JWT_SECRET=change-me-in-production \
  -e GROQ_API_KEY=your-groq-api-key \
  talentiq-backend
```

(Point `SPRING_DATASOURCE_URL` at your Postgres instance — adjust the host if running Postgres
in another container on a shared Docker network.)

## Running tests

```bash
mvn -DskipITs test
```

Covers: `GroqAiServiceImplTest` (Groq-response-to-DTO JSON parsing for resume analysis,
matching, interview generation, skill gap, and recommended jobs — the Groq HTTP call is
mocked, no network access or API key required), `AssistantServiceImplTest` (chat assistant
history mapping, also mocked), `GlobalExceptionHandlerTest` (`AiServiceException` → 502 mapping),
`AuthServiceImplTest` (login, bad password, unknown email), and `AuthControllerTest`
(MockMvc slice test for `/api/auth/*`).

## Architecture

```
controller -> service (interface + impl) -> repository -> entity
                    |
                    v
                dto (request/response) <-> mapper (MapStruct)
```

- `com.talentiq.ai` — the pluggable AI layer. `AiService` is the contract; `GroqAiServiceImpl`
  is the sole implementation, calling the Groq API for every method and parsing its
  JSON-mode responses directly into the response DTOs. `GroqClient` is the shared low-level
  HTTP helper (auth, timeouts, error/safety-block handling) reused by both `GroqAiServiceImpl`
  and the chat assistant (`AssistantServiceImpl`). A future implementation backed by a different
  LLM provider can implement the same `AiService` interface and be swapped in without touching
  any controller or service code.
- `com.talentiq.security` — stateless JWT auth: `JwtService` (sign/verify), `JwtAuthFilter`
  (per-request Bearer token validation), `UserDetailsServiceImpl` / `AppUserPrincipal`.
- `com.talentiq.exception` — `GlobalExceptionHandler` returns a consistent
  `{timestamp, status, error, message, path}` JSON error shape for all error responses, including
  `AiServiceException` (mapped to HTTP 502) for any Groq call failure.

## API summary

Base path: `/api`. JWT required unless marked public. Every account is a `CANDIDATE` — there is
no role-based access control left in this single-tenant app.

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | /api/auth/login | public | Login with the seeded Candidate account, returns JWT |
| GET | /api/auth/me | JWT | Current user info |
| POST | /api/resumes/upload | JWT | Upload PDF/DOCX resume (multipart `file`), returns ATS score |
| GET | /api/resumes/{userId} | JWT | List a user's resume versions |
| GET | /api/resumes/{id}/analysis | JWT | Full ATS analysis breakdown for a resume |
| POST | /api/match | JWT | AI-computed match score between a resume and a pasted job description |
| POST | /api/interview/generate | JWT | Generate 8-12 interview questions for a role |
| GET | /api/skills/gap/{resumeId} | JWT | Skill gap analysis + 8-week roadmap + certifications |
| GET | /api/careers/recommendations/{userId} | JWT | AI-recommended job openings for the Indian IT market (see below) |
| GET | /api/profile | JWT | Current user's profile |
| PUT | /api/profile | JWT | Update headline/experience/education/skills |
| POST | /api/assistant/chat | JWT | Conversational AI assistant (`{message, history}` -> `{reply}`) for resume, ATS, job matching, interview prep, skill gap, and career questions |

### Career recommendations (`GET /api/careers/recommendations/{userId}`)

Returns 4-6 AI-recommended, realistic job openings tailored to the candidate's actual resume
content and skill profile, flavored for the Indian IT job market with compensation expressed in
INR LPA (Lakhs Per Annum). Each element of the returned array has this shape:

```json
{
  "jobTitle": "Senior Backend Engineer",
  "company": "a mid-size Bengaluru fintech startup",
  "location": "Bengaluru, India",
  "matchScore": 88,
  "salaryMinLPA": 18,
  "salaryMaxLPA": 28,
  "marketDemand": "Strong demand for backend engineers with Spring Boot and cloud experience across Indian fintech.",
  "growthPercent": 12,
  "requiredUpskilling": ["Kubernetes", "Kafka"]
}
```

`company` and `location` are illustrative/representative examples of the kind of employer and
place that would realistically hire for that role — not literal scraped job listings.

Full interactive documentation is available at `/swagger-ui.html` once the app is running.
