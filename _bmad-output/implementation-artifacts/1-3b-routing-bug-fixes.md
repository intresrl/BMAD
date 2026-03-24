# Story 1.3b: Routing Bug Fixes — Landing Page & Auth Guard

Status: done

## Story

As a **restaurant owner**,
I want the app to route me correctly based on my authentication state,
so that unauthenticated users land on the login page and authenticated users cannot access auth pages.

## Acceptance Criteria

1. **Given** I open the app root (`/`) without being logged in, **when** the page loads, **then** I am redirected to `/auth/login` (not `/auth/register`).

2. **Given** I navigate to an unknown route without being logged in, **when** the page loads, **then** I am redirected to `/auth/login`.

3. **Given** I navigate to `/auth` (no sub-path) without being logged in, **when** the page loads, **then** I am redirected to `/auth/login`.

4. **Given** I am already logged in (valid token in sessionStorage), **when** I navigate to `/auth/login`, **then** I am redirected to `/dashboard`.

5. **Given** I am already logged in, **when** I navigate to `/auth/register`, **then** I am redirected to `/dashboard`.

6. **Given** I am already logged in, **when** I navigate to `/auth/forgot-password`, **then** I am redirected to `/dashboard`.

## Tasks / Subtasks

- [x] Task 1 — Fix default redirects in `app.routes.ts` (AC: 1, 2)
  - [x] Edit `apps/frontend/src/app/app.routes.ts`
  - [x] Change `{ path: '', redirectTo: 'auth/register', pathMatch: 'full' }` → `{ path: '', redirectTo: 'auth/login', pathMatch: 'full' }`
  - [x] Change `{ path: '**', redirectTo: 'auth/register' }` → `{ path: '**', redirectTo: 'auth/login' }`

- [x] Task 2 — Fix default redirect in `auth.routes.ts` (AC: 3)
  - [x] Edit `apps/frontend/src/app/features/auth/auth.routes.ts`
  - [x] Change `{ path: '', redirectTo: 'register', pathMatch: 'full' }` → `{ path: '', redirectTo: 'login', pathMatch: 'full' }`

- [x] Task 3 — Create `noAuthGuard` (AC: 4, 5, 6)
  - [x] Create `apps/frontend/src/app/core/guards/no-auth.guard.ts`
  - [x] Inject `TokenService` and `Router`
  - [x] If `tokenService.isAuthenticated()` returns `true` → return `router.createUrlTree(['/dashboard'])`
  - [x] Otherwise → return `true`
  - [x] Use `CanActivateFn` functional guard pattern (same pattern as existing `authGuard`)

- [x] Task 4 — Apply `noAuthGuard` to the auth lazy-loaded route (AC: 4, 5, 6)
  - [x] Edit `apps/frontend/src/app/app.routes.ts`
  - [x] Add `canActivate: [noAuthGuard]` to the `{ path: 'auth', loadChildren: ... }` route entry
  - [x] Import `noAuthGuard` from `'./core/guards/no-auth.guard'`

## Dev Notes

- **No backend changes required** — this is a pure Angular frontend fix.
- `TokenService` (`apps/frontend/src/app/core/services/token.service.ts`) already provides `isAuthenticated()` which validates JWT expiry via `payload.exp`. Reuse it directly in the new guard — do NOT duplicate logic.
- The existing `authGuard` at `apps/frontend/src/app/core/guards/auth.guard.ts` is the mirror pattern: it redirects unauthenticated → login. The new `noAuthGuard` is the inverse: authenticated → dashboard.
- `ChangeDetectionStrategy.OnPush` is not relevant here (guards, not components).
- No new modules/packages needed.

### Project Structure Notes

| File | Action |
|---|---|
| `apps/frontend/src/app/app.routes.ts` | Edit — fix two `redirectTo` strings + add `canActivate` on auth route |
| `apps/frontend/src/app/features/auth/auth.routes.ts` | Edit — fix one `redirectTo` string |
| `apps/frontend/src/app/core/guards/no-auth.guard.ts` | Create — new functional guard |

### References

- Existing auth guard pattern: `apps/frontend/src/app/core/guards/auth.guard.ts`
- Token service: `apps/frontend/src/app/core/services/token.service.ts`
- App routes: `apps/frontend/src/app/app.routes.ts`
- Auth routes: `apps/frontend/src/app/features/auth/auth.routes.ts`

## Dev Agent Record

### Agent Model Used

Claude Opus 4.6 (PinoDeiPalazzi — FE-specialized dev agent)

### Debug Log References

N/A — no issues encountered.

### Completion Notes List

- All 4 tasks completed in sequence per story specification.
- `noAuthGuard` created as functional `CanActivateFn`, mirrors `authGuard` (inverse logic).
- Unit tests written with Vitest (project test runner): 2 specs for noAuthGuard — authenticated redirect + unauthenticated pass-through.
- Full test suite: 2 files, 3 tests, all green.

### File List

| File | Action |
|---|---|
| `apps/frontend/src/app/app.routes.ts` | Edited — redirects changed to `auth/login`, `noAuthGuard` added to auth route |
| `apps/frontend/src/app/features/auth/auth.routes.ts` | Edited — default redirect changed to `login` |
| `apps/frontend/src/app/core/guards/no-auth.guard.ts` | Created — functional guard |
| `apps/frontend/src/app/core/guards/no-auth.guard.spec.ts` | Created — 2 unit tests |
