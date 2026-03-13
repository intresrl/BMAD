---
stepsCompleted: ['step-01-validate-prerequisites', 'step-02-design-epics', 'step-03-create-stories', 'step-04-final-validation']
workflowStatus: 'complete'
completedAt: '2026-03-13'
inputDocuments:
  - '_bmad-output/planning-artifacts/prd.md'
  - '_bmad-output/planning-artifacts/architecture.md'
  - '_bmad-output/planning-artifacts/ux-design-specification.md'
---

# FoodCost App - Epic Breakdown

## Overview

This document provides the complete epic and story breakdown for FoodCost App, decomposing the requirements from the PRD, UX Design, and Architecture requirements into implementable stories.

> **⚠️ Sprint Planning Note — MVP Vertical Slice Approach**
> All stories in this document are MVP scope. The epic sequence (1→7) represents **technical dependency order**, not sprint execution order.
> Sprint planning MUST follow a **vertical slice strategy**: each sprint delivers a runnable, end-to-end increment of the product by pulling stories from multiple epics. Do NOT implement one full epic per sprint — that produces a waterfall, not an MVP.
> When running sprint planning with the SM agent (Bob), explicitly instruct: *"Build MVP by vertical slice cross-epic, not epic-by-epic."*

## Requirements Inventory

### Functional Requirements

FR1: A user can register with email and password to create a restaurant account
FR2: A user can log in to their account with email and password
FR3: A user can request a password reset via email
FR4: A user can start a 14-day free trial without entering payment data
FR5: A user can select and subscribe to a plan (Base / Pro / Premium)
FR6: A user can independently update or change their subscription plan
FR7: A user can add an ingredient to the warehouse with name, unit of measure, and purchase price
FR8: A user can edit the price, quantity, and unit of measure of an existing ingredient
FR9: A user can organize ingredients by category (e.g., meats, fish, spices, beverages)
FR10: A user can view the complete warehouse list with updated prices
FR11: The system automatically updates the cost of all recipes that use an ingredient when its price changes
FR12: A user can photograph a delivery note (DDT) with the device camera
FR13: The system automatically extracts products, quantities, and prices from the bill photo
FR14: A user can review and manually correct OCR-extracted data before confirming
FR15: A user can confirm the warehouse update with the reviewed bill data
FR16: The system clearly signals unrecognized products with low confidence for manual review
FR17: The system improves recognition of bills from the same supplier over time
FR18: A user can create a recipe by adding ingredients with their respective quantities
FR19: A user can add operational costs to a recipe (energy in €/kWh, labor in hours × hourly cost)
FR20: A user can create a macro-recipe (base preparation) and use it as an ingredient in other recipes
FR21: The system automatically calculates the total cost of a recipe including composite macro-recipes
FR22: A user can view the food cost percentage of each recipe relative to the selling price
FR23: A user can update the selling price of a dish and see the recalculated food cost %
FR24: A user can generate a pre-service report with all currently active dishes
FR25: The report shows for each dish: unit cost, food cost %, selling price, and estimated margin
FR26: The report visually highlights dishes with food cost % above a configurable threshold
FR27: A user can add or remove dishes from the active card for the current service
FR28: The system guides a new user through the first 3 fundamental steps (first ingredient → first recipe → first report)
FR29: A user can access all main app sections from a mobile-first navigation
FR30: The system sends a reminder notification 2 hours before service to open the pre-service report
FR31: An administrator can create and activate a new tenant account (restaurant)
FR32: An administrator can assign or modify the subscription plan of a tenant
FR33: An administrator can view the onboarding funnel for each tenant (completed steps)
FR34: The system automatically sends an activation email to the new user after tenant creation

### NonFunctional Requirements

NFR1: User actions (navigation, saving, calculations) complete in <2 seconds on 4G mobile connection
NFR2: Pre-service report generates in <3 seconds even with 20+ dishes and composite recipes
NFR3: OCR process (photo → parsing results) completes in <60 seconds from photo confirmation
NFR4: Main dashboard loads in <1.5 seconds on first access after login
NFR5: All data at rest encrypted with AES-256 or equivalent
NFR6: All client-server communications protected with TLS 1.2+
NFR7: No sensitive commercial data (prices, margins, suppliers) exposed in logs, telemetry, or error reporting
NFR8: User passwords stored with secure hashing (Argon2id per OWASP 2024 recommendation)
NFR9: JWT sessions with refresh token rotation; persistent sessions on mobile to avoid re-authentication in the kitchen
NFR10: GDPR compliance: right to data deletion, signed DPA with EU cloud provider, max retention 90 days post-account deletion; bill images deleted automatically 30 days post-processing
NFR11: System supports 300 concurrent sessions (pre-service peak) without performance degradation
NFR12: Multi-tenant architecture supports growth from 100 to 10,000 tenants without re-architecture
NFR13: Recursive recipe data model supports composition depth up to 5 levels without performance impact on calculations
NFR14: Automatic daily backup of each tenant's data; RTO (Recovery Time Objective) <4 hours
NFR15: OCR layer abstracted by replaceable interface — provider change requires no core application changes
NFR16: OCR provider supports API calls with max 30-second timeout and automatic retry error handling
NFR17: Transactional email provider guarantees ≥98% delivery rate and supports HTML templates
NFR18: Payment integration supports Stripe or equivalent with self-service trial, upgrade, and cancellation management

### Additional Requirements

**From Architecture:**

- **Starter Template (impacts Epic 1 Story 1):** Monorepo with Angular CLI 21 PWA (`apps/frontend/`) + Spring Boot Initializr Kotlin (`apps/backend/`). Initialization commands are the first implementation story.
- Multi-tenancy via PostgreSQL Row-Level Security (RLS): every table has `tenant_id UUID NOT NULL`; `tenant_id` is NEVER accepted from user input — extracted exclusively from verified JWT via SecurityContext.
- Database migrations via Flyway (versioned SQL files, auto-applied at startup).
- Eager materialized cost graph: `calculated_cost` field on every recipe/macro-recipe, recalculated transactionally on every ingredient price change (BFS/DFS, max 5 levels); cycle detection mandatory before saving any recipe relationship.
- JWT: access token 15min, refresh token 30 days with rotation; stored in HttpOnly cookie on PWA mobile.
- Argon2id password hashing (`memoryCost=65536`, `iterations=3`, `parallelism=4`).
- Async OCR pipeline: `POST /api/v1/scans` → 202 Accepted → polling `GET /api/v1/scans/{scanId}` every 3s (max 20 attempts = 60s); OcrProvider interface (Adapter pattern) for provider substitutability.
- RFC 7807 Problem Details as the error response standard for all API errors.
- Rate limiting: OCR endpoint max 10 scans/minute per tenant; auth endpoint max 5 attempts/minute per IP.
- Feature gating: `plan` claim in JWT allows frontend/backend subscription tier enforcement without extra API calls; `planGuard` and `roleGuard` on Angular routes.
- Docker containerization (layered Spring Boot image + nginx Alpine for frontend); docker-compose for local development with PostgreSQL.
- GitHub Actions CI/CD: lint + test + build on PR; auto-deploy staging on merge to main; manual approval for production on version tags.
- Spring Boot Actuator for health/metrics; Sentry for error tracking (no PII/sensitive data in payloads); structured JSON logs with correlationId; explicit filter preventing prices, margins, VAT numbers, supplier names from appearing in logs.
- EU cloud provider mandatory from day 1 (GDPR data residency); DPA signed with provider.
- Self-service data export for users (GDPR data portability).

**From UX Design:**

- Dark mode only ("Nero di Cucina" theme) for MVP — `#111827` background, `#22C55E` accent; no light mode required.
- Responsive hybrid layout: Direction 1 "Comando" on mobile (< 768px) with persistent bottom navigation + FAB centered for OCR scan; Direction 6 "Zero" on desktop (≥ 768px) with topbar navigation + "Scansiona bolla" button in top-right.
- WCAG AA accessibility: minimum 44×44px tap targets, visible focus rings, minimum 14px text for interactive elements, `aria-label` on editable fields, food cost badge never communicates via color alone (always paired with numeric value).
- Angular CDK Virtual Scroll for long ingredient/recipe lists (500+ items) — no paginator.
- `FoodCostBadge` custom component: threshold-driven color coding (green ≤ threshold, amber threshold+1% to +5%, red > threshold+5%); size variants sm/md/lg.
- `OcrCartItem` custom component: states `recognized`, `low-confidence` (amber border + warning icon), `editing`, `confirmed`; inline editing without opening dialog.
- `RecipeCardCompact`, `KpiTile`, `OnboardingTracker`, `MacroRecipeBadge` custom components as defined in UX spec.
- `inputmode="decimal"` on price fields; `inputmode="numeric"` on integer quantity fields; Italian locale formatting for price display (`DecimalPipe '1.2-2'`).
- Progressive disclosure onboarding: 3-step activation (first ingredient → first recipe → first report), each step unlocked by the previous.
- OCR flow is linear and uninterruptible: scan → review cart → confirm → dashboard. Back arrow during flow prompts cancellation confirmation.
- `font-variant-numeric: tabular-nums` (`tabular-nums` Tailwind class) on all numeric data columns.
- Empty state pattern: centered SVG, 1-line message, one Secondary CTA — for Magazzino, Ricette, Bolle, Report, Admin tenant list.

### FR Coverage Map

FR1: Epic 1 — User registration with email/password
FR2: Epic 1 — Login with email/password
FR3: Epic 1 — Password reset via email
FR4: Epic 1 — 14-day trial activation
FR5: Epic 1 — Plan selection and subscription
FR6: Epic 1 — Self-service plan upgrade/change
FR7: Epic 2 — Add ingredient to warehouse
FR8: Epic 2 — Edit ingredient price/quantity/unit
FR9: Epic 2 — Organize ingredients by category
FR10: Epic 2 — View full warehouse list with current prices
FR11: Epic 2 — Automatic cost propagation to dependent recipes on price change
FR12: Epic 4 — Photograph delivery note (DDT) with device camera
FR13: Epic 4 — Automatic product/quantity/price extraction from OCR
FR14: Epic 4 — Review and manually correct OCR data before confirming
FR15: Epic 4 — Confirm warehouse update with reviewed bill data
FR16: Epic 4 — Clear signal of low-confidence unrecognized products
FR17: Epic 4 — Supplier-pattern learning for improved future accuracy
FR18: Epic 3 — Create recipe with ingredients and quantities
FR19: Epic 3 — Add operational costs (energy, labor) to a recipe
FR20: Epic 3 — Create macro-recipe and use it as ingredient in other recipes
FR21: Epic 3 — Automatic total cost calculation including composite macro-recipes
FR22: Epic 3 — View food cost % per recipe vs. selling price
FR23: Epic 3 — Update selling price and see recalculated food cost %
FR24: Epic 5 — Generate pre-service report for all active dishes
FR25: Epic 5 — Report shows unit cost, food cost %, selling price, estimated margin
FR26: Epic 5 — Visual highlight for dishes above configurable food cost threshold
FR27: Epic 5 — Add/remove dishes from active service card
FR28: Epic 6 — Guided 3-step onboarding for new users
FR29: Epic 6 — Mobile-first navigation to all app sections
FR30: Epic 6 — Reminder notification 2h before service to open report
FR31: Epic 7 — Create and activate new tenant account
FR32: Epic 7 — Assign/modify tenant subscription plan
FR33: Epic 7 — View onboarding funnel per tenant
FR34: Epic 7 — Automatic activation email after tenant creation

## Epic List

### Epic 1: Authenticated Restaurant Account
Restaurant owners can register, log in, manage their account, and activate a subscription trial — giving them a secure, personalized workspace for their restaurant. Includes project infrastructure initialization (monorepo Angular 21 + Spring Boot Kotlin) as the first story, establishing the foundation for all subsequent development. JWT-based subscription tier enforcement is established here and consumed by all future epics.
**FRs covered:** FR1, FR2, FR3, FR4, FR5, FR6

### Epic 2: Ingredient Warehouse
Restaurant owners can build and maintain a digital ingredient warehouse — adding, organizing, and updating ingredients with real prices, with automatic cost propagation to all dependent recipes when a price changes.
**FRs covered:** FR7, FR8, FR9, FR10, FR11

### Epic 3: Recipes & Macro-Recipes
Restaurant owners can create recipes using warehouse ingredients and macro-recipes (base preparations like stock or dough), and see the real-time food cost % of every dish — with automatic cost updates cascading through all composite preparations via the recursive cost graph.
**FRs covered:** FR18, FR19, FR20, FR21, FR22, FR23

### Epic 4: OCR Delivery Note Scanning
Restaurant owners on the Pro plan can photograph a supplier delivery note with their phone camera and automatically update their warehouse — with a review cart to confirm or manually correct extracted data, and supplier-pattern learning for improved future accuracy.
**FRs covered:** FR12, FR13, FR14, FR15, FR16, FR17

### Epic 5: Pre-Service Report
Restaurant owners can generate and consult a real-time pre-service food cost report for all active dishes — with threshold-based visual alerts, full margin visibility, and control over which dishes are in the active service card.
**FRs covered:** FR24, FR25, FR26, FR27

### Epic 6: Guided Onboarding & Notifications
New restaurant owners are guided through the 3 critical activation steps (first ingredient → first recipe → first report), ensuring they reach the product's "aha moment" within their first session. The mobile-first app shell navigation is delivered here. Existing users receive a pre-service reminder notification.
**FRs covered:** FR28, FR29, FR30

### Epic 7: Internal Admin Panel
Internal admins can create and activate restaurant tenant accounts, assign subscription plans, and monitor each tenant's onboarding funnel progress in real time — with automatic activation email delivery on tenant creation.
**FRs covered:** FR31, FR32, FR33, FR34

---

## Epic 1: Authenticated Restaurant Account

Restaurant owners can register, log in, manage their account, and activate a subscription trial — giving them a secure, personalized workspace for their restaurant. Includes project infrastructure initialization as Story 1.1, establishing the monorepo foundation for all subsequent development. JWT-based subscription tier enforcement is established here and consumed by all future epics.

### Story 1.1: Project Infrastructure Initialization

As a **developer**,
I want the monorepo project scaffolded with Angular 21 PWA (frontend) and Spring Boot Kotlin (backend) pre-configured with all base dependencies,
So that all subsequent stories have a consistent, runnable foundation to build on.

**Acceptance Criteria:**

**Given** the workspace is empty
**When** the initialization commands are executed
**Then** `apps/frontend/` contains an Angular 21 standalone PWA with `@angular/material`, `@angular/pwa`, and Tailwind CSS v4 configured; `apps/backend/` contains a Spring Boot Kotlin Gradle project with Spring Web, Spring Security, Spring Data JPA, PostgreSQL Driver, Flyway, Actuator, and Validation dependencies; and a `docker-compose.yml` at the root starts PostgreSQL 16 locally
**And** `ng serve` runs the frontend without errors and the backend starts with `./gradlew bootRun` connected to the local PostgreSQL
**And** the Angular global stylesheet applies the "Nero di Cucina" dark theme baseline (`--color-bg-base: #111827`) and the `fc-` component prefix is configured in the Angular workspace
**And** a GitHub Actions CI workflow file exists at `.github/workflows/ci.yml` that runs lint + build on every PR

---

### Story 1.2: User Registration

As a **restaurant owner**,
I want to register a new account with my email and password,
So that I have a secure, personal workspace for my restaurant in the app.

**Acceptance Criteria:**

**Given** I am on the registration screen
**When** I submit a valid email and a password of at least 8 characters
**Then** a new user and tenant record are created in the database; the password is stored hashed with Argon2id (never plaintext); and I am redirected to the onboarding screen
**And** the API returns HTTP 201 with the created user details (no password field in response)
**And** the `tenant_id` is generated server-side and never accepted from the registration request body
**And** if I submit an email already registered, the API returns HTTP 422 with RFC 7807 Problem Detail: `"detail": "An account with this email already exists"`
**And** if I submit an empty or invalid email format, a `mat-error` inline validation message appears below the field without submitting the form

---

### Story 1.3: User Login

As a **restaurant owner**,
I want to log in with my email and password,
So that I can access my restaurant's data securely.

**Acceptance Criteria:**

**Given** I have a registered account
**When** I submit my correct email and password on the login screen
**Then** the server returns an access token (15-minute expiry) and sets a refresh token in an HttpOnly cookie (30-day expiry, rotated on every use); I am redirected to the dashboard; and the `plan` and `tenantId` claims are present in the JWT payload
**And** navigating to any protected route without a valid access token redirects me to the login screen with `returnUrl` preserved; after login I am redirected to the original `returnUrl`
**And** if I submit incorrect credentials, the API returns HTTP 401 with RFC 7807 Problem Detail, and the UI displays an inline error without revealing whether the email or password was wrong
**And** the auth login endpoint enforces a rate limit of 5 attempts per minute per IP; on the 6th attempt within the window the API returns HTTP 429 with a `Retry-After` header

---

### Story 1.4: Password Reset via Email

As a **restaurant owner**,
I want to request a password reset via email,
So that I can regain access to my account if I forget my password.

**Acceptance Criteria:**

**Given** I am on the "Forgot Password" screen
**When** I submit my email address
**Then** I see a neutral confirmation: "If an account exists for this email, a reset link has been sent" (same message regardless of whether the email exists — prevents user enumeration)
**And** if the email is registered, a password-reset email is sent with a time-limited (1-hour) secure token link
**And** when I follow the reset link and submit a valid new password, my password is updated (Argon2id), all existing refresh tokens for my account are invalidated, and I am redirected to the login screen
**And** if the reset token is expired or already used, I see an inline error: "This link has expired. Request a new password reset."
**And** the reset link is single-use; a second use returns the expired token error

---

### Story 1.5: Trial Activation & Subscription Plan Selection

As a **restaurant owner**,
I want to activate a 14-day free trial and select my plan,
So that I can explore the product features before committing to a paid subscription.

**Acceptance Criteria:**

**Given** I have just registered and completed the first login
**When** I reach the plan selection screen
**Then** I can see the three plans (Base, Pro, Premium) with their feature sets, and I can activate the 14-day trial for any plan without entering payment data
**And** after activating the trial, the `plan` claim in my JWT reflects the selected tier (e.g., `"plan": "pro"`), and plan-gated features (OCR scan, Pre-Service Report) become accessible without a page refresh
**And** an automatic reminder email is sent 3 days before trial expiry
**And** after trial expiry with no payment, the account is downgraded to Base automatically; accessing Pro/Premium-gated routes returns HTTP 403 with RFC 7807 Problem Detail: `"detail": "This feature requires a Pro or Premium plan"`

---

### Story 1.6: Self-Service Plan Management

As a **restaurant owner**,
I want to upgrade, downgrade, or change my subscription plan independently from within the app,
So that I can adjust my plan as my needs change without contacting support.

**Acceptance Criteria:**

**Given** I am on the Account / Subscription settings screen
**When** I select a different plan and confirm the change
**Then** my subscription is updated immediately; newly issued JWTs carry the updated `plan` claim; and I see a snackbar: "Piano aggiornato a Pro"
**And** if I downgrade from Pro to Base, any open Pro-gated screens show an inline banner: "Questa funzione richiede il piano Pro" and disable the primary CTA without crashing
**And** plan changes do not delete any existing data (ingredients, recipes, scans) — all data is preserved across plan changes

---

## Epic 2: Ingredient Warehouse

Restaurant owners can build and maintain a digital ingredient warehouse — adding, organizing, and updating ingredients with real prices, with automatic cost propagation to all dependent recipes when a price changes.

### Story 2.1: Add Ingredient to Warehouse

As a **restaurant owner**,
I want to add a new ingredient to my warehouse with its name, unit of measure, and purchase price,
So that I can start building my digital inventory to calculate recipe costs.

**Acceptance Criteria:**

**Given** I am on the Magazzino screen
**When** I tap "Aggiungi ingrediente" and submit a valid name, unit of measure (from the grouped selector: Peso/Volume/Unità), and price per unit
**Then** the ingredient is saved with `tenant_id` injected server-side from the JWT (never from the request body); the API returns HTTP 201; and the new ingredient appears at the top of the warehouse list
**And** the price field uses `inputmode="decimal"` on mobile, and negative or zero values are rejected client-side with inline `mat-error`: "Inserisci un prezzo valido (es. 4.50)"
**And** if I submit a duplicate ingredient name within my tenant, the API returns HTTP 422 with RFC 7807 Problem Detail: `"detail": "An ingredient with this name already exists in your warehouse"`
**And** when the warehouse has no ingredients, an empty state is shown: "Nessun ingrediente ancora. Aggiungi il tuo primo ingrediente per iniziare a calcolare i costi." with an "Aggiungi ingrediente" secondary CTA button

---

### Story 2.2: View Warehouse List

As a **restaurant owner**,
I want to view my complete ingredient warehouse with all current prices and units,
So that I have a clear, real-time picture of my inventory.

**Acceptance Criteria:**

**Given** I have at least one ingredient in my warehouse
**When** I open the Magazzino section
**Then** the list loads in <1.5 seconds and shows all my ingredients with name, category, unit of measure, and current price per unit
**And** the list uses Angular CDK Virtual Scroll — all items are accessible by scrolling with no pagination controls
**And** price values use `tabular-nums` font styling for clean column alignment
**And** the list is scoped exclusively to my tenant — no other restaurant's ingredients are ever visible
**And** while the list is loading (>300ms), 3 skeleton placeholder rows are shown instead of a spinner

---

### Story 2.3: Edit Ingredient Price, Quantity, and Unit

As a **restaurant owner**,
I want to edit the price, quantity, and unit of measure of an existing ingredient,
So that my warehouse reflects the current real prices I am paying to suppliers.

**Acceptance Criteria:**

**Given** I am viewing an ingredient in my warehouse
**When** I update its price, quantity, or unit of measure and save
**Then** the ingredient is updated immediately; the API returns HTTP 200 with the updated resource; and the change is visible in the list without a full page reload
**And** validation prevents saving a negative or zero price with inline `mat-error`
**And** the `updated_at` timestamp is refreshed on the ingredient record
**And** on mobile the edit form opens as a `mat-bottom-sheet`; on desktop it opens inline or in a dialog

---

### Story 2.4: Organize Ingredients by Category

As a **restaurant owner**,
I want to assign a category to each ingredient and filter the warehouse list by category,
So that I can navigate my inventory efficiently as it grows.

**Acceptance Criteria:**

**Given** I am adding or editing an ingredient
**When** I select a category from the predefined list (Carni, Pesce, Latticini, Verdure, Spezie, Bevande, Altro)
**Then** the category is saved on the ingredient and displayed as a label in the warehouse list
**And** a category filter chip row is shown above the list; tapping a chip filters to show only that category's ingredients
**And** tapping "Tutti" or deselecting all chips restores the full unfiltered list
**And** filtering operates client-side on already-loaded data with no additional API calls

---

### Story 2.5: Automatic Recipe Cost Propagation on Price Change

As a **restaurant owner**,
I want the cost of all recipes that use an ingredient to update automatically when I change that ingredient's price,
So that my food cost percentages always reflect the real prices I am paying — without any manual recalculation.

**Acceptance Criteria:**

**Given** I have at least one recipe that uses an ingredient
**When** I update the price of that ingredient and save
**Then** the backend recalculates `calculated_cost` for all dependent recipes (direct and via macro-recipes up to 5 levels deep) in a single `@Transactional` BFS/DFS operation before returning the HTTP 200 response
**And** the next time I open any affected recipe, its food cost % already reflects the new ingredient price — no manual refresh required
**And** if the dependency graph would exceed 5 levels, the API returns HTTP 422 with RFC 7807 Problem Detail: `"detail": "Recipe composition depth exceeds maximum of 5 levels"`
**And** no prices, margins, or ingredient names appear in server logs or Sentry payloads (GDPR log filter enforced)

---

## Epic 3: Recipes & Macro-Recipes

Restaurant owners can create recipes using warehouse ingredients and macro-recipes (base preparations like stock or dough), and see the real-time food cost % of every dish — with automatic cost updates cascading through all composite preparations via the recursive cost graph.

### Story 3.1: Create Basic Recipe with Ingredients

As a **restaurant owner**,
I want to create a recipe by adding warehouse ingredients with their quantities,
So that I can calculate the real ingredient cost of a dish.

**Acceptance Criteria:**

**Given** I have at least one ingredient in my warehouse
**When** I create a new recipe with a name, number of portions, and at least one ingredient with its quantity and unit
**Then** the recipe is saved with `calculated_cost` computed by the backend; the API returns HTTP 201; and the recipe appears in my recipes list
**And** the ingredient search in the recipe editor is a `mat-autocomplete` that filters warehouse ingredients by name as I type, showing ingredient name and price-per-unit in each option
**And** the footer of the recipe editor shows the food cost % in real time using a `FoodCostBadge` component (green/amber/red based on 32% default threshold), updating on every quantity change without saving
**And** if I set a selling price, food cost % is calculated as `calculated_cost / selling_price_per_portion × 100`; if no selling price is set, the badge shows in neutral gray state
**And** on mobile, each recipe in the list is rendered as a `RecipeCardCompact` showing name, cost per portion, and food cost badge

---

### Story 3.2: Add Operational Costs to Recipe

As a **restaurant owner**,
I want to add energy and labor costs to a recipe,
So that my food cost calculation reflects the true total cost of preparing a dish, not just ingredient costs.

**Acceptance Criteria:**

**Given** I am editing a recipe
**When** I expand the "Costi operativi" section and enter energy cost (€/kWh × estimated kWh) and/or labor cost (hourly rate × estimated hours)
**Then** these operational costs are included in `calculated_cost` alongside ingredient costs; the real-time food cost % in the footer updates immediately to reflect the total
**And** operational cost fields are optional — a recipe with no operational costs is valid and saves without them
**And** the energy cost field uses `inputmode="decimal"` and rejects negative values with inline `mat-error`
**And** the stored `calculated_cost` equals ingredient costs + energy cost + labor cost summed together

---

### Story 3.3: Create Macro-Recipe and Use as Ingredient

As a **restaurant owner**,
I want to create a base preparation (macro-recipe) like stock, dough, or sauce and use it as an ingredient in other recipes,
So that my complex dishes show the true propagated cost of every component, no matter how deeply nested.

**Acceptance Criteria:**

**Given** I have a saved recipe designated as a macro-recipe
**When** I search for ingredients in another recipe's editor
**Then** macro-recipes appear in the same autocomplete list as regular ingredients, distinguished by a `MacroRecipeBadge` ("M" pill in green); selecting one adds it with a quantity (in grams or portions)
**And** the cost contributed by a macro-recipe is `macro_calculated_cost / macro_yield × quantity`, computed server-side and returned in the recipe item DTO
**And** before saving a recipe relationship, the backend validates no cycle exists (e.g., Recipe A → Macro B → Recipe A); if a cycle is detected, the API returns HTTP 422 with RFC 7807 Problem Detail: `"detail": "This would create a circular recipe dependency"`
**And** when I open a recipe that includes a macro-recipe ingredient, I can tap the macro-recipe item to navigate to its own detail screen (optional drill-down), but cost is already shown at the parent level without requiring it

---

### Story 3.4: View and Update Food Cost Percentage

As a **restaurant owner**,
I want to see the food cost percentage of each recipe and update the selling price to see it recalculate instantly,
So that I can make informed pricing decisions for every dish on my menu.

**Acceptance Criteria:**

**Given** I have a recipe with at least one ingredient and a selling price
**When** I open the recipe detail screen
**Then** the food cost % is displayed prominently using the `FoodCostBadge` in `lg` size variant at the top of the screen, with threshold-driven color (green ≤ 32%, amber ≤ 37%, red > 37%)
**And** when I edit the selling price field, the food cost % updates in real time in the header on every keystroke — no save required to preview the new value
**And** when I edit any ingredient quantity, the food cost % also updates in real time
**And** the recipe list (mobile card view and desktop table view) shows the `FoodCostBadge` in `sm` variant for every recipe without requiring the detail screen to be opened

---

### Story 3.5: Edit and Delete Recipe

As a **restaurant owner**,
I want to edit an existing recipe's ingredients, quantities, and operational costs, and delete a recipe I no longer need,
So that my recipe library stays accurate and up to date as my menu evolves.

**Acceptance Criteria:**

**Given** I have an existing recipe
**When** I update any ingredient, quantity, operational cost, or selling price and save
**Then** the backend recalculates `calculated_cost` transactionally; the API returns HTTP 200; and the updated food cost % is immediately visible in the recipe list without a page reload
**And** when I attempt to delete a recipe that is used as a macro-recipe ingredient in other recipes, the API returns HTTP 422: `"detail": "This recipe is used as an ingredient in N other recipes. Remove it from those recipes before deleting."`
**And** delete always requires a `mat-dialog` confirmation: title "Elimina ricetta?", body "Questa azione non può essere annullata.", actions "Annulla" (ghost) and "Elimina" (destructive)
**And** when the recipe list is empty, the empty state shows: "Nessuna ricetta in carta. Crea la tua prima ricetta per calcolare il food cost." with a "Nuova ricetta" secondary CTA

---

## Epic 4: OCR Delivery Note Scanning

Restaurant owners on the Pro plan can photograph a supplier delivery note with their phone camera and automatically update their warehouse — with a review cart to confirm or manually correct extracted data, and supplier-pattern learning for improved future accuracy.

### Story 4.1: OCR Async Pipeline Infrastructure

As a **developer**,
I want the OCR async pipeline infrastructure in place (upload endpoint, job processor, polling endpoint, OcrProvider adapter interface),
So that all subsequent OCR scan stories can rely on a stable, provider-agnostic processing backbone.

**Acceptance Criteria:**

**Given** the backend is running
**When** a Pro-tier authenticated user calls `POST /api/v1/scans` with a `multipart/form-data` image
**Then** the server returns HTTP 202 Accepted with `{ "scanId": "uuid", "status": "processing", "pollIntervalMs": 3000 }`; the image is saved to temporary storage; and an async OCR job is queued
**And** `GET /api/v1/scans/{scanId}` returns the current status: `{ "status": "processing|completed|partial|failed", "items": [...], "confidenceScores": {...} }`
**And** the `OcrProvider` interface is defined as an adapter — the concrete `HttpOcrProvider` implements it; swapping providers requires no changes outside the `providers/` package
**And** a Base-tier user calling `POST /api/v1/scans` receives HTTP 403 with RFC 7807 Problem Detail: `"detail": "This feature requires a Pro or Premium plan"`
**And** the OCR endpoint enforces a rate limit of 10 scans per minute per tenant; exceeding it returns HTTP 429 with a `Retry-After` header

---

### Story 4.2: Camera Capture and OCR Processing UI

As a **restaurant owner** on the Pro plan,
I want to tap the "Scansiona bolla" FAB and photograph a delivery note with my phone camera,
So that I can initiate automatic data extraction without leaving the app.

**Acceptance Criteria:**

**Given** I am on any screen of the app
**When** I tap the FAB (mobile: centered in bottom nav bar) or the "Scansiona bolla" button (desktop: topbar top-right)
**Then** the device's native camera opens directly with no intermediate screen; after taking the photo, the image is uploaded to `POST /api/v1/scans` and I see a full-screen loading state: "Analisi bolla in corso..." with a `mat-progress-spinner`
**And** the frontend polls `GET /api/v1/scans/{scanId}` every 3 seconds (max 20 attempts = 60 seconds) with a visible progress indicator throughout
**And** if the OCR provider returns `status: "failed"` or polling times out, the screen shows: "Documento non riconosciuto — riprova con luce migliore o carica un file" with options to retake the photo or upload a PDF/JPG
**And** tapping the back arrow during the scan or loading screen prompts a `mat-dialog` confirmation: "Vuoi annullare la scansione?" before discarding the in-progress scan

---

### Story 4.3: OCR Review Cart — Happy Path

As a **restaurant owner**,
I want to review the products extracted from my delivery note before confirming the warehouse update,
So that I have full control over what gets applied to my inventory.

**Acceptance Criteria:**

**Given** the OCR scan completes with `status: "completed"` (all items above confidence threshold)
**When** the review cart screen is shown
**Then** a scrollable list displays every extracted item as an `OcrCartItem` row showing: product name | quantity | unit of measure | unit price | previous warehouse price (if the ingredient already exists in the warehouse)
**And** price variations ≥ 15% vs. the stored warehouse price are highlighted with an amber badge showing the delta (e.g., "⚠️ +18%") — amber only, never red, as this is informational not an error
**And** I can tap any field on any row to edit it inline (name, quantity, unit, price) without opening a dialog
**And** the primary CTA "Conferma e aggiorna magazzino (N prodotti)" is active and shows the confirmed item count
**And** tapping "Annulla" discards the scan with no warehouse changes; no confirmation dialog is required to cancel

---

### Story 4.4: OCR Review Cart — Partial Recognition Edge Case

As a **restaurant owner**,
I want low-confidence or unrecognized items in the review cart to be clearly flagged for my correction,
So that I can fix OCR errors quickly inline without the flow being blocked by items the system could not read.

**Acceptance Criteria:**

**Given** the OCR scan completes with `status: "partial"` (some items below confidence threshold)
**When** the review cart screen is shown
**Then** low-confidence `OcrCartItem` rows render with an amber left border, a warning icon, and a "Da revisionare" label; their fields are pre-filled with the OCR best-guess but visually invite correction
**And** items with completely unrecognized fields are shown with empty highlighted fields; the primary CTA "Conferma" is disabled until all mandatory fields (name, quantity, price) are filled or the item is explicitly removed from the list
**And** I can remove an unrecognized item by tapping a trash icon on that row without blocking confirmation of the rest
**And** a summary badge at the top of the cart shows "N prodotti da revisionare" in amber
**And** after confirming a cart that included manual corrections, the backend calls `POST /api/v1/scans/{scanId}/learn-pattern` with the supplier corrections to improve future recognition accuracy

---

### Story 4.5: Warehouse Update Confirmation and Feedback

As a **restaurant owner**,
I want confirming the OCR review cart to update my warehouse and trigger recipe cost recalculation,
So that my ingredient prices and all dependent food cost percentages are accurate from the moment I confirm.

**Acceptance Criteria:**

**Given** I have reviewed the OCR cart and all mandatory fields are filled
**When** I tap "Conferma e aggiorna magazzino"
**Then** `POST /api/v1/scans/{scanId}/confirm` is called; the warehouse is updated for all confirmed items; and any ingredient price changes trigger the same transactional recipe cost recalculation as Story 2.5
**And** a `mat-snack-bar` appears for 4 seconds: "Magazzino aggiornato — N prodotti, M ricette ricalcolate" (silent background recalculation — no blocking spinner)
**And** the app returns to the previous context screen, not forced to the warehouse; the next time I open any affected recipe its food cost % is already updated
**And** the scan image is marked for automatic deletion 30 days after processing (stored as `delete_after` timestamp on the scan record — GDPR compliance)
**And** no product names, prices, or supplier data appear in server-side logs or Sentry payloads

---

## Epic 5: Pre-Service Report

Restaurant owners can generate and consult a real-time pre-service food cost report for all active dishes — with threshold-based visual alerts, full margin visibility, and control over which dishes are in the active service card.

### Story 5.1: Generate Pre-Service Report

As a **restaurant owner** on the Pro plan,
I want to open the Report section and see a pre-service food cost report with all my active dishes,
So that I can check margins and make pricing decisions before every service.

**Acceptance Criteria:**

**Given** I have at least one recipe in my active service card
**When** I navigate to the Report section
**Then** `GET /api/v1/report/pre-service` is called and the response is returned in <3 seconds even with 20+ dishes with composite macro-recipes
**And** the report displays all active dishes ordered by food cost % descending (highest cost first), each row showing: dish name | unit cost | food cost % (`FoodCostBadge` sm variant) | selling price | estimated margin
**And** the report reads the `calculated_cost` materialized field — no live recalculation is triggered on report load
**And** a Base-tier user attempting to access the Report route sees an inline banner: "Questa funzione richiede il piano Pro" without being hard-redirected
**And** when no recipes exist yet, the empty state shows: "Aggiungi almeno una ricetta con ingredienti per generare il report food cost." with a "Vai alle ricette" secondary CTA

---

### Story 5.2: Visual Threshold Alerts on Report

As a **restaurant owner**,
I want dishes above my food cost threshold to be visually highlighted in the report,
So that I can immediately spot which plates are hurting my margins without reading every row.

**Acceptance Criteria:**

**Given** I am viewing the pre-service report
**When** any dish has a food cost % above the configured threshold
**Then** its `FoodCostBadge` renders in amber (threshold+1% to threshold+5%) or red (> threshold+5%) — the badge always pairs color with the numeric value (never color-only, WCAG accessibility)
**And** the report header shows a summary: "N piatti fuori soglia" in the appropriate severity color
**And** tapping a dish row on mobile (or clicking on desktop) navigates to the recipe detail for that dish, where I can investigate or adjust ingredients and selling price
**And** the food cost threshold defaults to 32%; changes to the threshold are applied to the report immediately without a page reload

---

### Story 5.3: Configure Food Cost Threshold

As a **restaurant owner**,
I want to set my own food cost threshold percentage,
So that the report highlights are calibrated to my restaurant's actual margin targets, not a generic default.

**Acceptance Criteria:**

**Given** I am in Account Settings
**When** I update the food cost threshold (numeric field, 1–100%)
**Then** the new threshold is saved on my tenant profile and all `FoodCostBadge` components across the entire app (report, recipe list, recipe detail) reflect the new threshold coloring on next render
**And** the threshold field uses `inputmode="decimal"`, rejects values outside 1–100 with inline `mat-error`, and shows helper text: "Soglia predefinita: 32%"
**And** the threshold is stored per-tenant server-side and returned in the user profile API response so it is consistent across devices

---

### Story 5.4: Manage Active Service Card

As a **restaurant owner**,
I want to add or remove dishes from my active service card,
So that the pre-service report only shows the dishes I am actually serving tonight, not my entire recipe library.

**Acceptance Criteria:**

**Given** I have multiple recipes in my recipe library
**When** I toggle a recipe as "in carta" or "fuori carta" from the Report screen or the Recipe list
**Then** the change is saved immediately via `PATCH /api/v1/recipes/{id}` with `{ "inActiveCard": true/false }`; the pre-service report updates without a page reload
**And** the recipe list shows a small "in carta" chip on recipes currently in the active service card, allowing card management directly from the recipe list view
**And** attempting to remove the last dish from the active card shows an inline message: "La carta deve contenere almeno un piatto." and the toggle is reverted

---

## Epic 6: Guided Onboarding & Notifications

New restaurant owners are guided through the 3 critical activation steps (first ingredient → first recipe → first report), ensuring they reach the product's "aha moment" within their first session. The mobile-first app shell navigation is delivered here. Existing users receive a pre-service reminder notification.

### Story 6.1: App Shell Navigation (Mobile & Desktop)

As a **restaurant owner**,
I want a persistent navigation bar that gives me access to all main sections of the app from any screen,
So that I can move between Dashboard, Magazzino, Ricette, and Report without getting lost.

**Acceptance Criteria:**

**Given** I am logged in and on any screen of the app
**When** I view the app on mobile (< 768px)
**Then** a persistent bottom navigation bar shows 4 items: Dashboard · Magazzino · Ricette · Report, with a centered elevated FAB (`mat-fab`) between items 2 and 3 for the OCR scan action (visible only on Pro/Premium plan; hidden on Base)
**And** the active nav item shows the accent green icon + label with a 2px green underline; inactive items render in `text-gray-400`
**And** when I view the app on desktop (≥ 768px), a `mat-toolbar` topbar shows the same 4 navigation links horizontally + a "Scansiona bolla" `mat-raised-button` in the top-right (Pro/Premium only)
**And** the navigation uses the same Angular route vocabulary on both breakpoints — the user's mental model does not change between mobile and desktop
**And** nav items with pending alerts (e.g., Report with dishes above threshold) show a `matBadge` numeric count on the nav icon

---

### Story 6.2: Guided 3-Step Onboarding for New Users

As a **new restaurant owner**,
I want to be guided through adding my first ingredient, creating my first recipe, and opening my first report,
So that I reach a working food cost calculation within my first session without needing external help.

**Acceptance Criteria:**

**Given** I have just registered and completed plan selection
**When** I first enter the app
**Then** an onboarding progress banner is shown at the top of the Dashboard with 3 steps: "1. Aggiungi il tuo primo ingrediente · 2. Crea la tua prima ricetta · 3. Apri il report food cost"; each step shows a status indicator (gray = pending, green checkmark = completed)
**And** tapping a pending step navigates me directly to the relevant section with a contextual tooltip highlighting the primary action (e.g., "Aggiungi ingrediente" button highlighted on first visit to Magazzino)
**And** each step is automatically marked as completed when the corresponding server-side event is detected: ingredient created (step 1), recipe created (step 2), report endpoint called (step 3)
**And** once all 3 steps are completed, the onboarding banner disappears permanently and a one-time snackbar confirms: "Configurazione completata — benvenuto in FoodCost App"
**And** onboarding progress is persisted server-side per user so it survives page refreshes and device changes

---

### Story 6.3: Pre-Service Reminder Notification

As a **restaurant owner**,
I want to receive a reminder email approximately 2 hours before my service,
So that I remember to check the pre-service food cost report before the rush starts.

**Acceptance Criteria:**

**Given** I have a Pro or Premium plan and an active service card with at least one dish
**When** the scheduled time 2 hours before my configured service start time is reached
**Then** the backend `NotificationScheduler` sends a transactional email with subject: "Il tuo report pre-servizio è pronto" and a direct deep link to the Report section
**And** the service start time defaults to 19:00 and is configurable in Account Settings (time picker, 24h format)
**And** the reminder email is sent only once per day per user; if I have already opened the report that day, no email is sent
**And** I can opt out of reminder emails from Account Settings with a single toggle; opting out does not affect other transactional emails (trial expiry, activation)
**And** the email body contains no prices, food cost data, ingredient names, or margin figures — only the call-to-action link (GDPR: no sensitive business data in email bodies)

---

## Epic 7: Internal Admin Panel

Internal admins can create and activate restaurant tenant accounts, assign subscription plans, and monitor each tenant's onboarding funnel progress in real time — with automatic activation email delivery on tenant creation.

### Story 7.1: Admin Panel Access and Tenant List

As an **internal admin**,
I want to access a dedicated admin panel and see the list of all registered restaurant tenants,
So that I have a central place to manage all restaurant accounts on the platform.

**Acceptance Criteria:**

**Given** I am logged in with an account that has the `admin` role in the JWT
**When** I navigate to the `/admin` route
**Then** I see a list of all tenants with: restaurant name | owner email | current plan | trial status | registration date | onboarding funnel progress summary
**And** the admin route is protected by `roleGuard('admin')` — a non-admin user attempting to access `/admin` receives HTTP 403 with RFC 7807 Problem Detail: `"detail": "Admin access required"`
**And** the tenant list supports basic text search by restaurant name or email (client-side on loaded data)
**And** when there are no tenants yet, the empty state shows: "Nessun tenant attivo." with a "Nuovo tenant" CTA button

---

### Story 7.2: Create and Activate New Tenant

As an **internal admin**,
I want to create a new restaurant tenant account with a name, owner email, initial plan, and trial duration,
So that I can onboard new restaurant clients onto the platform without requiring them to self-register.

**Acceptance Criteria:**

**Given** I am on the Admin Panel tenant list screen
**When** I fill in the "Nuovo Tenant" form (restaurant name, owner email, initial plan: Base/Pro/Premium, trial duration in days) and submit
**Then** `POST /api/v1/admin/tenants` is called; a new tenant and user record are created server-side; PostgreSQL RLS policies are provisioned for the new `tenant_id`; and the new tenant appears in the list immediately
**And** the API returns HTTP 201; if the email already exists, HTTP 422 is returned with RFC 7807: `"detail": "A user with this email already exists"`
**And** all form fields are validated on submit: restaurant name required, email must be valid format, plan must be one of Base/Pro/Premium, trial days must be a positive integer
**And** form validation errors appear as inline `mat-error` messages below each field — no toast errors for validation failures

---

### Story 7.3: Automatic Activation Email

As an **internal admin**,
I want a new tenant's owner to automatically receive an activation email when I create their account,
So that they can set their password and access the platform immediately without manual follow-up from me.

**Acceptance Criteria:**

**Given** a new tenant has just been created via Story 7.2
**When** the `POST /api/v1/admin/tenants` request completes successfully
**Then** an activation email is automatically sent to the owner's address containing: a welcome message with the restaurant name, their assigned plan and trial duration, and a secure activation link (time-limited to 72 hours) to set their password
**And** the activation link uses the same single-use token mechanism as password reset (Story 1.4) — 72-hour expiry, stored token hashed with Argon2id
**And** if email delivery fails, the tenant record is still created and the admin sees a warning snackbar: "Tenant creato. Invio email non riuscito — riprova dall'elenco tenant."
**And** the admin can manually resend the activation email from the tenant detail screen via a "Reinvia email attivazione" secondary button

---

### Story 7.4: Assign and Modify Tenant Subscription Plan

As an **internal admin**,
I want to change the subscription plan of any tenant at any time,
So that I can handle manual upgrades, downgrades, or corrections without requiring the tenant to do it themselves.

**Acceptance Criteria:**

**Given** I am viewing a tenant's detail screen
**When** I select a new plan from a `mat-select` and confirm
**Then** `PATCH /api/v1/admin/tenants/{id}` updates the tenant's plan; the tenant's next JWT refresh carries the updated `plan` claim; and I see a snackbar: "Piano aggiornato a Pro per Ristorante Da Luca"
**And** a plan change by the admin takes precedence over any self-service change the tenant may have made
**And** downgrading a tenant's plan does not delete their data (ingredients, recipes, scans) — all data is preserved

---

### Story 7.5: Monitor Tenant Onboarding Funnel

As an **internal admin**,
I want to see the onboarding funnel progress for each tenant (which of the 3 activation steps they have completed),
So that I can identify tenants who are stuck and proactively outreach before their trial expires.

**Acceptance Criteria:**

**Given** I am viewing a tenant's detail screen
**When** the onboarding funnel section is shown
**Then** an `OnboardingTracker` component displays 3 steps with their completion status: "1. Primo ingrediente aggiunto" · "2. Prima ricetta creata" · "3. Primo report aperto" — each with a gray (pending), pulsing green (active), or solid green checkmark (completed) indicator
**And** the funnel data is fetched from the same onboarding events stored server-side that drive Story 6.2
**And** a tenant with all 3 steps completed shows an "Onboarding completato ✓" badge next to their name in the tenant list
**And** the tenant list can be filtered by onboarding status: "Tutti" | "Onboarding completato" | "In corso" | "Non iniziato" — to quickly identify at-risk tenants during trial
