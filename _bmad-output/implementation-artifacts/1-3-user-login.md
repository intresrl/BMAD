# Story 1.3: User Login

Status: done

## Story

As a **restaurant owner**,
I want to log in with my email and password,
so that I can access my restaurant's data securely.

## Acceptance Criteria

1. **Given** I have a registered account, **when** I submit my correct email and password on the login screen, **then** the server returns an access token (15-minute expiry) and sets a refresh token in an HttpOnly cookie (30-day expiry, rotated on every use); I am redirected to the dashboard; and the `plan` and `tenantId` claims are present in the JWT payload.

2. **Given** navigating to any protected route without a valid access token, **when** I am redirected to the login screen, **then** the `returnUrl` is preserved as a query parameter; after successful login I am redirected to the original `returnUrl`.

3. **Given** I submit incorrect credentials (wrong email or wrong password), **when** the API processes the request, **then** it returns HTTP 401 with RFC 7807 Problem Detail without revealing whether the email or password was wrong; the UI displays an inline error: "Credenziali non valide. Controlla email e password."

4. **Given** the auth login endpoint, **when** a client IP exceeds 5 login attempts within 60 seconds, **then** the API returns HTTP 429 with RFC 7807 Problem Detail and a `Retry-After` header indicating seconds remaining in the window.

## Tasks / Subtasks

### Backend

- [x] Task 1 — Create `LoginRequest.kt` DTO (AC: 1, 3)
  - [x] Create `apps/backend/src/main/kotlin/com/foodcost/auth/dto/LoginRequest.kt`
  - [x] `data class LoginRequest(@field:NotBlank val email: String, @field:NotBlank val password: String)`
  - [x] Use `@NotBlank` only (not `@Email`) — avoids leaking validation errors that could distinguish "malformed email" from "wrong password" (anti-enumeration protection)
  - [x] Import: `jakarta.validation.constraints.NotBlank`

- [x] Task 2 — Create `InvalidCredentialsException.kt` (AC: 3)
  - [x] Create `apps/backend/src/main/kotlin/com/foodcost/auth/service/InvalidCredentialsException.kt`
  - [x] `class InvalidCredentialsException : RuntimeException("Invalid credentials")`
  - [x] Place in `com.foodcost.auth.service` package (same as `EmailAlreadyExistsException`)

- [x] Task 3 — Add `login()` method to `AuthService` (AC: 1, 3, 4)
  - [x] Add to `apps/backend/src/main/kotlin/com/foodcost/auth/service/AuthService.kt`
  - [x] Signature: `fun login(request: LoginRequest): Pair<AuthResponse, String>`
  - [x] Step 1: `val user = userRepository.findByEmail(request.email)` — if null, call `passwordEncoder.matches(request.password, DUMMY_HASH)` then throw `InvalidCredentialsException` (timing-attack prevention: always run expensive hash comparison regardless of whether user exists)
  - [x] Step 2: if user found but `!passwordEncoder.matches(request.password, user.passwordHash)` → throw `InvalidCredentialsException`
  - [x] Step 3: generate raw refresh token via `jwtService.generateRefreshToken()`
  - [x] Step 4: hash with `sha256Hex()` (already in `AuthService`); save `RefreshToken(userId = user.id!!, tokenHash, expiresAt = now + 30 days)`
  - [x] Step 5: generate access token via `jwtService.generateAccessToken(user)`
  - [x] Return `AuthResponse(accessToken, UserDto.from(user))` to rawRefreshToken
  - [x] Annotate with `@Transactional`
  - [x] Add `companion object { private val DUMMY_HASH = Argon2PasswordEncoder(16, 32, 4, 65536, 3).encode("dummy-prevent-timing-attack") }` or compute it lazily in the class — this ensures the same CPU cost path when user doesn't exist
  - [x] **DO NOT log** the email or password in any shape within this method

- [x] Task 4 — Add `login` endpoint to `AuthController` (AC: 1)
  - [x] Add to `apps/backend/src/main/kotlin/com/foodcost/auth/AuthController.kt`
  - [x] `@PostMapping("/login") fun login(@Valid @RequestBody request: LoginRequest, response: HttpServletResponse): ResponseEntity<AuthResponse>`
  - [x] Reuse the exact same cookie-setting logic from `register()`: HttpOnly=true, Secure=true, path="/api/v1/auth/refresh", maxAge=30×24×60×60, SameSite=Strict
  - [x] Return `ResponseEntity.ok(authResponse)` (HTTP 200 — not 201, login is not resource creation)
  - [x] Add `LoginRequest` import

- [x] Task 5 — Add `InvalidCredentialsException` handler to `GlobalExceptionHandler` (AC: 3)
  - [x] Add to `apps/backend/src/main/kotlin/com/foodcost/config/GlobalExceptionHandler.kt`
  - [x] Handler: `@ExceptionHandler(InvalidCredentialsException::class) @ResponseStatus(HttpStatus.UNAUTHORIZED) fun handleInvalidCredentials(...): ProblemDetail`
  - [x] Response body: `ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid credentials").also { it.type = URI.create("https://foodcost.app/errors/invalid-credentials"); it.title = "Unauthorized" }`
  - [x] **DO NOT** include whether the email or password was wrong in the detail message (anti-enumeration)

- [x] Task 6 — Add `Retry-After` header to `RateLimitFilter` 429 response (AC: 4)
  - [x] Edit `apps/backend/src/main/kotlin/com/foodcost/auth/filter/RateLimitFilter.kt`
  - [x] When returning 429, calculate: `val retryAfterSeconds = windowSeconds - (now - entry.windowStart) / 1000`
  - [x] Add: `response.setHeader("Retry-After", retryAfterSeconds.toString())`
  - [x] Place the header call BEFORE `response.writer.write(...)`
  - [x] The existing `shouldNotFilter` already restricts this filter to `/api/v1/auth/` — no change needed there; this covers the login endpoint automatically

- [x] Task 7 — Add explicit rate-limit properties to `application.properties` (AC: 4)
  - [x] Add to `apps/backend/src/main/resources/application.properties`:
    ```
    # Rate limiting (auth endpoints — max 5 attempts/60s per IP)
    app.rate-limit.max-requests=5
    app.rate-limit.window-seconds=60
    ```
  - [x] These already have `@Value` defaults in `RateLimitFilter` — making them explicit in properties makes tuning obvious

- [x] Task 8 — Backend tests (AC: 1, 3, 4)
  - [x] `AuthServiceTest.kt` in `src/test/kotlin/com/foodcost/auth/` — unit tests with MockK
    - [x] `login_withValidCredentials_returnsAuthResponseAndRefreshToken()`
    - [x] `login_withNonExistentEmail_throwsInvalidCredentialsException()`
    - [x] `login_withWrongPassword_throwsInvalidCredentialsException()`
  - [x] `AuthControllerIntegrationTest.kt` — `@SpringBootTest` + `MockMvc` (same pattern as Story 1.2)
    - [x] `POST /api/v1/auth/login` with valid credentials → 200 + AuthResponse + refreshToken cookie
    - [x] `POST /api/v1/auth/login` with wrong password → 401 + RFC 7807 Problem Detail
    - [x] `POST /api/v1/auth/login` with non-existent email → 401 + same RFC 7807 message (verify same message as wrong password — anti-enumeration)
    - [x] `POST /api/v1/auth/login` with blank email → 400 (NotBlank validation)

### Frontend

- [x] Task 9 — Add `login()` method to `AuthService` (Angular) (AC: 1)
  - [x] Edit `apps/frontend/src/app/features/auth/auth.service.ts`
  - [x] Add `login(email: string, password: string): Observable<AuthResponse>` — POST `/api/v1/auth/login` with `{ email, password }`
  - [x] Return type: `Observable<AuthResponse>` (same shape as `register()`)
  - [x] No changes to existing `register()` method

- [x] Task 10 — Create `LoginComponent` (AC: 1, 2, 3)
  - [x] Create `apps/frontend/src/app/features/auth/login/login.component.ts` (and `.html`, `.scss`)
  - [x] Selector: `fc-login`; `ChangeDetectionStrategy.OnPush` (MANDATORY — architecture rule)
  - [x] `ReactiveFormsModule` form with `FormBuilder`: fields `email` (`Validators.required`) and `password` (`Validators.required`)
  - [x] No `Validators.email` or `Validators.minLength` — frontend validates only presence; server-side normalization handles format. (Keeping validation minimal prevents giving hints about input format in login context)
  - [x] Inject: `FormBuilder`, `AuthService`, `Router`, `TokenService`, `ActivatedRoute`
  - [x] On submit: read `returnUrl` from `route.snapshot.queryParamMap.get('returnUrl') || '/dashboard'`; call `authService.login(email, password)`, on success: `tokenService.setToken(response.accessToken)` then `router.navigateByUrl(returnUrl)`
  - [x] On error: `isSubmitting.set(false)`; set `serverError` to `err?.error?.detail ?? 'Errore durante il login. Riprova.'` — use a **generic message in the template** that matches anti-enumeration ("Credenziali non valide. Controlla email e password.")
  - [x] Signals: `protected readonly isSubmitting = signal(false)`, `protected readonly serverError = signal<string | null>(null)`
  - [x] Button disabled when `form.invalid || isSubmitting()`; show `mat-spinner` inside button while `isSubmitting()`
  - [x] Template: two `mat-form-field` blocks (email + password), generic `mat-error` below form for server error, mat-raised-button CTA "Accedi"
  - [x] Password field: `type="password"`, no show/hide toggle for MVP
  - [x] "Hai dimenticato la password?" link → `routerLink="/auth/forgot-password"` (stub route, will be built in Story 1.4)
  - [x] "Non hai un account? Registrati" link → `routerLink="/auth/register"`
  - [x] Component imports: `ReactiveFormsModule`, `MatFormFieldModule`, `MatInputModule`, `MatButtonModule`, `MatProgressSpinnerModule`, `RouterLink`

- [x] Task 11 — Update `auth.routes.ts` to use real `LoginComponent` (AC: 1, 2)
  - [x] Edit `apps/frontend/src/app/features/auth/auth.routes.ts`
  - [x] Replace `{ path: 'login', component: RegisterComponent }` stub with `{ path: 'login', loadComponent: () => import('./login/login.component').then(m => m.LoginComponent) }`
  - [x] Add stub route for forgot-password: `{ path: 'forgot-password', loadComponent: () => import('./register/register.component').then(m => m.RegisterComponent) }` — temporary stub, will be replaced in Story 1.4

- [x] Task 12 — Create `DashboardComponent` stub (AC: 1)
  - [x] Create `apps/frontend/src/app/features/dashboard/dashboard.component.ts`
  - [x] Selector: `fc-dashboard`; `ChangeDetectionStrategy.OnPush`; displays "Dashboard (prossimamente)" placeholder
  - [x] Update `apps/frontend/src/app/features/dashboard/dashboard.routes.ts` to define the default route: `{ path: '', component: DashboardComponent }`
  - [x] This is the navigation target after successful login; `DASHBOARD_ROUTES` is currently an empty array — if left empty, navigating to `/dashboard` renders nothing (no crash, but blank screen); a stub component solves this for Story 1.3

## Dev Notes

### Critical Security Rules (Non-Negotiable)

- **Anti-enumeration in login responses:** The HTTP 401 message MUST be identical whether the email does not exist OR the password is wrong. Never say "User not found" or "Wrong password" — always "Invalid credentials". This prevents username/account discovery via login probing (OWASP A07 Identification and Authentication Failures).
- **Timing attack prevention:** When a user is not found, still run `passwordEncoder.matches(request.password, DUMMY_HASH)` to ensure consistent response time. Skipping password hashing for non-existent users creates a measurable timing difference that can confirm account existence.
- **No sensitive data in logs:** Do NOT log the email or password in `AuthService.login()`. The existing `log.info("Registration attempt for email domain: {}", ...)` pattern (domain only) is acceptable for register; for login, log nothing about the request content — only a generic "auth attempt".
- **`tenant_id` never from request:** Login request body contains only `email` and `password`. The `tenantId` is extracted from the found user record. Never accept `tenantId` from the request.
- **Refresh token cookie:** Identical settings to register — HttpOnly, Secure, path=/api/v1/auth/refresh, maxAge=30days, SameSite=Strict.

### Backend — What to Reuse (DO NOT Re-Implement)

| Component | Location | Reuse Pattern |
|----------|----------|---------------|
| `JwtService.generateAccessToken()` | `com.foodcost.auth.service.JwtService` | Call directly — already handles `tenantId`, `roles`, `plan` claims |
| `JwtService.generateRefreshToken()` | `com.foodcost.auth.service.JwtService` | Returns raw UUID string — same as used in `register()` |
| `sha256Hex()` | `AuthService` private method | Already exists — reuse for hashing refresh token before DB save |
| `RefreshToken` entity + repository | `com.foodcost.auth.entity/repository` | Same pattern as `register()` — save new RefreshToken on login |
| Cookie construction | `AuthController.register()` | Copy cookie-setting block EXACTLY — same params |
| RFC 7807 `ProblemDetail` | Spring 6.x native | Already used in `GlobalExceptionHandler` — follow existing pattern |
| `Argon2PasswordEncoder` bean | `com.foodcost.config.PasswordEncoderConfig` | Already `@Bean` — inject into `AuthService` as `passwordEncoder` |
| `UserRepository.findByEmail()` | `com.foodcost.auth.repository.UserRepository` | Returns `User?` (Kotlin nullable) |

### Backend — Package Structure (new files only)

```
apps/backend/src/main/kotlin/com/foodcost/
├── auth/
│   ├── AuthController.kt              ← EDIT: add login() POST handler
│   ├── dto/
│   │   └── LoginRequest.kt            ← NEW: { email: String, password: String }
│   ├── filter/
│   │   └── RateLimitFilter.kt         ← EDIT: add Retry-After header to 429
│   └── service/
│       ├── AuthService.kt             ← EDIT: add login() method
│       └── InvalidCredentialsException.kt  ← NEW: RuntimeException subclass
└── config/
    └── GlobalExceptionHandler.kt      ← EDIT: add InvalidCredentialsException handler
```

**No new Flyway migration needed** — all needed tables (`users`, `refresh_tokens`) were created in V2 (Story 1.2).

**SecurityConfig is correct as-is** — `/api/v1/auth/login` is already in the `permitAll()` list.

### Backend — AuthService Login Implementation Pattern

```kotlin
// AuthService.kt — new method to add
companion object {
    // Precomputed hash for timing-attack prevention when user not found
    // Same Argon2id parameters as production: memoryCost=65536, iterations=3, parallelism=4
    private val DUMMY_HASH = Argon2PasswordEncoder(16, 32, 4, 65536, 3)
        .encode("timing-attack-prevention-placeholder")
}

@Transactional
fun login(request: LoginRequest): Pair<AuthResponse, String> {
    val user = userRepository.findByEmail(request.email)

    // Anti-timing: always run expensive hash comparison, even if user doesn't exist
    val hashToCheck = user?.passwordHash ?: DUMMY_HASH
    val passwordMatches = passwordEncoder.matches(request.password, hashToCheck)

    if (user == null || !passwordMatches) {
        throw InvalidCredentialsException()
    }

    val rawRefreshToken = jwtService.generateRefreshToken()
    val tokenHash = sha256Hex(rawRefreshToken)
    refreshTokenRepository.save(
        RefreshToken(
            userId = user.id!!,
            tokenHash = tokenHash,
            expiresAt = Instant.now().plusSeconds(30L * 24 * 60 * 60),
        )
    )

    val accessToken = jwtService.generateAccessToken(user)
    return AuthResponse(accessToken = accessToken, user = UserDto.from(user)) to rawRefreshToken
}
```

### Backend — RateLimitFilter `Retry-After` Header (Edit)

```kotlin
// In RateLimitFilter.doFilterInternal, replace the 429 block:
if (entry.count.get() > maxRequests) {
    val retryAfterSeconds = maxOf(0L, windowSeconds - (now - entry.windowStart) / 1000)
    response.status = 429
    response.contentType = "application/problem+json"
    response.setHeader("Retry-After", retryAfterSeconds.toString())  // ← ADD THIS
    response.writer.write(
        """{"type":"https://foodcost.app/errors/rate-limit","title":"Too Many Requests","status":429,"detail":"Rate limit exceeded. Try again later."}""",
    )
    return
}
```

### Backend — GlobalExceptionHandler Addition

```kotlin
// Add to GlobalExceptionHandler.kt (import InvalidCredentialsException)
@ExceptionHandler(InvalidCredentialsException::class)
@ResponseStatus(HttpStatus.UNAUTHORIZED)
fun handleInvalidCredentials(e: InvalidCredentialsException): ProblemDetail =
    ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid credentials").also {
        it.type = URI.create("https://foodcost.app/errors/invalid-credentials")
        it.title = "Unauthorized"
    }
```

### Backend — Test Strategy (Spring Boot 4.x caveats)

From Story 1.2 dev notes: **`@WebMvcTest` has issues with this Spring Boot version** — Use `@SpringBootTest` with `MockMvc` for all controller integration tests.

```kotlin
// Unit test pattern (MockK — NOT Mockito):
@ExtendWith(MockKExtension::class)
class AuthServiceTest {
    @MockK lateinit var userRepository: UserRepository
    @MockK lateinit var passwordEncoder: Argon2PasswordEncoder
    // ...
    @Test
    fun `login with wrong password throws InvalidCredentialsException`() {
        val mockUser = User(tenantId = UUID.randomUUID(), email = "chef@example.com", passwordHash = "hash")
        every { userRepository.findByEmail("chef@example.com") } returns mockUser
        every { passwordEncoder.matches("wrongpass", "hash") } returns false
        assertThrows<InvalidCredentialsException> {
            authService.login(LoginRequest("chef@example.com", "wrongpass"))
        }
    }
}
```

For integration tests, use `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)` with a `TestContainers` approach or H2 in-memory database (H2 is already a `testRuntimeOnly` dependency). Note: H2 may not support all PostgreSQL-specific SQL in Flyway migrations — if H2 tests fail due to `gen_random_uuid()` or other PG-specific SQL, use `@SQL` annotations to set up test fixtures directly instead of relying on Flyway SQL.

**Mockito dependency**: `mockito-kotlin:5.4.0` is in the build — however, use `io.mockk:mockk:1.14.0` for Kotlin-idiomatic mocking (same pattern established in Story 1.2).

### Frontend — Angular Conventions (Non-Negotiable)

- `ChangeDetectionStrategy.OnPush` on ALL components — mandatory architecture rule
- Do NOT add `standalone: true` to `@Component` decorator — it is the default in Angular 21 and adding it is redundant
- Use `input()` signal for `@Input()`, `output()` for `@Output()` — but `LoginComponent` has no inputs/outputs
- Use `inject()` function OR constructor injection — follow the pattern from `RegisterComponent` (constructor injection used there)
- Component selector: `fc-login` (fc- prefix mandatory)
- Template file: `login.component.html` (separate file — not inline template)
- The `FormBuilder.nonNullable` pattern should be used: `this.fb.nonNullable.group({...})`

### Frontend — LoginComponent Key Pattern

```typescript
// apps/frontend/src/app/features/auth/login/login.component.ts
@Component({
  selector: 'fc-login',
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatProgressSpinnerModule, RouterLink],
})
export class LoginComponent {
  protected readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required]],
    password: ['', [Validators.required]],
  });
  protected readonly isSubmitting = signal(false);
  protected readonly serverError = signal<string | null>(null);

  private readonly returnUrl: string;

  constructor(
    private readonly fb: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly tokenService: TokenService,
  ) {
    this.returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') ?? '/dashboard';
  }

  submit(): void {
    if (this.form.invalid || this.isSubmitting()) return;
    this.isSubmitting.set(true);
    this.serverError.set(null);
    const { email, password } = this.form.getRawValue();
    this.authService.login(email, password).subscribe({
      next: (response) => {
        this.tokenService.setToken(response.accessToken);
        this.router.navigateByUrl(this.returnUrl);
      },
      error: (err) => {
        this.isSubmitting.set(false);
        // Always show generic message — never reveal which field was wrong
        this.serverError.set('Credenziali non valide. Controlla email e password.');
      },
    });
  }
}
```

**Security note:** Ignore `err?.error?.detail` from the server for the auth error case. Always show the local generic Italian message to prevent leaking the server-side enumeration-safe message through the UI. The `serverError` signal is always set to the same Italian string on 401.

### Frontend — auth.routes.ts Final State

```typescript
import { Routes } from '@angular/router';
import { RegisterComponent } from './register/register.component';

export const AUTH_ROUTES: Routes = [
  { path: 'register', component: RegisterComponent },
  { path: 'login', loadComponent: () => import('./login/login.component').then(m => m.LoginComponent) },
  { path: 'forgot-password', component: RegisterComponent }, // stub — replaced in Story 1.4
  { path: '', redirectTo: 'register', pathMatch: 'full' },
];
```

### Frontend — Dashboard Stub

```typescript
// apps/frontend/src/app/features/dashboard/dashboard.component.ts
@Component({
  selector: 'fc-dashboard',
  template: `<div class="p-4 text-white">Dashboard (prossimamente)</div>`,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardComponent {}
```

```typescript
// apps/frontend/src/app/features/dashboard/dashboard.routes.ts
import { Routes } from '@angular/router';
import { DashboardComponent } from './dashboard.component';

export const DASHBOARD_ROUTES: Routes = [
  { path: '', component: DashboardComponent },
];
```

### Frontend — ReturnUrl Security

The `returnUrl` from query params is a potential open redirect vulnerability. Validate it before use:
- Only redirect to relative URLs (starting with `/`)
- If `returnUrl` starts with `http://` or `https://` or `//`, ignore it and redirect to `/dashboard` instead

```typescript
// Safe returnUrl extraction
const raw = this.route.snapshot.queryParamMap.get('returnUrl') ?? '/dashboard';
this.returnUrl = raw.startsWith('/') && !raw.startsWith('//') ? raw : '/dashboard';
```

### JWT Payload Verification

The `plan` and `tenantId` claims in the JWT are set in `JwtService.generateAccessToken()`:
- `tenantId`: comes from `user.tenantId` (UUID from DB — correct)
- `plan`: hardcoded to `"base"` for now — **DO NOT change this in Story 1.3** (Story 1.5 will make it dynamic from the tenant record)

Both claims ARE present in the JWT. AC1 is already satisfied by the existing `JwtService` implementation. The login endpoint just needs to call `jwtService.generateAccessToken(user)` and the claims are in the token.

### Project Structure Notes

- **Package alignment:** All new backend classes follow `com.foodcost.{feature}` pattern — `LoginRequest` goes in `com.foodcost.auth.dto`, `InvalidCredentialsException` in `com.foodcost.auth.service`
- **Frontend alignment:** `LoginComponent` in `features/auth/login/` — mirrors `features/auth/register/` structure
- **No new Flyway migration** — V1 (init) and V2 (auth schema) are complete and sufficient for Story 1.3
- **RateLimitFilter scope:** The filter applies to ALL `/api/v1/auth/` endpoints (register + login). This is intentional for MVP. For Story 1.5 (subscriptions), consider splitting rate limit windows per endpoint if needed
- **`DashboardComponent`** is a stub — its selector `fc-dashboard` follows the `fc-` prefix rule. The actual dashboard content is delivered in Epic 5/6

### References

- User story requirements: [planning-artifacts/epics.md - Story 1.3](../_bmad-output/planning-artifacts/epics.md)
- Architecture — JWT strategy: [planning-artifacts/architecture.md - Categoria 2](../_bmad-output/planning-artifacts/architecture.md)
- Architecture — API design (RFC 7807): [planning-artifacts/architecture.md - Categoria 3](../_bmad-output/planning-artifacts/architecture.md)
- Architecture — Rate limiting: [planning-artifacts/architecture.md - Rate Limiting section](../_bmad-output/planning-artifacts/architecture.md)
- Previous story dev notes: [implementation-artifacts/1-2-user-registration.md - Dev Notes](./1-2-user-registration.md)
- Existing `RateLimitFilter`: [apps/backend/.../auth/filter/RateLimitFilter.kt](../../apps/backend/src/main/kotlin/com/foodcost/auth/filter/RateLimitFilter.kt)
- Existing `JwtService`: [apps/backend/.../auth/service/JwtService.kt](../../apps/backend/src/main/kotlin/com/foodcost/auth/service/JwtService.kt)
- Existing `AuthService`: [apps/backend/.../auth/service/AuthService.kt](../../apps/backend/src/main/kotlin/com/foodcost/auth/service/AuthService.kt)
- Existing `GlobalExceptionHandler`: [apps/backend/.../config/GlobalExceptionHandler.kt](../../apps/backend/src/main/kotlin/com/foodcost/config/GlobalExceptionHandler.kt)

## Dev Agent Record

### Agent Model Used

claude-sonnet-4-5 (via GitHub Copilot)

### Senior Developer Review (AI)

**Review Date:** 2026-03-18
**Reviewer Model:** Claude Opus 4.6 (via GitHub Copilot)
**Review Outcome:** Changes Requested → All Fixed

#### Action Items

- [x] [HIGH] No integration/unit test for AC4 (rate-limit 429 + Retry-After header) — created `RateLimitFilterTest.kt` with 4 tests
- [x] [MEDIUM] Timing-attack prevention not verified in `login_withNonExistentEmail` test — added `verify(exactly = 1) { passwordEncoder.matches(any(), any()) }`
- [x] [MEDIUM] Cookie construction duplicated in `AuthController.register()` and `login()` — extracted `addRefreshTokenCookie()` private method
- [x] [MEDIUM] RateLimitFilter shared state could leak between integration tests — mitigated by using isolated unit tests for rate-limit verification
- [x] [LOW] No logging on login endpoint — added `log.info("POST /api/v1/auth/login called")`
- [ ] [LOW] RateLimitFilter never purges expired IP entries (memory leak) — pre-existing from Story 1.2, tracked for future cleanup

### Debug Log References

- Fixed `DUMMY_HASH` type mismatch: `Argon2PasswordEncoder.encode()` returns `String?` in Kotlin — added `!!` non-null assertion since the encoder always returns a value for valid input.

### Completion Notes List

- **Task 1:** Created `LoginRequest.kt` DTO with `@NotBlank` only (no `@Email`) for anti-enumeration protection.
- **Task 2:** Created `InvalidCredentialsException` — simple `RuntimeException` subclass with generic message.
- **Task 3:** Added `login()` to `AuthService` with timing-attack prevention via `DUMMY_HASH`. Uses same `sha256Hex()` and refresh token pattern as `register()`.
- **Task 4:** Added `POST /api/v1/auth/login` endpoint to `AuthController`. Returns HTTP 200 (not 201). Cookie settings identical to `register()`.
- **Task 5:** Added `InvalidCredentialsException` handler to `GlobalExceptionHandler` — returns RFC 7807 ProblemDetail with generic "Invalid credentials" message.
- **Task 6:** Added `Retry-After` header to `RateLimitFilter` 429 response, calculated from window start time.
- **Task 7:** Made rate-limit properties explicit in `application.properties` (were previously only `@Value` defaults).
- **Task 8:** Added 3 unit tests to `AuthServiceTest` (valid login, non-existent email, wrong password) and 4 integration tests to `AuthControllerIntegrationTest` (200 success + cookie, 401 wrong password, 401 non-existent email same message, 400 blank email). All tests pass 100%.
- **Task 9:** Added `login()` method to Angular `AuthService` — same pattern as `register()`.
- **Task 10:** Created `LoginComponent` with `OnPush`, `ReactiveFormsModule`, signal-based state, `returnUrl` with open-redirect protection, and generic Italian error message for anti-enumeration.
- **Task 11:** Updated `auth.routes.ts` — replaced login stub with lazy-loaded `LoginComponent`, added `forgot-password` stub route.
- **Task 12:** Created `DashboardComponent` stub and updated `dashboard.routes.ts` with default route.

### File List

**New files:**
- `apps/backend/src/main/kotlin/com/foodcost/auth/dto/LoginRequest.kt`
- `apps/backend/src/main/kotlin/com/foodcost/auth/service/InvalidCredentialsException.kt`
- `apps/backend/src/test/kotlin/com/foodcost/auth/RateLimitFilterTest.kt`
- `apps/frontend/src/app/features/auth/login/login.component.ts`
- `apps/frontend/src/app/features/auth/login/login.component.html`
- `apps/frontend/src/app/features/auth/login/login.component.scss`
- `apps/frontend/src/app/features/dashboard/dashboard.component.ts`

**Modified files:**
- `apps/backend/src/main/kotlin/com/foodcost/auth/service/AuthService.kt`
- `apps/backend/src/main/kotlin/com/foodcost/auth/AuthController.kt`
- `apps/backend/src/main/kotlin/com/foodcost/config/GlobalExceptionHandler.kt`
- `apps/backend/src/main/kotlin/com/foodcost/auth/filter/RateLimitFilter.kt`
- `apps/backend/src/main/resources/application.properties`
- `apps/backend/src/test/kotlin/com/foodcost/auth/AuthServiceTest.kt`
- `apps/backend/src/test/kotlin/com/foodcost/auth/AuthControllerIntegrationTest.kt`
- `apps/frontend/src/app/features/auth/auth.service.ts`
- `apps/frontend/src/app/features/auth/auth.routes.ts`
- `apps/frontend/src/app/features/dashboard/dashboard.routes.ts`

## Change Log

- 2026-03-18: Implemented Story 1.3 User Login — all 12 tasks completed (backend login endpoint, anti-enumeration security, Retry-After header, frontend LoginComponent with returnUrl support, DashboardComponent stub). All tests pass (100% success rate).
- 2026-03-18: Code review fixes — added RateLimitFilterTest (4 tests for AC4 coverage), added verify for timing-attack prevention test, extracted addRefreshTokenCookie() to eliminate duplication, added login endpoint logging. 5 issues fixed, 1 LOW deferred (pre-existing memory leak in RateLimitFilter).
