# TalentIQ AI — Frontend

Intelligent Resume Screening & Candidate Evaluation Platform — React 18 + Vite frontend.

## Tech Stack

- React 18 + Vite (JavaScript/JSX, no TypeScript)
- React Router v6
- Axios
- Recharts (charts)
- lucide-react (icons)
- Custom CSS with CSS variables (light/dark theming), no CSS frameworks

## Running Standalone (Development)

Requires Node.js 20+.

```bash
npm install
npm run dev
```

The app runs at `http://localhost:5173`. API calls to `/api/*` are proxied to
`http://localhost:8080` (the Spring Boot backend) via `vite.config.js`. Make sure the
backend is running locally on port 8080.

### Build & Preview

```bash
npm run build
npm run preview
```

## Running via Docker

The included multi-stage `Dockerfile` builds the app with Node 20 and serves the static
output with nginx, proxying `/api` to a `backend` service (matching a docker-compose
service name) on port 8080.

```bash
docker build -t talentiq-frontend .
docker run -p 8080:80 talentiq-frontend
```

In a `docker-compose.yml` alongside a `backend` service:

```yaml
services:
  frontend:
    build: ./frontend
    ports:
      - "8080:80"
    depends_on:
      - backend
  backend:
    build: ./backend
    ports:
      - "8081:8080"
```

nginx serves the SPA with a fallback to `index.html` for client-side routes, and proxies
`/api/*` requests to `http://backend:8080/api/*`.

## Project Structure

```
src/
  api/         Axios client + one module per backend resource
  components/  Reusable UI (AppShell, GlassCard, StatCard, ScoreGauge, SkillBar, ...)
  context/     AuthContext, ThemeContext, ToastContext
  pages/       One file per route (Landing, Login, Dashboard, Upload, Analysis, ...)
  styles/      global.css — CSS variable theme system + animations
```

## Environment / API Contract

The frontend expects a backend reachable at `/api` implementing the TalentIQ AI REST
contract (auth, resumes, match, interview, skills, careers, profile, notifications).
See `src/api/*.js` for exact endpoint shapes.

This is a single-tenant, Candidate-only app — there is no self-registration or
role-based access. Sign in with the one seeded account:

- Email: `candidate@talentiq.ai`
- Password: `Password123!`

## Auth

JWT + user info are stored in `localStorage` (`talentiq_token`, `talentiq_user`) via
`AuthContext`. A 401 response from any API call clears auth state and redirects to
`/login`.
