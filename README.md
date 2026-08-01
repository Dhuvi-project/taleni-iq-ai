# TalentIQ AI — Intelligent Resume Screening & Career Assistant

A single-tenant, AI-powered career assistant: upload a resume and get real ATS scoring, job-match analysis against pasted job descriptions, AI-generated interview prep, a skill-gap roadmap, and AI-recommended job openings tailored to the Indian IT market with salary bands in INR LPA (Lakhs Per Annum).

There is no self-registration and no Recruiter/Admin roles — it's a focused single-user app with one seeded login.

## Architecture

```
┌──────────────────────┐        HTTPS/JSON         ┌───────────────────────┐        JDBC        ┌──────────────┐
│   Frontend (React)   │ ────────────────────────▶ │  Backend (Spring Boot) │ ──────────────────▶ │ PostgreSQL 16 │
│  Vite + React Router │ ◀──────────────────────── │  REST API + JWT Auth   │ ◀────────────────── │  (Flyway)     │
│  served by Nginx     │        /api/*             │  AiService (Groq)     │                  └──────────────┘
└──────────────────────┘                           └───────────┬───────────┘
        ▲                                                     ▲ │
        │ docker-compose network: talentiq-net                │ └──▶ Groq API (api.groq.com)
        └─────────────────────────────────────────────────────┘
                                                    pgAdmin (DB inspection) ──▶ PostgreSQL
```

- **Frontend**: React 18 (Vite, JSX), custom CSS with light/dark theming, React Router, Axios, Recharts, lucide-react. Built to a static bundle and served by Nginx, which also reverse-proxies `/api` to the backend container. The app opens at the login screen — no landing page, no registration.
- **Backend**: Java 17 / Spring Boot 3 — layered `controller → service → repository → entity` architecture, Spring Security + stateless JWT, MapStruct DTO mapping, Bean Validation, an `AiService` backed by the Groq API (free, no billing required) for resume analysis, job matching, interview question generation, skill-gap analysis, AI-recommended job openings, and the in-app AI chat assistant, Apache PDFBox/POI resume parsing, Swagger UI at `/swagger-ui.html`.
- **Database**: PostgreSQL 16, schema managed by Flyway (`V1__schema.sql`) plus a seed migration that creates exactly one default Candidate account.
- **Infra**: Docker Compose with 4 services — `postgres`, `backend`, `frontend`, `pgadmin` — multi-stage Dockerfiles for both app images.

## Quick start (Docker — recommended)

```bash
cp .env.example .env
# edit .env and set GROQ_API_KEY (free key, no billing required: https://console.groq.com/keys)
docker compose up --build
```

- Frontend: http://localhost (opens at the login screen)
- Backend API: http://localhost:8080/api
- Swagger UI: http://localhost:8080/swagger-ui.html
- pgAdmin: http://localhost:5050

**Login** with the single seeded account:

| Email | Password |
|---|---|
| `candidate@talentiq.ai` | `Password123!` |

There is no registration screen — this is a single-tenant app.

## Running locally without Docker

See [`backend/README.md`](backend/README.md) and [`frontend/README.md`](frontend/README.md) for standalone setup (local Postgres + `mvn spring-boot:run`, and `npm install && npm run dev`).

## API summary

Base path: `/api`. Every account is `CANDIDATE` — there's no role-based access control left. Full request/response shapes are documented in [`backend/README.md`](backend/README.md) and live at `/swagger-ui.html`.

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | /api/auth/login | Public | Login with the seeded account, returns JWT |
| GET | /api/auth/me | JWT | Current user info |
| POST | /api/resumes/upload | JWT | Upload resume (PDF/DOCX ≤10MB) |
| GET | /api/resumes/{userId} | JWT | List a user's resume versions |
| GET | /api/resumes/{id}/analysis | JWT | ATS breakdown, skills, strengths/weaknesses |
| POST | /api/match | JWT | Resume ↔ pasted job description match score |
| POST | /api/interview/generate | JWT | Generate interview questions for a role |
| GET | /api/skills/gap/{resumeId} | JWT | Skill gap + 8-week roadmap |
| GET | /api/careers/recommendations/{userId} | JWT | AI-recommended job openings (Indian IT market, salary in LPA) |
| GET/PUT | /api/profile | JWT | View/update profile |
| POST | /api/assistant/chat | JWT | Chat with the Groq-powered AI assistant |

## Project structure

```
TalentIQ-AI/
├── backend/            Spring Boot API (Java 17, Maven, Flyway)
├── frontend/           React 18 + Vite SPA
├── docker-compose.yml  4-service orchestration
├── .env.example        Environment template
└── README.md           This file
```

## Tests

- Backend: `cd backend && mvn test` (JUnit 5 + Mockito — AI response parsing, auth service, controller slice tests).
- Frontend: `cd frontend && npm run build` verifies a production build compiles cleanly.
