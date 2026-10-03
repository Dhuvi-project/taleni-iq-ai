# TalentIQ AI — Complete Project Documentation

A single-tenant, AI-powered resume and career assistant. Upload a resume and get real ATS scoring, AI job-match analysis against pasted job descriptions, AI-generated interview prep, a skill-gap roadmap, and AI-recommended job openings tailored to the Indian IT market with salary bands in INR LPA — plus a conversational AI assistant.

This document covers the full system end to end: what it does, how each piece was built, the complete tech stack, and exact deployment steps across Supabase, Render, and Vercel.

> Note: this app has no self-registration/sign-up flow. It is single-tenant — one account is created directly in the database/Supabase Auth (see "Authentication" below), and there is no public sign-up screen.

---

## 1. What This App Does (Features)

### Dashboard
Landing page after login. Shows the candidate's resume health at a glance — ATS score, quick stats, and an entry point into every other feature.

### Upload Resume
Drag-and-drop upload for PDF/DOCX resumes (max 10MB). On upload, the backend:
1. Extracts raw text from the file (PDFBox for PDF, Apache POI for DOCX).
2. Stores the file and parsed text, versioned per user (each new upload is a new version, e.g. v1, v2, v3...).
3. Immediately runs AI-based ATS analysis on the extracted text and returns a score.

The page shows only the **current (latest) resume** — not a full version history table — with its ATS score and a link into the full analysis.

### AI Resume Analysis
A single Groq AI call reads the resume's raw text and returns, as structured JSON:
- Overall ATS score (0–100)
- Section-by-section scores (contact, summary, experience, education, skills)
- Keyword density and action-verb count
- 3–6 concrete strengths
- 3–6 concrete weaknesses/improvement areas
- Extracted skills list

This is genuine LLM reasoning over the resume text, not a keyword-matching formula.

### Job Matching
Two independent things on one page:
1. **Match scoring**: paste a job description, pick a resume version, get an AI-computed match score (0–100) plus matched/missing keyword chips — the model reads both texts and judges fit directly.
2. There is intentionally **no real job-listing/browsing feature** — that was removed since there's no recruiter role to post real jobs in this single-tenant app. Job matching is purely "paste a JD, get scored."

### Interview Prep (AI-generated)
Enter a target role and a question type (All / Technical / Behavioral / Coding). Groq generates 8–12 fresh interview questions on demand — genuinely varied per call (see "Notes & Gotchas" below for how variety was tuned in), not pulled from a static bank. Questions are revealed one at a time in a chat-style practice flow.

### Skill Gap Analysis
Pick a resume version. One Groq call:
- Infers the candidate's current skills (with proficiency: Beginner/Intermediate/Advanced) directly from the resume text.
- Infers the single best-fit target role and its required skills.
- Computes a gap score (0–100).
- Produces an 8-week upskilling roadmap (one focus topic + 2–3 resources per week).
- Suggests 3–5 relevant certifications.

No keyword-matching table is used for this — the model does the entire assessment itself, given a hint list of ~100 known skill names for vocabulary grounding.

### Recommended Jobs (Careers page)
Pick a resume version. The AI is given the candidate's profile skills + resume text and returns 4–6 realistic **recommended job openings** (not generic "career paths"):
- Job title, an illustrative representative company/employer type, and location (e.g. "Bengaluru, India")
- A match score (0–100) against the actual resume
- Salary range in **Indian Rupees, expressed as LPA (Lakhs Per Annum)** — e.g. "₹18 – 28 LPA"
- A short market-demand commentary sentence
- Projected growth %
- 2–4 recommended upskilling topics

Company/location are explicitly illustrative (representative of the kind of employer that would hire for that role), not scraped real job postings.

### AI Chat Assistant
A floating chat widget (available on every page) backed by Groq, with a system prompt framing it as a recruitment-platform assistant for resumes/ATS/matching/interview prep/skill gaps/careers. Maintains conversation history within the session.

### Profile
Edit headline, skills (chip input), and structured **experience** entries (title, company, start date, end date, and a **"Currently working here"** checkbox that clears/disables the end date field) and **education** entries (degree, institution, year). Both frontend (blocks save with a clear error) and backend (Bean Validation, `@NotBlank` on required sub-fields) reject incomplete entries — you can't save an experience/education row with blank required fields.

---

## 2. Tech Stack

### Frontend
| Piece | Choice |
|---|---|
| Framework | React 18 (JSX, no TypeScript) |
| Build tool | Vite 5 |
| Routing | React Router v6 |
| HTTP client | Axios |
| Charts | Recharts |
| Icons | lucide-react |
| Styling | Hand-written custom CSS (CSS variables, glassmorphism, light/dark theme) — no CSS framework |
| Auth client | `@supabase/supabase-js` |

### Backend
| Piece | Choice |
|---|---|
| Language / runtime | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Web | Spring Web (REST controllers) |
| Security | Spring Security + **OAuth2 Resource Server** (JWT verification via JWKS — see Authentication) |
| Persistence | Spring Data JPA + Hibernate |
| DB migrations | Flyway |
| Validation | Spring Bean Validation (`jakarta.validation`) |
| Boilerplate reduction | Lombok |
| DTO mapping | MapStruct |
| Resume parsing | Apache PDFBox (PDF), Apache POI (DOCX) |
| API docs | springdoc-openapi (Swagger UI at `/swagger-ui.html`) |
| Build tool | Maven |
| Testing | JUnit 5, Mockito, AssertJ, Spring Security Test |

### Database & Auth
| Piece | Choice |
|---|---|
| Database | PostgreSQL 16/17, hosted on **Supabase** |
| Authentication | **Supabase Auth** (GoTrue) — email/password, JWTs signed with **ES256** (asymmetric), verified by the backend via Supabase's public JWKS endpoint |
| Connection | Supabase's session pooler (`*.pooler.supabase.com:5432`) |

### AI
| Piece | Choice |
|---|---|
| Provider | **Groq** (`api.groq.com`, OpenAI-compatible `chat/completions` API) |
| Model | `openai/gpt-oss-120b` |
| Why Groq | Free tier, no billing setup required, fast inference — swapped in after an earlier attempt with Google Gemini hit a zero-quota free-tier restriction on that particular Google Cloud project |
| Integration pattern | A single `AiService` interface (pluggable) with one implementation, `GroqAiServiceImpl`, calling a shared low-level `GroqClient` HTTP helper |

### Infrastructure / Deployment
| Piece | Choice |
|---|---|
| Frontend hosting | **Vercel** |
| Backend hosting | **Render** (Docker-based Web Service) |
| Database + Auth hosting | **Supabase** |
| Source control | GitHub (`Dhuvi-project/taleni-iq-ai`, branches `main` and `mvp`) |
| Local dev | Docker Compose (backend + frontend containers, pointed at the same hosted Supabase project — no local Postgres) |

---

## 3. Architecture

```
                                   Browser
                                      │
                                      ▼
                        ┌─────────────────────────┐
                        │   Vercel (frontend)      │
                        │   React 18 + Vite SPA    │
                        │   static build + edge    │
                        └───────────┬─────────────┘
                                    │  /api/* rewritten
                                    │  server-side to Render
                                    │  (no browser CORS involved)
                                    ▼
                        ┌─────────────────────────┐
                        │   Render (backend)       │
                        │   Spring Boot 3 / Java17 │
                        │   Docker container       │
                        └──────┬───────────┬───────┘
                               │           │
                 verifies JWT  │           │  JDBC (Hikari pool)
                 via JWKS      │           │
                               ▼           ▼
                    ┌────────────────┐  ┌──────────────────┐
                    │ Supabase Auth  │  │ Supabase Postgres │
                    │ (GoTrue)       │  │ (public schema)   │
                    └────────────────┘  └──────────────────┘

                        Backend also calls out to:
                        ┌──────────────────────────┐
                        │  Groq API (Llama 3.3 70B) │
                        │  resume analysis, match,  │
                        │  interview qs, skill gap, │
                        │  job recs, chat assistant │
                        └──────────────────────────┘
```

**Why the Vercel rewrite matters**: `frontend/vercel.json` proxies every `/api/*` request server-side from Vercel's edge to the Render backend URL. The browser only ever talks to the Vercel domain — it never makes a direct cross-origin request to Render. This sidesteps CORS entirely for the deployed app (CORS is still configured on the backend via `APP_CORS_ORIGIN`, but it's not actually exercised in the normal request path).

---

## 4. Authentication Model (No Sign-Up)

This app deliberately has **no public registration screen**. There is exactly one way an account gets created: a row is inserted directly into Supabase's `auth.users` / `auth.identities` tables (done once, manually, via SQL in the Supabase SQL Editor).

How login and request auth work after that:

1. The frontend uses `@supabase/supabase-js` to call `supabase.auth.signInWithPassword({ email, password })` directly against Supabase — the backend is never involved in login itself.
2. Supabase returns a JWT **signed with ES256** (asymmetric elliptic-curve signing — Supabase's current default, not the older shared-secret HS256 scheme).
3. Every subsequent API call attaches that JWT as `Authorization: Bearer <token>`.
4. The backend (`SecurityConfig` + `SupabaseJwtAuthConverter`) verifies the JWT's signature against Supabase's public JWKS endpoint (`https://<project>.supabase.co/auth/v1/.well-known/jwks.json`) and checks the issuer claim — no shared secret is stored or needed on the backend at all.
5. On a user's **first-ever** authenticated request, `SupabaseJwtAuthConverter` auto-provisions a local `users` row (`auth_user_id`, `email`, a name derived from the email, default role `CANDIDATE`) — there's no separate "create profile" step.
6. `supabase-js` handles token refresh and session persistence in the browser automatically; the Axios client pulls the current token from the Supabase session on every request.

There is a single role, `CANDIDATE` — the app originally had Recruiter/Admin roles and dashboards, which were deliberately removed to keep this a focused single-user tool.

---

## 5. How Each AI Feature Was Actually Built

All AI features go through one interface, `AiService`, with one implementation, `GroqAiServiceImpl`, so the LLM provider could be swapped later without touching any controller/service code. Two low-level call modes are used:

- **`GroqClient.generateJson(prompt, temperature)`** — used for every feature that must return structured data (analysis, matching, skill gap, interview questions, job recommendations). Calls Groq with `response_format: {"type": "json_object"}`, so the model is forced to return valid JSON. The prompt spells out the exact JSON shape expected, with field names matching the response DTOs 1:1, so parsing is a direct Jackson deserialization.
- **`GroqClient.generateChatReply(...)`** — used only by the chat assistant, for free-form multi-turn plain-text replies.

**Gotcha handled**: Groq's JSON mode requires the top-level response to be a JSON *object*, not a bare array. Early versions asked for "a JSON array" for interview questions and job recommendations, which Groq silently wrapped in an object anyway (e.g. `{"questions": [...]}`), breaking naive array parsing. Fixed by explicitly asking for and parsing a wrapper object (`{"questions": [...]}`, `{"recommendations": [...]}`).

**Gotcha handled — variety**: the first version used one fixed low temperature (0.4) for everything, which is right for scoring/analysis (consistency matters) but wrong for interview questions and job recommendations, which converged on near-identical output across repeated calls. Fixed by giving `generateJson` an explicit temperature parameter — analysis/matching/skill-gap stay at 0.4 for consistency, while interview generation runs at 0.95 and job recommendations at 0.8, each with an explicit "don't just repeat the obvious/generic answer" instruction baked into the prompt, plus a random per-call variety token for the interview prompt.

**Resume text is never sent through generic "resume upload" boilerplate parsing logic** — PDFBox/POI extract raw text once at upload time, and it's stored (`resumes.parsed_text`) so every downstream AI feature (analysis, matching, skill gap, careers) reuses the same stored text instead of re-parsing the file each time.

---

## 6. Project Structure

```
TalentIQ-AI/
├── backend/                          Spring Boot API
│   ├── src/main/java/com/talentiq/
│   │   ├── ai/                       AiService interface, GroqClient, GroqAiServiceImpl, SkillCatalog
│   │   ├── config/                   SecurityConfig, CorsConfig, OpenApiConfig
│   │   ├── controller/                8 REST controllers
│   │   ├── dto/                      request/response DTOs + shared ExperienceEntry/EducationEntry
│   │   ├── entity/                   User, Resume, Skill, ResumeSkill, InterviewQuestion + enums
│   │   ├── exception/                GlobalExceptionHandler + typed exceptions
│   │   ├── mapper/                   MapStruct mappers
│   │   ├── repository/               Spring Data JPA repositories
│   │   ├── security/                 SupabaseJwtAuthConverter, AppUserPrincipal, SupabaseAuthenticationToken
│   │   └── service/                  business logic (+ impl/ subpackage)
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/V1__schema.sql
│   ├── src/test/java/...             JUnit/Mockito tests
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                          React + Vite SPA
│   ├── src/
│   │   ├── api/                      one module per backend resource + supabaseClient.js
│   │   ├── components/               AppShell, GlassCard, SkillBar, ScoreGauge, ProtectedRoute, etc.
│   │   ├── context/                  AuthContext, ThemeContext, ToastContext
│   │   ├── pages/                    10 pages (see Features above)
│   │   └── styles/                   global.css (theme, glassmorphism, animations)
│   ├── Dockerfile
│   ├── vercel.json                   /api/* rewrite to Render + SPA fallback
│   └── package.json
├── docker-compose.yml                 local dev: backend + frontend containers only (DB is Supabase)
├── .env.example                       root env template (Supabase + Groq + backend config)
└── PROJECT_DOCUMENTATION.md           this file
```

---

## 7. Database Schema

Single Flyway migration (`V1__schema.sql`) creates the `public` schema (Supabase's own `auth` schema, with `auth.users`/`auth.identities`, is separate and managed by Supabase itself):

| Table | Purpose |
|---|---|
| `users` | App profile per Supabase Auth account. `auth_user_id` (UUID) links to `auth.users.id`. Holds name/email/role plus profile JSON blobs (`experience_json`, `education_json`, `skills_json`) and `headline`. |
| `resumes` | One row per uploaded resume version per user. Stores `parsed_text` (extracted at upload) and `ats_score`. |
| `skills` / `resume_skills` | Present in the schema but not actively used by the current AI-driven implementation (skill extraction is done by the AI model / an in-code `SkillCatalog` list, not a DB-driven join table). Left in place for potential future use. |
| `interview_questions` | Same as above — present but not populated; interview questions are generated live by Groq, not read from this table. |

No `jobs`, `applications`, `audit_logs`, or `notifications` tables exist — those features (real job postings, a recruiter pipeline, audit logging, in-app notifications) were built early in development and deliberately removed as the product scope narrowed to a single-tenant AI career tool.

---

## 8. Deployment — Platforms Used

| Platform | Role | Free tier used? |
|---|---|---|
| **Supabase** | Postgres database + Auth (user identity, JWT issuing) | Yes |
| **Render** | Backend hosting (Docker container running the Spring Boot jar) | Yes — see cold-start note below |
| **Vercel** | Frontend hosting (static Vite build + edge rewrites) | Yes |
| **GitHub** | Source control, the thing Render/Vercel build from | — |
| **Groq** | AI inference API (not "deployment" but a required external dependency) | Yes |

---

## 9. Deployment Steps (Exact Sequence Followed)

### 9.1 Supabase (do this first — everything else needs its values)

1. Create a project at supabase.com (set a database password, pick a region).
2. From **Project Settings → Database**, copy the connection string (Session Pooler tab — works over IPv4, needed for most hosts including Render): host, port `5432`, database `postgres`, username in the form `postgres.<project-ref>`.
3. From **Project Settings → API**, copy the **Project URL** and the **anon public key**.
4. Create the one application user directly via **SQL Editor**, since Supabase's Admin API key formats didn't cooperate with scripted user creation in this project — inserting directly into `auth.users` + `auth.identities` with a `pgcrypto`-hashed password was simpler and is a well-known, supported pattern:

```sql
WITH new_user AS (
  INSERT INTO auth.users (
    instance_id, id, aud, role, email, encrypted_password,
    email_confirmed_at, created_at, updated_at,
    confirmation_token, email_change, email_change_token_new, recovery_token,
    raw_app_meta_data, raw_user_meta_data, is_super_admin, is_sso_user, is_anonymous
  ) VALUES (
    '00000000-0000-0000-0000-000000000000',
    gen_random_uuid(), 'authenticated', 'authenticated',
    'YOUR_EMAIL_HERE', crypt('YOUR_PASSWORD_HERE', gen_salt('bf')),
    now(), now(), now(), '', '', '', '',
    '{"provider":"email","providers":["email"]}'::jsonb, '{}'::jsonb,
    false, false, false
  )
  RETURNING id, email
)
INSERT INTO auth.identities (id, provider_id, user_id, identity_data, provider, last_sign_in_at, created_at, updated_at)
SELECT gen_random_uuid(), id::text, id,
       jsonb_build_object('sub', id::text, 'email', email, 'email_verified', true),
       'email', now(), now(), now()
FROM new_user
RETURNING user_id;
```

This creates a fully email-confirmed account with zero emails sent — necessary because Supabase's free-tier built-in SMTP has a very low send-rate limit, and there's no real inbox to confirm against for a made-up domain anyway.

5. No manual table creation needed for the app schema — Flyway creates it automatically the first time the backend boots against this database.

### 9.2 Render (backend)

1. New **Web Service**, connect the GitHub repo, select branch `main`.
2. **Root Directory**: `backend`. **Runtime**: Docker (auto-detects `backend/Dockerfile`).
3. Environment variables:

```
SPRING_DATASOURCE_URL=jdbc:postgresql://<pooler-host>:5432/postgres
SPRING_DATASOURCE_USERNAME=postgres.<project-ref>
SPRING_DATASOURCE_PASSWORD=<your db password>
SUPABASE_URL=https://<project-ref>.supabase.co
GROQ_API_KEY=<your groq key>
GROQ_MODEL=openai/gpt-oss-120b
APP_CORS_ORIGIN=http://localhost:5173
```

(`PORT` is injected automatically by Render — the app already honors it via `server.port: ${PORT:${SERVER_PORT:8080}}`.)

4. Deploy. First build takes several minutes (full Maven build inside the Docker build stage).

### 9.3 Vercel (frontend)

1. Import the same GitHub repo as a new project.
2. **Root Directory**: `frontend`. **Framework Preset**: Vite (auto-detected). Build command / output directory: defaults (`npm run build` / `dist`).
3. Environment variables (baked into the JS bundle at build time):

```
VITE_SUPABASE_URL=https://<project-ref>.supabase.co
VITE_SUPABASE_ANON_KEY=<your anon public key>
```

4. `frontend/vercel.json` (already committed) handles the rest:

```json
{
  "rewrites": [
    { "source": "/api/(.*)", "destination": "https://<your-render-url>/api/$1" },
    { "source": "/(.*)", "destination": "/index.html" }
  ]
}
```

Update the Render URL inside this file if the backend's URL ever changes, then redeploy.

5. Deploy. Done — the app is live.

---

## 10. Notes, Gotchas & Fixes Encountered Along the Way

These are worth keeping if you ever touch this stack again:

1. **Groq JSON mode needs a top-level object.** Bare-array prompts get silently wrapped by Groq; parse code must expect and unwrap `{"key": [...]}`, not a raw array.
2. **AI creativity vs. consistency needs different temperatures.** One fixed temperature for every AI call is wrong — scoring/analysis wants low temperature (0.4, repeatable), interview/career generation wants high temperature (0.8–0.95) plus explicit "vary your answer" prompt instructions, or every call looks the same.
3. **Supabase JWTs are ES256 (asymmetric), not the older HS256 shared secret.** `NimbusJwtDecoder.withJwkSetUri(...)` defaults to only accepting RS256 — it must be told explicitly via `.jwsAlgorithm(SignatureAlgorithm.ES256)`, or every request fails with "Signed JWT rejected: Another algorithm expected."
4. **Render free tier + default JVM heap sizing = OOM crash loop.** The JVM doesn't automatically size its heap safely against Render's 512MB container limit, leading to the container being silently OOM-killed and restarted on a loop (visible externally as requests randomly flip-flopping between success and Render's own "no-server" placeholder 404). Fixed with explicit `-XX:+UseContainerSupport -XX:MaxRAMPercentage=70.0 -XX:MaxMetaspaceSize=128m` JVM flags in the Dockerfile's `ENTRYPOINT`, plus trimming the Hikari connection pool to 5.
5. **Render free tier cold starts.** The service spins down after ~15 minutes idle; the first request afterward can take up to 30+ seconds. This is a platform limitation of the free tier, not a bug — worth knowing before a live demo (hit the URL once beforehand to "wake" it).
6. **Vercel rewrites eliminate the CORS problem entirely for the deployed app**, since the browser only ever talks to the Vercel origin. `APP_CORS_ORIGIN` on the backend is only relevant if something calls the Render URL directly from a browser at a different origin (e.g. testing Swagger UI).
7. **`@Lob` on a `TEXT` Postgres column with Hibernate causes garbled reads.** Hibernate/the Postgres JDBC driver can interpret a `@Lob String` field as a large-object OID reference (a `bigint`) rather than inline text, corrupting reads of long text columns. Fixed by dropping `@Lob` and keeping just `@Column(columnDefinition = "TEXT")`.
8. **`@Transactional(propagation = REQUIRES_NEW)` inside the same request that just created the referenced row will fail a foreign-key check.** A separate transaction can't see another transaction's uncommitted insert. Fixed by using default (`REQUIRED`) propagation so audit-style calls join the caller's transaction instead of racing it.
9. **Self-invocation defeats Spring's `@Transactional` proxy.** Calling an `@Transactional`-annotated method on `this` from within the same bean bypasses the AOP proxy entirely — the annotation silently does nothing. Worth remembering whenever refactoring service internals.
10. **Standalone MockMvc tests don't wire `@AuthenticationPrincipal` resolution or the Spring Security filter chain automatically.** Needed `.setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())` plus manually populating `SecurityContextHolder` in the test (rather than relying on `SecurityMockMvcRequestPostProcessors.authentication(...)`, which needs the real filter chain to do anything in a standalone setup).

---

## 11. Local Development

```bash
git clone git@github.com:Dhuvi-project/taleni-iq-ai.git
cd taleni-iq-ai

# Backend + frontend containers, both pointed at your hosted Supabase project
cp .env.example .env
# fill in SUPABASE_URL, SPRING_DATASOURCE_*, GROQ_API_KEY in .env
docker compose up --build
```

Or run each side natively without Docker:

```bash
# Backend
cd backend
mvn spring-boot:run   # reads the same env vars

# Frontend
cd frontend
cp .env.example .env.local   # fill in VITE_SUPABASE_URL / VITE_SUPABASE_ANON_KEY
npm install
npm run dev            # http://localhost:5173, proxies /api to localhost:8080 via vite.config.js
```

There is no local Postgres in this setup — local dev uses the same hosted Supabase database as production, since the whole point of the architecture is that Supabase *is* the database.

---

## 12. Testing

- **Backend**: `cd backend && mvn test` — JUnit 5 + Mockito, covering AI response JSON parsing (with the Groq HTTP call mocked, so tests never hit the network or need a real API key), auth/profile service logic, and controller slice tests.
- **Frontend**: `cd frontend && npm run build` — verifies the production build compiles cleanly (no automated UI test suite; manual verification was used throughout development).
