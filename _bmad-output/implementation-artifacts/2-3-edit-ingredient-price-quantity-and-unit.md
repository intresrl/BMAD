# Story 2.3: Edit Ingredient Price, Quantity, and Unit

Status: ready-for-dev

## Story

As a **restaurant owner**,
I want to edit the price, quantity, and unit of measure of an existing ingredient,
so that my warehouse reflects the current real prices I am paying to suppliers.

## Acceptance Criteria

1. **Given** I am viewing an ingredient in my warehouse, **when** I update its price, quantity, or unit of measure and save, **then** the ingredient is updated immediately; the API returns HTTP 200 with the updated resource; and the change is visible in the list without a full page reload.

2. **Given** I am editing an ingredient, **when** I enter a negative or zero price, **then** validation prevents saving with inline `mat-error`: "Inserisci un prezzo valido (es. 4.50)".

3. **Given** I have saved an edit, **when** I check the ingredient record, **then** the `updated_at` timestamp is refreshed on the ingredient record.

4. **Given** I am on mobile (<768px), **when** I tap an ingredient to edit, **then** the edit form opens as a `mat-bottom-sheet`. On desktop (≥768px), it opens inline or in a `MatDialog`.

5. **Given** I change the ingredient name to a name already used by another ingredient in my tenant, **when** I save, **then** the API returns HTTP 422 with RFC 7807 Problem Detail: `"detail": "An ingredient with this name already exists in your warehouse"`.

## Tasks / Subtasks

### Backend

- [ ] Task 1 — Create `IngredientUpdateRequest` DTO (AC: 1, 2)
  - [ ] Create `apps/backend/src/main/kotlin/com/foodcost/ingredient/dto/IngredientUpdateRequest.kt`
  - [ ] Fields:
    ```kotlin
    data class IngredientUpdateRequest(
        @field:NotBlank val name: String,
        @field:NotBlank val unit: String,
        @field:NotNull @field:DecimalMin(value = "0.0001", inclusive = true) val price: BigDecimal,
    )
    ```
  - [ ] **NO `tenantId` or `id` field** — `id` from path param, `tenantId` from JWT
  - [ ] Same validation pattern as `IngredientCreateRequest`

- [ ] Task 2 — Create `IngredientNotFoundException` (AC: 1)
  - [ ] Create `apps/backend/src/main/kotlin/com/foodcost/ingredient/service/IngredientNotFoundException.kt`
    ```kotlin
    class IngredientNotFoundException : RuntimeException("Ingredient not found")
    ```

- [ ] Task 3 — Add `update` method to `IngredientService` (AC: 1, 2, 3, 5)
  - [ ] Edit `apps/backend/src/main/kotlin/com/foodcost/ingredient/service/IngredientService.kt`
  - [ ] New method:
    ```kotlin
    @Transactional
    fun update(id: UUID, request: IngredientUpdateRequest, tenantId: UUID): IngredientDto {
        if (request.unit !in ALLOWED_UNITS) {
            throw InvalidUnitException(request.unit)
        }

        val ingredient = ingredientRepository.findByIdAndTenantId(id, tenantId)
            ?: throw IngredientNotFoundException()

        val trimmedName = request.name.trim()

        // Check duplicate name only if name changed (case-insensitive)
        if (!ingredient.name.equals(trimmedName, ignoreCase = true)) {
            if (ingredientRepository.existsByTenantIdAndNameIgnoreCase(tenantId, trimmedName)) {
                throw DuplicateIngredientException()
            }
        }

        ingredient.name = trimmedName
        ingredient.unit = request.unit
        ingredient.price = request.price
        ingredient.updatedAt = Instant.now()

        return IngredientDto.from(ingredientRepository.save(ingredient))
    }
    ```
  - [ ] **CRITICAL:** Lookup uses BOTH `id` AND `tenantId` — prevents cross-tenant access (OWASP Broken Access Control)
  - [ ] Duplicate check skips if name hasn't changed (allows saving other fields without false duplicate error)
  - [ ] `updatedAt` is refreshed explicitly (AC3)

- [ ] Task 4 — Add `findByIdAndTenantId` to `IngredientRepository` (AC: 1)
  - [ ] Edit `apps/backend/src/main/kotlin/com/foodcost/ingredient/repository/IngredientRepository.kt`
  - [ ] Add method:
    ```kotlin
    fun findByIdAndTenantId(id: UUID, tenantId: UUID): Ingredient?
    ```

- [ ] Task 5 — Make `Ingredient` entity fields mutable for update (AC: 1, 3)
  - [ ] Edit `apps/backend/src/main/kotlin/com/foodcost/ingredient/entity/Ingredient.kt`
  - [ ] Change `val name` → `var name`, `val unit` → `var unit`, `val price` → `var price`
  - [ ] `updatedAt` is already `var` — no change needed
  - [ ] Keep `id`, `tenantId`, `createdAt` as `val` — these should never change

- [ ] Task 6 — Add `PUT /api/v1/ingredients/{id}` endpoint (AC: 1)
  - [ ] Edit `apps/backend/src/main/kotlin/com/foodcost/ingredient/IngredientController.kt`
  - [ ] Add endpoint:
    ```kotlin
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: IngredientUpdateRequest,
        authentication: Authentication,
    ): IngredientDto {
        val tenantId = extractTenantId(authentication)
        return ingredientService.update(id, request, tenantId)
    }
    ```
  - [ ] Returns HTTP 200 (default for `@PutMapping`) with updated `IngredientDto`
  - [ ] Architecture specifies `PUT` for full resource replacement (all fields required)

- [ ] Task 7 — Add `IngredientNotFoundException` handler to `GlobalExceptionHandler` (AC: 1)
  - [ ] Edit `apps/backend/src/main/kotlin/com/foodcost/config/GlobalExceptionHandler.kt`
  - [ ] Add handler:
    ```kotlin
    @ExceptionHandler(IngredientNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleIngredientNotFound(e: IngredientNotFoundException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            "Ingredient not found",
        ).also {
            it.type = URI.create("https://foodcost.app/errors/ingredient-not-found")
            it.title = "Not Found"
        }
    ```

- [ ] Task 8 — Backend unit tests for `update` (AC: 1, 2, 3, 5)
  - [ ] Edit `apps/backend/src/test/kotlin/com/foodcost/ingredient/IngredientServiceTest.kt`
  - [ ] Use **MockK** (same pattern as existing tests)
  - [ ] New tests:
    - `update_withValidRequest_updatesAndReturnsDto()` — verify fields updated, `updatedAt` refreshed, save called
    - `update_withSameNameDifferentCase_doesNotThrowDuplicate()` — name "Farina" → "farina" on same ingredient is OK
    - `update_withDuplicateNameDifferentIngredient_throwsDuplicateIngredientException()`
    - `update_withInvalidUnit_throwsInvalidUnitException()`
    - `update_withNonExistentId_throwsIngredientNotFoundException()`
    - `update_withWrongTenantId_throwsIngredientNotFoundException()` — simulates cross-tenant access attempt

- [ ] Task 9 — Backend integration tests for `PUT /api/v1/ingredients/{id}` (AC: 1, 2, 5)
  - [ ] Edit `apps/backend/src/test/kotlin/com/foodcost/ingredient/IngredientControllerIntegrationTest.kt`
  - [ ] New tests:
    - `PUT ingredients/{id} with valid body and JWT returns 200 and updated IngredientDto`
    - `PUT ingredients/{id} with duplicate name returns 422 RFC 7807`
    - `PUT ingredients/{id} with negative price returns 400`
    - `PUT ingredients/{id} with non-existent id returns 404`
    - `PUT ingredients/{id} without JWT returns 401`

### Frontend

- [ ] Task 10 — Add `IngredientUpdateRequest` to model and `update` to service (AC: 1)
  - [ ] Edit `apps/frontend/src/app/shared/models/ingredient.model.ts`
  - [ ] Add:
    ```typescript
    export interface IngredientUpdateRequest {
      name: string;
      unit: string;
      price: number;
    }
    ```
  - [ ] Edit `apps/frontend/src/app/features/warehouse/ingredient.service.ts`
  - [ ] Add method:
    ```typescript
    update(id: string, request: IngredientUpdateRequest): Observable<Ingredient> {
      return this.http.put<Ingredient>(`/api/v1/ingredients/${id}`, request);
    }
    ```

- [ ] Task 11 — Refactor `IngredientFormComponent` to support edit mode (AC: 1, 2, 4, 5)
  - [ ] Edit `apps/frontend/src/app/features/warehouse/ingredient-form/ingredient-form.component.ts`
  - [ ] Add `ingredient` input signal for edit mode:
    ```typescript
    readonly ingredient = input<Ingredient | null>(null);
    ```
  - [ ] On init, if `ingredient()` is set, pre-populate the form:
    ```typescript
    private readonly ingredientEffect = effect(() => {
      const ing = this.ingredient();
      if (ing) {
        this.form.patchValue({ name: ing.name, unit: ing.unit, price: ing.price });
      }
    });
    ```
  - [ ] Modify `submit()` to call either `create()` or `update()`:
    ```typescript
    const ing = this.ingredient();
    const obs = ing
      ? this.ingredientService.update(ing.id, { name, unit, price: price! })
      : this.ingredientService.create({ name, unit, price: price! });
    ```
  - [ ] Change snackbar message: `"Ingrediente aggiornato"` for edit, `"Ingrediente aggiunto"` for create
  - [ ] Update title in template: `"Modifica ingrediente"` when editing, `"Aggiungi ingrediente"` when creating
  - [ ] Same validation rules apply (price > 0, mat-error, submit-only validation)

- [ ] Task 12 — Add edit trigger to `IngredientListComponent` (AC: 4)
  - [ ] Edit `apps/frontend/src/app/features/warehouse/ingredient-list/ingredient-list.component.ts`
  - [ ] Add `openEditForm(ingredient: Ingredient)` method:
    ```typescript
    openEditForm(ingredient: Ingredient): void {
      const isMobile = this.breakpointObserver.isMatched('(max-width: 767px)');

      if (isMobile) {
        const ref = this.bottomSheet.open(IngredientFormComponent);
        ref.instance.ingredient = signal(ingredient); // NO — use component input properly
        // See Dev Notes for correct approach
      } else {
        const ref = this.dialog.open(IngredientFormComponent, {
          width: '480px',
          data: ingredient,
        });
        ref.componentInstance.saved.subscribe(() => {
          ref.close();
          this.ingredients.reload();
        });
      }
    }
    ```
  - [ ] **IMPORTANT:** Passing data to `MatDialog` / `MatBottomSheet` components requires using `MAT_DIALOG_DATA` / `MAT_BOTTOM_SHEET_DATA` injection tokens — see Dev Notes for the correct pattern
  - [ ] Edit template to make ingredient rows clickable:
    ```html
    <div class="ingredient-row" (click)="openEditForm(ingredient)" role="button" tabindex="0">
    ```
  - [ ] Add `cursor: pointer` and hover/focus styles to `.ingredient-row`

## Dev Notes

### Critical Security Rules (Non-Negotiable)

- **Lookup by BOTH `id` AND `tenantId`:** The `findByIdAndTenantId` query ensures a tenant can ONLY update their own ingredients. Using just `findById` would be a cross-tenant vulnerability (OWASP Broken Access Control — IDOR).
- **`tenantId` from JWT only:** Same as create — never from request body or path parameter.
- **No sensitive data in logs:** Do NOT log ingredient names, prices, or any tenant-specific data (GDPR).

### Backend — What to Reuse (DO NOT Re-Implement)

| Component | Location | Reuse Pattern |
|----------|----------|---------------|
| `JwtAuthenticationFilter` | `com.foodcost.auth.filter.JwtAuthenticationFilter` | Already extracts tenantId — just read from `authentication.details` |
| `SecurityConfig` | `com.foodcost.config.SecurityConfig` | `anyRequest().authenticated()` already protects all `/api/v1/**` |
| `GlobalExceptionHandler` | `com.foodcost.config.GlobalExceptionHandler` | ADD new `IngredientNotFoundException` handler — existing handlers for `DuplicateIngredientException` and `InvalidUnitException` already work for edit too |
| `extractTenantId()` | `IngredientController.kt` | Already exists as private method — reuse for the PUT endpoint |
| `IngredientService.ALLOWED_UNITS` | `IngredientService.kt` | Same unit validation applies |
| `DuplicateIngredientException` | Already exists | Reuse for edit duplicate name check |
| `InvalidUnitException` | Already exists | Reuse for edit invalid unit check |

### Backend — Ingredient Entity Mutation

The current `Ingredient.kt` entity uses `val` for `name`, `unit`, `price`. These must become `var` to support JPA-managed updates:

```kotlin
// BEFORE (from story 2-1):
val name: String,
val unit: String,
val price: BigDecimal,

// AFTER:
var name: String,
var unit: String,
var price: BigDecimal,
```

`id`, `tenantId`, and `createdAt` remain `val` — they should never change.

### Frontend — Passing Data to MatDialog / MatBottomSheet

The `IngredientFormComponent` currently uses `output()` signal for `saved` event. For edit mode, we need to pass the existing `Ingredient` data into the component.

**Pattern for MatDialog:**
```typescript
// In IngredientListComponent:
const ref = this.dialog.open(IngredientFormComponent, {
  width: '480px',
  data: ingredient,  // passed via MAT_DIALOG_DATA
});

// In IngredientFormComponent:
private readonly dialogData = inject<Ingredient | null>(MAT_DIALOG_DATA, { optional: true });
private readonly bottomSheetData = inject<Ingredient | null>(MAT_BOTTOM_SHEET_DATA, { optional: true });

// Resolve ingredient from either source:
private readonly editIngredient = this.dialogData ?? this.bottomSheetData ?? null;
```

**Pattern for MatBottomSheet:**
```typescript
// In IngredientListComponent:
const ref = this.bottomSheet.open(IngredientFormComponent, {
  data: ingredient,  // passed via MAT_BOTTOM_SHEET_DATA
});
```

This way the component works for both create (no data injected) and edit (data injected from either dialog or bottom sheet).

### Frontend — Form Component Dual Mode (Create vs Edit)

```typescript
// Determine mode from injected data
protected readonly isEditMode = !!this.editIngredient;

// Pre-populate on init
constructor() {
  if (this.editIngredient) {
    this.form.patchValue({
      name: this.editIngredient.name,
      unit: this.editIngredient.unit,
      price: this.editIngredient.price,
    });
  }
}

// Submit dispatches based on mode
submit(): void {
  // ...validation...
  const { name, unit, price } = this.form.getRawValue();
  const obs = this.editIngredient
    ? this.ingredientService.update(this.editIngredient.id, { name, unit, price: price! })
    : this.ingredientService.create({ name, unit, price: price! });
  // ...subscribe...
}
```

Template changes:
```html
<h2>{{ isEditMode ? 'Modifica ingrediente' : 'Aggiungi ingrediente' }}</h2>
<!-- ... -->
<button ...>{{ isEditMode ? 'Salva' : 'Aggiungi' }}</button>
```

### Frontend — Making Rows Tappable

```html
<div class="ingredient-row"
     (click)="openEditForm(ingredient)"
     (keydown.enter)="openEditForm(ingredient)"
     role="button"
     tabindex="0">
```

```scss
.ingredient-row {
  cursor: pointer;
  transition: background-color 0.15s ease;

  &:hover, &:focus-visible {
    background-color: rgba(255, 255, 255, 0.04);
    outline: none;
  }
}
```

WCAG: `role="button"`, `tabindex="0"`, `keydown.enter` handler for keyboard accessibility.

### Frontend — Angular Conventions Reminder

- `ChangeDetectionStrategy.OnPush` on ALL components — mandatory
- Do NOT add `standalone: true` — default in Angular 21
- Use `inject()` for DI
- Selector prefix: `fc-`
- Separate `.html` template file
- Native control flow (`@if`, `@for`) — never `*ngIf`, `*ngFor`
- `rxResource` for data loading — use `this.ingredients.reload()` after successful edit
- `mat-snack-bar` for success feedback (3s)
- Price display: `tabular-nums` + `number:'1.2-4'` pipe
- `inputmode="decimal"` on price field
- Validation on submit only

### Previous Story Learnings (from Story 2-1)

1. **`rxResource` uses `stream` property** (NOT `loader`) — this was an Angular 21 breaking change from the dev preview API. Pattern: `rxResource({ stream: () => this.service.getAll() })`
2. **`fc-skeleton` and `fc-error-message` don't exist** as shared components — story 2-1 implemented inline skeleton CSS animation and inline error HTML. Reuse the same inline patterns.
3. **MockK for unit tests, @SpringBootTest + MockMvc for integration** — NOT `@WebMvcTest` (Spring Boot version incompatibility).
4. **JWT test helper pattern** is already in `IngredientControllerIntegrationTest` — reuse `generateTestJwt()` for new PUT tests.
5. **Trim bug was fixed during review** — `IngredientService.create()` now trims name BEFORE the duplicate check. Apply the same pattern in `update()`.

### Project Structure Notes

No new directories needed. All changes go into existing ingredient package files.

**Backend files to EDIT:**
- `apps/backend/src/main/kotlin/com/foodcost/ingredient/entity/Ingredient.kt` — `val` → `var` for mutable fields
- `apps/backend/src/main/kotlin/com/foodcost/ingredient/repository/IngredientRepository.kt` — add `findByIdAndTenantId`
- `apps/backend/src/main/kotlin/com/foodcost/ingredient/service/IngredientService.kt` — add `update()` method
- `apps/backend/src/main/kotlin/com/foodcost/ingredient/IngredientController.kt` — add PUT endpoint
- `apps/backend/src/main/kotlin/com/foodcost/config/GlobalExceptionHandler.kt` — add IngredientNotFoundException handler
- `apps/backend/src/test/kotlin/com/foodcost/ingredient/IngredientServiceTest.kt` — add update tests
- `apps/backend/src/test/kotlin/com/foodcost/ingredient/IngredientControllerIntegrationTest.kt` — add PUT tests

**Backend files to CREATE:**
- `apps/backend/src/main/kotlin/com/foodcost/ingredient/dto/IngredientUpdateRequest.kt`
- `apps/backend/src/main/kotlin/com/foodcost/ingredient/service/IngredientNotFoundException.kt`

**Frontend files to EDIT:**
- `apps/frontend/src/app/shared/models/ingredient.model.ts` — add `IngredientUpdateRequest`
- `apps/frontend/src/app/features/warehouse/ingredient.service.ts` — add `update()` method
- `apps/frontend/src/app/features/warehouse/ingredient-form/ingredient-form.component.ts` — dual create/edit mode
- `apps/frontend/src/app/features/warehouse/ingredient-form/ingredient-form.component.html` — dynamic title/button
- `apps/frontend/src/app/features/warehouse/ingredient-list/ingredient-list.component.ts` — add `openEditForm()`
- `apps/frontend/src/app/features/warehouse/ingredient-list/ingredient-list.component.html` — clickable rows

### References

- [epics.md - Story 2.3](_bmad-output/planning-artifacts/epics.md) — acceptance criteria
- [architecture.md - Naming Patterns API Endpoints](_bmad-output/planning-artifacts/architecture.md) — `PUT /api/v1/ingredients/{id}` pattern
- [architecture.md - Process Patterns Tenant Context](_bmad-output/planning-artifacts/architecture.md) — tenant_id extraction from JWT
- [ux-design-specification.md - Modal Patterns](_bmad-output/planning-artifacts/ux-design-specification.md) — bottom-sheet mobile, dialog desktop
- [2-1-add-ingredient-to-warehouse.md](_bmad-output/implementation-artifacts/2-1-add-ingredient-to-warehouse.md) — previous story patterns and learnings
- [IngredientService.kt](apps/backend/src/main/kotlin/com/foodcost/ingredient/service/IngredientService.kt) — existing create method to follow
- [IngredientController.kt](apps/backend/src/main/kotlin/com/foodcost/ingredient/IngredientController.kt) — existing controller with extractTenantId
- [GlobalExceptionHandler.kt](apps/backend/src/main/kotlin/com/foodcost/config/GlobalExceptionHandler.kt) — existing RFC 7807 handlers
- [IngredientFormComponent](apps/frontend/src/app/features/warehouse/ingredient-form/ingredient-form.component.ts) — existing form to extend

## Dev Agent Record

### Agent Model Used

### Debug Log References

### Completion Notes List

### File List
