# Story 2.1: Add Ingredient to Warehouse

Status: review

## Story

As a **restaurant owner**,
I want to add a new ingredient to my warehouse with its name, unit of measure, and purchase price,
so that I can start building my digital inventory to calculate recipe costs.

## Acceptance Criteria

1. **Given** I am on the Magazzino screen, **when** I tap "Aggiungi ingrediente" and submit a valid name, unit of measure (from the grouped selector: Peso/Volume/Unità), and price per unit, **then** the ingredient is saved with `tenant_id` injected server-side from the JWT (never from the request body); the API returns HTTP 201; and the new ingredient appears at the top of the warehouse list.

2. **Given** the price field, **when** I enter a value, **then** it uses `inputmode="decimal"` on mobile; negative or zero values are rejected client-side with inline `mat-error`: "Inserisci un prezzo valido (es. 4.50)".

3. **Given** I submit a duplicate ingredient name within my tenant, **when** the API processes the request, **then** it returns HTTP 422 with RFC 7807 Problem Detail: `"detail": "An ingredient with this name already exists in your warehouse"`.

4. **Given** the warehouse has no ingredients, **when** I open the Magazzino screen, **then** an empty state is shown: "Nessun ingrediente ancora. Aggiungi il tuo primo ingrediente per iniziare a calcolare i costi." with an "Aggiungi ingrediente" secondary CTA button.

## Tasks / Subtasks

### Backend

- [x] Task 1 — Create Flyway migration `V3__create_ingredients.sql` (AC: 1, 3)
  - [x] Create `apps/backend/src/main/resources/db/migration/V3__create_ingredients.sql`
  - [x] Table `ingredients`:
    ```sql
    CREATE TABLE ingredients (
        id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
        tenant_id   UUID         NOT NULL REFERENCES tenants(id),
        name        VARCHAR(255) NOT NULL,
        unit        VARCHAR(50)  NOT NULL,
        price       NUMERIC(12,4) NOT NULL,
        created_at  TIMESTAMP    NOT NULL DEFAULT now(),
        updated_at  TIMESTAMP    NOT NULL DEFAULT now(),
        CONSTRAINT uq_ingredients_tenant_name UNIQUE (tenant_id, name)
    );
    CREATE INDEX idx_ingredients_tenant_id ON ingredients(tenant_id);
    ```
  - [x] `price` is `NUMERIC(12,4)` — supports up to 99,999,999.9999 with 4 decimal places for sub-cent precision on unit prices (e.g., saffron per gram = €0.0350)
  - [x] `unit` stores the raw unit string (e.g., "kg", "g", "l", "ml", "pz") — validated by backend DTO, not DB constraint
  - [x] `uq_ingredients_tenant_name` constraint enforces unique ingredient names per tenant (AC3)
  - [x] **No RLS policy in this migration** — RLS will be added in a dedicated migration (V6 per architecture plan). Tenant isolation is enforced by application-level filtering via `tenant_id` from JWT/SecurityContext

- [x] Task 2 — Create `Ingredient` JPA entity (AC: 1)
  - [x] Create `apps/backend/src/main/kotlin/com/foodcost/ingredient/entity/Ingredient.kt`
  - [x] Package: `com.foodcost.ingredient.entity`
  - [x] Fields: `id: UUID?`, `tenantId: UUID`, `name: String`, `unit: String`, `price: BigDecimal`, `createdAt: Instant`, `updatedAt: Instant`
  - [x] `@Table(name = "ingredients")`
  - [x] `@Column(name = "tenant_id", nullable = false)` — mapped from JWT, NEVER from request body
  - [x] `@Column(precision = 12, scale = 4)` on `price`
  - [x] Pattern: follow `User.kt` entity pattern (same `@Id @GeneratedValue(strategy = GenerationType.UUID)`, same `Instant` timestamps)

- [x] Task 3 — Create `IngredientRepository` (AC: 1, 3)
  - [x] Create `apps/backend/src/main/kotlin/com/foodcost/ingredient/repository/IngredientRepository.kt`
  - [x] Package: `com.foodcost.ingredient.repository`
  - [x] `interface IngredientRepository : JpaRepository<Ingredient, UUID>`
  - [ ] Methods:
    - `fun findByTenantIdOrderByCreatedAtDesc(tenantId: UUID): List<Ingredient>` — returns all ingredients for a tenant, newest first (AC1: "new ingredient appears at the top")
    - `fun existsByTenantIdAndNameIgnoreCase(tenantId: UUID, name: String): Boolean` — case-insensitive duplicate check (AC3)

- [x] Task 4 — Create DTOs: `IngredientCreateRequest` and `IngredientDto` (AC: 1, 2, 3)
  - [x] Create `apps/backend/src/main/kotlin/com/foodcost/ingredient/dto/IngredientCreateRequest.kt`
    ```kotlin
    data class IngredientCreateRequest(
        @field:NotBlank val name: String,
        @field:NotBlank val unit: String,  // validated: must be in allowed set
        @field:NotNull @field:DecimalMin(value = "0.0001", inclusive = true) val price: BigDecimal,
    )
    ```
  - [x] **NO `tenantId` field** — injected server-side from JWT
  - [x] `price` validation: `@DecimalMin("0.0001")` — rejects zero and negative (AC2)
  - [x] `unit` must be one of: `kg`, `g`, `hg`, `l`, `cl`, `ml`, `pz`, `confezione`, `porzione` — validate in service layer, not DTO annotation (to return RFC 7807 error, not 400 validation error)
  - [x] Create `apps/backend/src/main/kotlin/com/foodcost/ingredient/dto/IngredientDto.kt`
    ```kotlin
    data class IngredientDto(
        val id: UUID,
        val name: String,
        val unit: String,
        val price: BigDecimal,
        val createdAt: Instant,
        val updatedAt: Instant,
    ) {
        companion object {
            fun from(ingredient: Ingredient) = IngredientDto(
                id = ingredient.id!!,
                name = ingredient.name,
                unit = ingredient.unit,
                price = ingredient.price,
                createdAt = ingredient.createdAt,
                updatedAt = ingredient.updatedAt,
            )
        }
    }
    ```
  - [x] **NO `tenantId` in IngredientDto** — never expose tenant_id in API responses

- [x] Task 5 — Create `IngredientService` (AC: 1, 3)
  - [x] Create `apps/backend/src/main/kotlin/com/foodcost/ingredient/service/IngredientService.kt`
  - [x] Package: `com.foodcost.ingredient.service`
  - [x] Method `create(request: IngredientCreateRequest, tenantId: UUID): IngredientDto`
    - Validate unit is in allowed set → throw `InvalidUnitException` if not
    - Check `existsByTenantIdAndNameIgnoreCase(tenantId, request.name)` → throw `DuplicateIngredientException` if true (AC3)
    - Save `Ingredient(tenantId = tenantId, name = request.name.trim(), unit = request.unit, price = request.price)`
    - Return `IngredientDto.from(saved)`
  - [x] Method `findAll(tenantId: UUID): List<IngredientDto>`
    - `ingredientRepository.findByTenantIdOrderByCreatedAtDesc(tenantId).map { IngredientDto.from(it) }`
  - [x] `@Transactional` on `create()` method
  - [x] **DO NOT log** ingredient names or prices (GDPR — architecture rule)

- [x] Task 6 — Create exception classes (AC: 3)
  - [x] Create `apps/backend/src/main/kotlin/com/foodcost/ingredient/service/DuplicateIngredientException.kt`
    - `class DuplicateIngredientException : RuntimeException("Duplicate ingredient name")`
  - [x] Create `apps/backend/src/main/kotlin/com/foodcost/ingredient/service/InvalidUnitException.kt`
    - `class InvalidUnitException(unit: String) : RuntimeException("Invalid unit: $unit")`

- [x] Task 7 — Add exception handlers to `GlobalExceptionHandler` (AC: 3)
  - [x] Edit `apps/backend/src/main/kotlin/com/foodcost/config/GlobalExceptionHandler.kt`
  - [x] Add handler for `DuplicateIngredientException`:
    ```kotlin
    @ExceptionHandler(DuplicateIngredientException::class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    fun handleDuplicateIngredient(e: DuplicateIngredientException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "An ingredient with this name already exists in your warehouse",
        ).also {
            it.type = URI.create("https://foodcost.app/errors/duplicate-ingredient")
            it.title = "Unprocessable Entity"
        }
    ```
  - [x] Add handler for `InvalidUnitException`:
    ```kotlin
    @ExceptionHandler(InvalidUnitException::class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    fun handleInvalidUnit(e: InvalidUnitException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.UNPROCESSABLE_ENTITY,
            e.message ?: "Invalid unit of measure",
        ).also {
            it.type = URI.create("https://foodcost.app/errors/invalid-unit")
            it.title = "Unprocessable Entity"
        }
    ```

- [x] Task 8 — Create `IngredientController` (AC: 1, 3)
  - [x] Create `apps/backend/src/main/kotlin/com/foodcost/ingredient/IngredientController.kt`
  - [x] Package: `com.foodcost.ingredient`
  - [x] `@RestController @RequestMapping("/api/v1/ingredients")`
  - [x] Endpoint: `POST /api/v1/ingredients`
    ```kotlin
    @PostMapping
    fun create(
        @Valid @RequestBody request: IngredientCreateRequest,
        authentication: Authentication,
    ): ResponseEntity<IngredientDto> {
        val tenantId = extractTenantId(authentication)
        val created = ingredientService.create(request, tenantId)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }
    ```
  - [x] Endpoint: `GET /api/v1/ingredients`
    ```kotlin
    @GetMapping
    fun list(authentication: Authentication): List<IngredientDto> {
        val tenantId = extractTenantId(authentication)
        return ingredientService.findAll(tenantId)
    }
    ```
  - [x] Helper: `extractTenantId(authentication)` — extracts `tenantId` from the JWT claims stored in `authentication.details` (set by `JwtAuthenticationFilter`)
    ```kotlin
    private fun extractTenantId(authentication: Authentication): UUID {
        val details = authentication.details as? Map<*, *>
            ?: throw IllegalStateException("Missing authentication details")
        val tenantId = details["tenantId"] as? String
            ?: throw IllegalStateException("Missing tenantId in JWT")
        return UUID.fromString(tenantId)
    }
    ```
  - [x] **CRITICAL:** `tenantId` comes from JWT via `authentication.details["tenantId"]` — set by `JwtAuthenticationFilter` (line 35: `details = mapOf("tenantId" to claims["tenantId"])`)
  - [x] Both endpoints are under `/api/v1/ingredients` which requires authentication (SecurityConfig: `anyRequest().authenticated()`) — no additional guards needed for this story

- [x] Task 9 — Backend unit tests (AC: 1, 3)
  - [x] Create `apps/backend/src/test/kotlin/com/foodcost/ingredient/IngredientServiceTest.kt`
  - [x] Use **MockK** (NOT Mockito) — same pattern as `AuthServiceTest`
  - [x] Tests:
    - `create_withValidRequest_savesAndReturnsDto()` — verify repository.save called with correct tenantId, name trimmed, unit, price
    - `create_withDuplicateName_throwsDuplicateIngredientException()` — verify existsByTenantIdAndNameIgnoreCase returns true → exception
    - `create_withInvalidUnit_throwsInvalidUnitException()` — verify invalid unit string triggers exception
    - `findAll_returnsSortedIngredients()` — verify returns list mapped to DTOs

- [x] Task 10 — Backend integration tests (AC: 1, 2, 3)
  - [x] Create `apps/backend/src/test/kotlin/com/foodcost/ingredient/IngredientControllerIntegrationTest.kt`
  - [x] Use `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `MockMvc` (same pattern as `AuthControllerIntegrationTest`)
  - [x] **IMPORTANT:** Need a valid JWT token for authenticated requests. Follow the pattern from Story 1.3 — either: (a) create a test utility that generates a JWT with tenantId claim, or (b) register a user in beforeEach and use the returned token
  - [x] Tests:
    - `POST /api/v1/ingredients` with valid body + valid JWT → 201 + IngredientDto with id, name, unit, price
    - `POST /api/v1/ingredients` with duplicate name same tenant → 422 + RFC 7807 detail
    - `POST /api/v1/ingredients` with negative price → 400 (validation fails)
    - `POST /api/v1/ingredients` without JWT → 401
    - `GET /api/v1/ingredients` with valid JWT → 200 + list of ingredients for that tenant only

### Frontend

- [x] Task 11 — Create `Ingredient` TypeScript model (AC: 1)
  - [x] Create `apps/frontend/src/app/shared/models/ingredient.model.ts`
  - [x] Interface:
    ```typescript
    export interface Ingredient {
      id: string;
      name: string;
      unit: string;
      price: number;
      createdAt: string;
      updatedAt: string;
    }

    export interface IngredientCreateRequest {
      name: string;
      unit: string;
      price: number;
    }
    ```
  - [x] **NO `tenantId`** in either interface — never exposed to frontend

- [x] Task 12 — Create `IngredientService` (Angular) (AC: 1)
  - [x] Create `apps/frontend/src/app/features/warehouse/ingredient.service.ts`
  - [x] `@Injectable({ providedIn: 'root' })`
  - [x] Inject `HttpClient` via `inject()` function
  - [x] Methods:
    - `getAll(): Observable<Ingredient[]>` → `GET /api/v1/ingredients`
    - `create(request: IngredientCreateRequest): Observable<Ingredient>` → `POST /api/v1/ingredients`
  - [x] The `jwt.interceptor.ts` (already exists in `core/auth/`) automatically adds the `Authorization: Bearer` header — no need to handle auth here

- [x] Task 13 — Create `IngredientListComponent` (AC: 1, 4)
  - [x] Create `apps/frontend/src/app/features/warehouse/ingredient-list/ingredient-list.component.ts` (+ `.html`, `.scss`)
  - [x] Selector: `fc-ingredient-list`
  - [x] `ChangeDetectionStrategy.OnPush` (MANDATORY)
  - [x] Use `rxResource` for data loading (architecture pattern):
    ```typescript
    protected readonly ingredients = rxResource({
      loader: () => this.ingredientService.getAll()
    });
    ```
  - [ ] Template pattern (3-state: loading / error / data):
    ```html
    @if (ingredients.isLoading()) {
      <!-- 3 skeleton placeholder rows per UX spec -->
      <fc-skeleton />
      <fc-skeleton />
      <fc-skeleton />
    } @else if (ingredients.error()) {
      <fc-error-message [error]="ingredients.error()" />
    } @else if (ingredients.value()?.length === 0) {
      <!-- Empty state (AC4) -->
      <div class="empty-state">
        <p>Nessun ingrediente ancora. Aggiungi il tuo primo ingrediente per iniziare a calcolare i costi.</p>
        <button mat-stroked-button (click)="openAddForm()">Aggiungi ingrediente</button>
      </div>
    } @else {
      @for (ingredient of ingredients.value(); track ingredient.id) {
        <div class="ingredient-row">
          <span class="name">{{ ingredient.name }}</span>
          <span class="unit">{{ ingredient.unit }}</span>
          <span class="price tabular-nums">{{ ingredient.price | number:'1.2-4' }} €/{{ ingredient.unit }}</span>
        </div>
      }
    }
    ```
  - [x] "Aggiungi ingrediente" button (top of list or FAB on mobile) → triggers add form
  - [x] Prices use `tabular-nums` font styling (architecture: `font-variant-numeric: tabular-nums`)
  - [x] After successful add: reload ingredient list to show new item at top

- [x] Task 14 — Create `IngredientFormComponent` (AC: 1, 2)
  - [x] Create `apps/frontend/src/app/features/warehouse/ingredient-form/ingredient-form.component.ts` (+ `.html`, `.scss`)
  - [x] Selector: `fc-ingredient-form`
  - [x] `ChangeDetectionStrategy.OnPush` (MANDATORY)
  - [x] Reactive form with typed controls:
    ```typescript
    protected readonly form = this.fb.nonNullable.group({
      name: ['', [Validators.required]],
      unit: ['kg', [Validators.required]],
      price: [null as number | null, [Validators.required, Validators.min(0.0001)]],
    });
    ```
  - [x] Unit selector: grouped `mat-select` with optgroups:
    - **Peso:** kg, g, hg
    - **Volume:** l, cl, ml
    - **Unità:** pz, confezione, porzione
  - [x] Price field: `inputmode="decimal"` on mobile (AC2)
  - [x] Price validation error message: `mat-error` "Inserisci un prezzo valido (es. 4.50)" — shown when value ≤ 0 or empty (AC2)
  - [x] Validation timing: **on submit only** (UX spec: "On submit only for multi-field forms")
  - [x] On submit: call `ingredientService.create()`, handle:
    - Success: `MatSnackBar` "Ingrediente aggiunto" (3s, neutral tone per UX), emit success event to parent
    - 422 duplicate: show server error detail in `mat-error` below the name field
    - Other errors: show generic `mat-error`
  - [x] Signals: `isSubmitting = signal(false)`, `serverError = signal<string | null>(null)`
  - [x] On mobile: form opens as `mat-bottom-sheet` (UX spec: "on mobile the edit form opens as a mat-bottom-sheet")
  - [x] On desktop: form opens inline or in a dialog
  - [x] Component imports: `ReactiveFormsModule`, `MatFormFieldModule`, `MatInputModule`, `MatSelectModule`, `MatButtonModule`, `MatProgressSpinnerModule`

- [x] Task 15 — Update `warehouse.routes.ts` (AC: 1)
  - [x] Edit `apps/frontend/src/app/features/warehouse/warehouse.routes.ts`
  - [x] Add route:
    ```typescript
    export const WAREHOUSE_ROUTES: Routes = [
      { path: '', component: IngredientListComponent },
    ];
    ```
  - [x] Import `IngredientListComponent`

## Dev Notes

### Critical Security Rules (Non-Negotiable)

- **`tenant_id` NEVER from request body:** The `IngredientCreateRequest` DTO must NOT contain a `tenantId` field. It is extracted server-side from the JWT token (`authentication.details["tenantId"]`). This prevents cross-tenant data injection (OWASP Broken Access Control).
- **`tenant_id` NEVER in API response:** `IngredientDto` must NOT expose `tenantId`. The client should never see this value.
- **No sensitive data in logs:** Do NOT log ingredient names, prices, or any tenant-specific data (GDPR — architecture enforcement rule #10).
- **Input validation on price:** Reject ≤ 0 on both client and server to prevent zero-cost ingredients corrupting food cost calculations.

### Backend — What to Reuse (DO NOT Re-Implement)

| Component | Location | Reuse Pattern |
|----------|----------|---------------|
| `JwtAuthenticationFilter` | `com.foodcost.auth.filter.JwtAuthenticationFilter` | Already extracts tenantId into `authentication.details` — just read it |
| `SecurityConfig` | `com.foodcost.config.SecurityConfig` | `anyRequest().authenticated()` already protects `/api/v1/ingredients` |
| `GlobalExceptionHandler` | `com.foodcost.config.GlobalExceptionHandler` | ADD new exception handlers — do NOT create a second handler class |
| `Argon2PasswordEncoder` bean | `com.foodcost.config.PasswordEncoderConfig` | Not needed for this story |
| RFC 7807 `ProblemDetail` | Spring 6.x native | Follow exact pattern from existing handlers |
| Flyway migrations | `src/main/resources/db/migration/` | Current: V1 (placeholder), V2 (auth schema). Next is V3 |

### Backend — Tenant ID Extraction Pattern

The `JwtAuthenticationFilter` (already implemented in Story 1.2) stores the tenantId in authentication details:
```kotlin
// JwtAuthenticationFilter.kt line 35:
val auth = UsernamePasswordAuthenticationToken(claims.subject, null, authorities).apply {
    details = mapOf("tenantId" to claims["tenantId"])
}
```

To extract in controllers:
```kotlin
private fun extractTenantId(authentication: Authentication): UUID {
    val details = authentication.details as? Map<*, *>
        ?: throw IllegalStateException("Missing authentication details")
    val tenantId = details["tenantId"] as? String
        ?: throw IllegalStateException("Missing tenantId in JWT")
    return UUID.fromString(tenantId)
}
```

**IMPORTANT:** The `tenantId` claim in the JWT is set by `JwtService.generateAccessToken()` which reads it from `user.tenantId`. It's always a UUID string.

### Backend — Package Structure (new files only)

```
apps/backend/src/main/kotlin/com/foodcost/
├── ingredient/
│   ├── IngredientController.kt         ← NEW
│   ├── dto/
│   │   ├── IngredientCreateRequest.kt  ← NEW
│   │   └── IngredientDto.kt            ← NEW
│   ├── entity/
│   │   └── Ingredient.kt              ← NEW
│   ├── repository/
│   │   └── IngredientRepository.kt    ← NEW
│   └── service/
│       ├── IngredientService.kt       ← NEW
│       ├── DuplicateIngredientException.kt ← NEW
│       └── InvalidUnitException.kt    ← NEW
└── config/
    └── GlobalExceptionHandler.kt      ← EDIT: add 2 new handlers
```

### Backend — Allowed Units (Validation Constant)

```kotlin
// In IngredientService or a companion object
val ALLOWED_UNITS = setOf("kg", "g", "hg", "l", "cl", "ml", "pz", "confezione", "porzione")
```

From UX spec: grouped selector — Peso (kg, g, hg), Volume (l, cl, ml), Unità (pz, confezione, porzione).

### Backend — Test Strategy

- **Unit tests:** MockK (NOT Mockito) for `IngredientService` — same pattern as `AuthServiceTest`
- **Integration tests:** `@SpringBootTest` + `MockMvc` (NOT `@WebMvcTest` — see Story 1.3 dev notes: `@WebMvcTest` has issues with this Spring Boot version)
- For integration tests requiring authenticated requests: register a user in `@BeforeEach`, extract the access token, and include it as `Authorization: Bearer {token}` header in test requests
- H2 may not support all PostgreSQL-specific SQL — if Flyway migration fails with H2 during tests, use `@Sql` annotations for test data setup

### Frontend — Angular Conventions (Non-Negotiable)

- `ChangeDetectionStrategy.OnPush` on ALL components — mandatory
- Do NOT add `standalone: true` to `@Component` — default in Angular 21
- Use `input()` signal for `@Input()`, `output()` for `@Output()`
- Use `inject()` function for DI in services; constructor injection is also acceptable (follow existing pattern)
- Component selector: `fc-` prefix (e.g., `fc-ingredient-list`)
- Template: separate `.html` file (not inline)
- Use native control flow (`@if`, `@for`, `@switch`) — never `*ngIf`, `*ngFor`
- Use `rxResource` for data fetching (architecture pattern) — NOT manual subscriptions with `BehaviorSubject`
- Use `mat-snack-bar` for success feedback (3s, neutral tone) — never modals for success
- Price display: `tabular-nums` CSS class on all numeric columns
- Prices formatted with Italian locale: `number:'1.2-4'` pipe (2-4 decimal places)

### Frontend — Package Structure (new files only)

```
apps/frontend/src/app/
├── shared/models/
│   └── ingredient.model.ts             ← NEW
└── features/warehouse/
    ├── warehouse.routes.ts             ← EDIT: add route
    ├── ingredient.service.ts           ← NEW
    ├── ingredient-list/
    │   ├── ingredient-list.component.ts    ← NEW
    │   ├── ingredient-list.component.html  ← NEW
    │   └── ingredient-list.component.scss  ← NEW
    └── ingredient-form/
        ├── ingredient-form.component.ts    ← NEW
        ├── ingredient-form.component.html  ← NEW
        └── ingredient-form.component.scss  ← NEW
```

### Frontend — Existing Components to Reuse

| Component | Location | Usage |
|-----------|----------|-------|
| `fc-skeleton` | `shared/components/fc-skeleton/` | Loading state for ingredient list (3 rows) |
| `fc-error-message` | `shared/components/fc-error-message/` | Error state for failed API calls |
| `jwt.interceptor.ts` | `core/auth/` | Automatically adds Bearer token — no manual auth needed |
| `authGuard` | `core/guards/auth.guard.ts` | Already applied on `/warehouse` route in `app.routes.ts` |

### Frontend — UX Requirements Summary

| Requirement | Source | Implementation |
|---|---|---|
| Empty state message + CTA | UX spec §Empty States | Centered message + "Aggiungi ingrediente" `mat-stroked-button` |
| Price `inputmode="decimal"` | UX spec + AC2 | `<input inputmode="decimal">` on price mat-form-field |
| Grouped unit selector | UX spec §Input Patterns | `mat-select` with `mat-optgroup` for Peso/Volume/Unità |
| 3 skeleton rows on load | UX spec §Loading States | `<fc-skeleton />` ×3 when `ingredients.isLoading()` |
| Mobile form as bottom-sheet | UX spec §Modal Patterns | `MatBottomSheet` for ingredient form on mobile (<768px) |
| Desktop form inline/dialog | UX spec §Modal Patterns | `MatDialog` or inline panel on desktop (≥768px) |
| Snackbar feedback (3s) | UX spec §Feedback Patterns | `MatSnackBar` "Ingrediente aggiunto" on success |
| `tabular-nums` on prices | UX spec §Typography | CSS class `tabular-nums` on price display elements |
| Dark theme styling | UX spec §Color System | All components use "Nero di Cucina" theme tokens |
| Form validation on submit | UX spec §Validation Timing | Validate only on submit, not on keydown |

### Previous Story Learnings (from Epic 1)

1. **`@WebMvcTest` doesn't work** with this Spring Boot version — always use `@SpringBootTest` + `MockMvc` for controller integration tests.
2. **Cookie extraction pattern** was established in Story 1.2/1.3 — not relevant here but the test utility pattern (register user → get token → use in tests) is.
3. **returnUrl security** was added in 1.3b — similar input validation mindset applies to ingredient name (trim whitespace, handle edge cases).
4. **RFC 7807 pattern** is well-established in `GlobalExceptionHandler` — follow exactly the same structure for new exception handlers.
5. **MockK** for Kotlin tests, NOT Mockito (explicit architectural rule).

### Git Recent Activity (last 5 commits)

```
ffd8917 manage routing
61d272e add pinoDeiPalazzi
756b6e6 feat(auth): implement user login functionality with JWT and error handling
f9508ff feat: enhance authentication and logging with debug information
d446d38 feat: complete user registration story
```

All recent work is in the `auth` feature. This story starts the `ingredient` feature — a new package/feature boundary. No overlap or regression risk with auth code.

### References

- [epics.md - Story 2.1](_bmad-output/planning-artifacts/epics.md) — acceptance criteria and user story
- [architecture.md - Naming Patterns Database](_bmad-output/planning-artifacts/architecture.md) — DB naming conventions
- [architecture.md - Naming Patterns API Endpoints](_bmad-output/planning-artifacts/architecture.md) — REST endpoint patterns
- [architecture.md - Process Patterns Tenant Context](_bmad-output/planning-artifacts/architecture.md) — tenant_id extraction from JWT
- [architecture.md - Implementation Patterns](_bmad-output/planning-artifacts/architecture.md) — enforcement guidelines
- [architecture.md - Frontend Developer Guidelines](_bmad-output/planning-artifacts/architecture.md) — Angular 21 conventions
- [ux-design-specification.md - Empty States](_bmad-output/planning-artifacts/ux-design-specification.md) — Magazzino empty state text
- [ux-design-specification.md - Input Patterns](_bmad-output/planning-artifacts/ux-design-specification.md) — unit selector, inputmode
- [ux-design-specification.md - Loading States](_bmad-output/planning-artifacts/ux-design-specification.md) — skeleton pattern
- [1-3-user-login.md - Dev Notes](_bmad-output/implementation-artifacts/1-3-user-login.md) — test patterns, Spring Boot caveats
- [JwtAuthenticationFilter.kt](apps/backend/src/main/kotlin/com/foodcost/auth/filter/JwtAuthenticationFilter.kt) — tenantId in auth details
- [SecurityConfig.kt](apps/backend/src/main/kotlin/com/foodcost/config/SecurityConfig.kt) — anyRequest().authenticated()
- [GlobalExceptionHandler.kt](apps/backend/src/main/kotlin/com/foodcost/config/GlobalExceptionHandler.kt) — existing RFC 7807 handlers

## Dev Agent Record

### Agent Model Used
Claude Opus 4.6 (GitHub Copilot)

### Debug Log References
- Angular 21 `rxResource` API uses `stream` instead of `loader` (changed from Angular 19/20 developer preview)
- `jwt.interceptor.ts` did not exist — created as prerequisite for authenticated API calls
- Shared components `fc-skeleton` and `fc-error-message` do not exist yet — implemented loading/error states with inline CSS skeleton animation and inline HTML respectively

### Completion Notes List
- Backend (Tasks 1-10) was already fully implemented from a previous session — all verified and tests passing
- Created JWT interceptor at `core/auth/jwt.interceptor.ts` and registered in `app.config.ts` — required for ingredient API calls
- Created `Ingredient` and `IngredientCreateRequest` TypeScript interfaces (no tenantId exposed)
- Created `IngredientService` Angular service using `inject(HttpClient)` pattern
- Created `IngredientListComponent` with `rxResource` for data loading, 3-state template (loading/error/empty/list), skeleton animation, tabular-nums pricing, empty state text per AC4
- Created `IngredientFormComponent` with grouped `mat-select` for units, `inputmode="decimal"` per AC2, submit-only validation, `MatSnackBar` success feedback, 422 duplicate error handling per AC3, mobile bottom-sheet / desktop dialog
- Updated `warehouse.routes.ts` to route `''` to `IngredientListComponent`
- Frontend build successful. Backend tests all passing (6/6 tasks up-to-date).

### File List
**New files:**
- `apps/frontend/src/app/core/auth/jwt.interceptor.ts`
- `apps/frontend/src/app/shared/models/ingredient.model.ts`
- `apps/frontend/src/app/features/warehouse/ingredient.service.ts`
- `apps/frontend/src/app/features/warehouse/ingredient-list/ingredient-list.component.ts`
- `apps/frontend/src/app/features/warehouse/ingredient-list/ingredient-list.component.html`
- `apps/frontend/src/app/features/warehouse/ingredient-list/ingredient-list.component.scss`
- `apps/frontend/src/app/features/warehouse/ingredient-form/ingredient-form.component.ts`
- `apps/frontend/src/app/features/warehouse/ingredient-form/ingredient-form.component.html`
- `apps/frontend/src/app/features/warehouse/ingredient-form/ingredient-form.component.scss`

**Modified files:**
- `apps/frontend/src/app/app.config.ts` (added JWT interceptor registration)
- `apps/frontend/src/app/features/warehouse/warehouse.routes.ts` (added IngredientListComponent route)

### Change Log
- 2026-03-24: Implemented frontend Tasks 11-15: TypeScript models, Angular IngredientService, IngredientListComponent with rxResource, IngredientFormComponent with grouped unit selector and mobile bottom-sheet, warehouse routes. Created JWT interceptor as missing prerequisite.
