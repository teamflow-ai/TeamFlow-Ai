# TeamFlow AI Frontend

Professional React + TypeScript + Vite frontend for the TeamFlow AI microservices project.

## Run

1. Install Node.js 20+.
2. Copy `.env.example` to `.env`.
3. Run:

```bash
npm install
npm run dev
```

Open http://localhost:5173

## Backend integration

The uploaded backend's API Gateway runs on port 8080 and uses Eureka discovery routing.
The auth service DTOs were used to model login/register responses, but the uploaded auth service
does not yet contain controllers/services, so authentication automatically falls back to demo mode
when the endpoint is unavailable.

Expected paths can be changed in `src/api.ts`.

Demo login: enter any valid email and a password of at least 8 characters.

## Production build

```bash
npm run build
npm run preview
```
