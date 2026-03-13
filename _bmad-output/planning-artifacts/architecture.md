---
stepsCompleted: [1, 2, 3, 4, 5, 6, 7, 8]
inputDocuments:
  - '_bmad-output/planning-artifacts/prd.md'
  - '_bmad-output/planning-artifacts/ux-design-specification.md'
  - '_bmad-output/planning-artifacts/research/market-food-cost-webapp-restaurants-research-2026-03-03.md'
  - '_bmad-output/brainstorming/brainstorming-session-2026-03-03-T1430.md'
workflowType: 'architecture'
lastStep: 8
workflowStatus: 'complete'
completedAt: '2026-03-13'
project_name: 'BMAD'
user_name: 'Intre'
date: '2026-03-13'
---

# Architecture Decision Document

_This document builds collaboratively through step-by-step discovery. Sections are appended as we work through each architectural decision together._

---

## Project Context Analysis

### Requirements Overview

**Functional Requirements:**
34 FR suddivisi in 7 categorie: Autenticazione & Account (FR1-FR6), Magazzino Ingredienti (FR7-FR11), OCR Bolla Fornitore (FR12-FR17), Ricette & Macro-Ricette (FR18-FR23), Report Pre-Servizio (FR24-FR27), Onboarding & Navigazione (FR28-FR30), Pannello Admin Interno (FR31-FR34).

Nessun epic/story set formale disponibile — architettura derivata direttamente dai FR.

**Non-Functional Requirements (architetturalmente significativi):**
- Performance: <2s azioni utente su 4G; <3s generazione report; <60s OCR processing; <1.5s dashboard load
- Security: AES-256 at-rest, TLS 1.2+, bcrypt/Argon2, JWT+refresh, nessun dato sensibile in log/telemetria
- GDPR: EU data residency obbligatoria, diritto cancellazione, retention 90gg post-account, DPA cloud provider
- Scalability: 300 sessioni concorrenti picco; 100→10.000 tenant senza re-architetture; ricorsività ricette fino a 5 livelli
- Integration: layer OCR sostituibile (no vendor lock-in), email delivery ≥98%, pagamenti Stripe-compatible

**Scale & Complexity:**
- Primary domain: Full-stack SaaS web + PWA mobile-first
- Complexity level: Medium-High
- Architettura multi-tenant con row-level security PostgreSQL
- Graph computation per propagazione costi ricorsivi
- Async OCR processing con fallback manuale

### Technical Constraints & Dependencies

**Stack definito a priori (vincoli non negoziabili):**
- Frontend: Angular PWA + Angular Material + Tailwind CSS
- Backend: Kotlin (Spring Boot o Ktor — da decidere)
- Database: PostgreSQL con row-level security per isolamento tenant
- Hosting: EU cloud provider obbligatorio (candidati: Hetzner, OVHcloud, AWS/GCP Frankfurt)
- Auth: JWT con refresh token, sessioni persistenti mobile
- OCR: provider TBD post-scelta cloud, accesso via adapter pattern
- Email: provider SMTP/API transazionale (Resend, Postmark o equiv.)
- Pagamenti: Stripe o equivalente con gestione trial/upgrade/cancel self-service

**Dipendenze esterne critiche:**
1. OCR provider — unica dipendenza esterna non sostituibile a breve termine; accuracy ≥90% su DDT italiani, timeout ≤30s
2. Email transazionale — delivery ≥98%, template HTML
3. Payment processor — gestione lifecycle sottoscrizioni (trial, upgrade, cancel)

### Cross-Cutting Concerns Identified

1. **Multi-tenancy** — ogni request deve essere scoped al tenant corrente; row-level security a DB, tenant context propagation in ogni layer
2. **Cost graph recalculation** — propagazione atomica e consistente del costo attraverso il grafo ricette/macro-ricette a ogni variazione prezzo ingrediente
3. **GDPR & Security** — hashing password, cifratura at-rest, zero dati sensibili in log/telemetria, hard delete + soft delete policy
4. **OCR async pipeline** — upload foto, processing asincrono, confidence scoring, callback/polling, fallback manuale
5. **Subscription tier enforcement** — feature gating coerente tra backend (API) e frontend (UI) per piani Base/Pro/Premium
6. **PWA service worker** — installabilità, aggiornamenti background, graceful degradation in condizioni di rete scarsa in cucina

---

## Starter Template Evaluation

### Primary Technology Domain

Full-stack SaaS web — monorepo unico con due app: Angular 21 PWA (frontend) in `apps/frontend/` e Kotlin Spring Boot (backend) in `apps/backend/`.

### Starter Options Considered

- **Frontend:** Angular CLI — unica scelta canonica per Angular PWA; versione target 21
- **Backend Opzione A:** Spring Boot Initializr (Kotlin) — framework completo, batterie incluse
- **Backend Opzione B:** Ktor (Kotlin) — lightweight, Kotlin-first, più configurazione manuale richiesta

### Selected Starter: Angular CLI 21 + Spring Boot Initializr (Kotlin)

**Rationale for Selection:**
Spring Boot scelto su Ktor per: multi-tenancy Hibernate nativa, Spring Security JWT consolidato, Actuator per health/monitoring, riduzione configurazione manuale per team piccolo MVP-oriented. Angular 21 per accesso alle API signals mature, standalone components di default, e control flow sintattico nativo.

**Initialization Commands:**

```bash
# 1. Creare la struttura monorepo nella root del workspace
mkdir -p apps

# 2. Frontend — Angular 21 in apps/frontend
ng new frontend --routing --style=scss --ssr=false --strict --directory=apps/frontend
cd apps/frontend
ng add @angular/pwa
ng add @angular/material
npm install -D tailwindcss @tailwindcss/vite
cd ../..

# 3. Backend — Spring Boot (Kotlin + Gradle) in apps/backend
# Via start.spring.io — Project: Gradle-Kotlin, Language: Kotlin,
# Spring Boot: latest stable, Package: com.foodcost
# Dependencies: Spring Web, Spring Security, Spring Data JPA,
#               PostgreSQL Driver, Validation, Actuator, Flyway Migration
# Estrarre lo zip generato in apps/backend/

# 4. Root del monorepo
# Creare docker-compose.yml e README.md nella root
```

**Architectural Decisions Provided by Starter:**

**Language & Runtime:**
- TypeScript strict (frontend) + Kotlin 2.x JVM 21 LTS (backend)
- Gradle Kotlin DSL build script

**Styling Solution:**
- Angular Material M3 theming + Tailwind CSS v4 utility layer
- SCSS per component styles

**Build Tooling:**
- Frontend: Angular CLI + Vite/esbuild (fast builds, HMR)
- Backend: Gradle + Spring Boot Plugin (fat JAR, layered Docker image)

**Testing Framework:**
- Frontend: Jest + Angular Testing Library + Angular CDK Component Harnesses
- Backend: JUnit 5 + MockK (Kotlin mock library) + Spring Boot Test

**Code Organization:**
- Frontend: feature-based folder structure (`src/app/features/`, `src/app/shared/`)
- Backend: package-by-feature (`com.foodcost.ingredient`, `com.foodcost.recipe`, `com.foodcost.ocr`, `com.foodcost.report`, `com.foodcost.admin`)

**Development Experience:**
- Frontend: `ng serve` con HMR + proxy config verso backend locale
- Backend: Spring Boot DevTools con hot reload
- Docker Compose per PostgreSQL locale in sviluppo

**Note:** Project initialization using these commands should be the first implementation story.

---

## Frontend Developer Guidelines (Angular 21)

> Reference: [Angular full LLM context](https://angular.dev/assets/context/llms-full.txt)

### Persona

Angular developer che usa le feature più moderne del framework: signals per lo state management reattivo, standalone components per architettura snella, control flow sintattico nativo. Performance è priorità costante — change detection ottimizzata e paradigmi moderni Angular sono la norma, non l'eccezione.

### Key Documentation References

| Area | Link |
|---|---|
| Overview | https://angular.dev/overview |
| Style Guide | https://next.angular.dev/style-guide |
| Components | https://angular.dev/guide/components |
| Signals | https://angular.dev/guide/signals |
| Templates | https://angular.dev/guide/templates |
| Control Flow | https://angular.dev/guide/templates/control-flow |
| Dependency Injection | https://angular.dev/guide/di |
| HttpClient | https://angular.dev/guide/http |
| Reactive Forms | https://angular.dev/guide/forms/reactive-forms |
| Routing | https://angular.dev/guide/routing |
| PWA / Service Worker | https://angular.dev/guide/performance |
| Zoneless | https://angular.dev/guide/zoneless |
| RxJS Interop | https://angular.dev/ecosystem/rxjs-interop |
| NgOptimizedImage | https://angular.dev/guide/image-optimization |
| Testing | https://angular.dev/guide/testing |
| Security | https://angular.dev/best-practices/security |
| API Reference | https://angular.dev/api |

### TypeScript Best Practices

- Usa `strict` type checking (già configurato da `ng new --strict`)
- Preferisci l'inferenza di tipo quando il tipo è ovvio
- Evita `any` — usa `unknown` quando il tipo è incerto

### Angular Best Practices

- **Sempre standalone components** — mai `NgModules`
- **Non impostare** `standalone: true` nei decoratori `@Component`, `@Directive`, `@Pipe` (è il default in Angular 21)
- Usa **signals** per lo state management
- Implementa **lazy loading** per ogni feature route
- **Non usare** `@HostBinding` / `@HostListener` — usa l'oggetto `host` nel decoratore `@Component` o `@Directive`
- Usa `NgOptimizedImage` per tutte le immagini statiche (non funziona per base64 inline)

### Component Conventions

```ts
import { ChangeDetectionStrategy, Component, signal, computed, input, output } from '@angular/core';

@Component({
  selector: 'fc-example',
  templateUrl: './example.html',
  styleUrl: './example.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'fc-example' },
})
export class ExampleComponent {
  // inputs via signal
  readonly value = input<number>(0);
  // outputs via function
  readonly changed = output<number>();
  // local state via signal
  protected readonly count = signal(0);
  // derived state via computed
  protected readonly doubled = computed(() => this.count() * 2);
}
```

- `input()` signal invece dei decoratori `@Input()`
- `output()` function invece dei decoratori `@Output()`
- `computed()` per stato derivato
- `ChangeDetectionStrategy.OnPush` sempre
- Logica nel file `.ts`, stili nel file `.scss`, template nel file `.html`

### Template Rules

- Usa **control flow nativo** (`@if`, `@for`, `@switch`) — mai `*ngIf`, `*ngFor`, `*ngSwitch`
- **Non usare** `ngClass` — usa class bindings: `[class.active]="condition"`
- **Non usare** `ngStyle` — usa style bindings: `[style.color]="value"`
- Non assumere globali come `new Date()` disponibili nel template
- Usa l'`async` pipe per gli observables
- Importa le pipe quando usate nel template

### State Management

- Signals per lo stato locale del componente
- `computed()` per lo stato derivato
- Trasformazioni di stato pure e predicibili
- **Non usare** `mutate()` sui signals — usa `update()` o `set()`

### Services

- Un servizio, una responsabilità
- `providedIn: 'root'` per i servizi singleton
- Usa `inject()` function invece del constructor injection

### Forms

- Preferisci **Reactive Forms** rispetto ai Template-driven
- Usa typed forms (`FormControl<T>`)

### Accessibility

- Deve superare tutti i check **AXE**
- Deve rispettare i minimi **WCAG AA**: focus management, color contrast, ARIA attributes

### Example Component Pattern

```ts
// example.component.ts
import { ChangeDetectionStrategy, Component, signal } from '@angular/core';

@Component({
  selector: 'fc-server-status',
  templateUrl: './server-status.html',
  styleUrl: './server-status.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ServerStatusComponent {
  protected readonly isRunning = signal(true);

  toggle() {
    this.isRunning.update(v => !v);
  }
}
```

```html
<!-- server-status.html -->
<section class="container">
  @if (isRunning()) {
    <span>Server is running</span>
  } @else {
    <span>Server is not running</span>
  }
  <button (click)="toggle()">Toggle</button>
</section>
```

```scss
/* server-status.scss */
.container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
```

---

## Core Architectural Decisions

### Decision Priority Analysis

**Decisioni critiche (bloccano l'implementazione):**
- Multi-tenancy strategy (RLS PostgreSQL)
- Calcolo grafo costi ricorsivi (eager materializzato)
- JWT strategy con refresh token rotation
- OCR async pipeline (upload → polling → fallback)
- API design pattern (REST + RFC 7807)

**Decisioni importanti (plasmano l'architettura):**
- Feature-based lazy routing Angular
- Angular Signals + rxResource (nessun NgRx)
- SpringDoc OpenAPI 3 per documentazione API
- Docker + GitHub Actions CI/CD
- Argon2id per password hashing

**Decisioni differite (post-MVP):**
- Event-driven recalculation (valutare se il grafo cresce oltre 5 livelli medi)
- Multi-region EU deployment (EU expansion Phase 3)
- WebSocket/SSE per notifiche real-time (attualmente polling)

---

### Categoria 1 — Data Architecture

**Multi-tenancy Strategy: Row-Level Security (RLS) PostgreSQL**
- Un unico schema, ogni tabella ha colonna `tenant_id UUID NOT NULL`
- PostgreSQL RLS policy attiva su tutte le tabelle tenant-scoped
- `tenant_id` estratto dal JWT → propagato via Spring Security `SecurityContext` → applicato via Hibernate `@Filter` su ogni sessione
- Nessun utente può mai inviare/manipolare `tenant_id` via API — viene sempre sovrascritto dal server
- Scala da 100 a 10.000 tenant senza re-architetture (conforme NFR PRD)

**Strategia migrazione DB: Flyway**
- Versionamento `V{n}__{descrizione}.sql`
- Incluso nello Spring Boot Initializr, zero configurazione aggiuntiva
- Migrazione automatica all'avvio del backend

**Calcolo grafo costi ricorsivi: Eager con costo materializzato**
- Ogni ricetta/macro-ricetta memorizza il campo `calculated_cost` aggiornato a ogni salvataggio
- Alla modifica del prezzo di un ingrediente → Spring Service ricalcola in transazione tutte le ricette dipendenti (BFS/DFS sul grafo, max 5 livelli)
- Detection cicli ricorsivi obbligatoria a livello di validazione (un ingrediente non può dipendere da sé stesso direttamente o indirettamente)
- Il report pre-servizio legge `calculated_cost` materializzato — risposta in <3s anche con 20+ piatti
- Profondità massima supportata: 5 livelli (conforme NFR PRD)

**Caching strategy:**
- Nessuna cache applicativa per MVP (scale 300 sessioni concorrenti → PostgreSQL sufficiente con indici ottimizzati)
- Valutare Redis per sessioni e cache report se i volumi crescono post-MVP

---

### Categoria 2 — Autenticazione & Sicurezza

**JWT Strategy**
- Access token: scadenza 15 minuti, payload: `{ sub: userId, tenantId, roles, plan }`
- Refresh token: scadenza 30 giorni, rotazione ad ogni uso (rotation strategy)
- Refresh token persiste su DB (revocabile lato server — logout forzato, gestione sessioni multiple)
- Su PWA mobile: refresh token in HttpOnly cookie (protegge da XSS)
- Il campo `plan` nel JWT consente feature gating lato frontend senza chiamate extra

**Password hashing: Argon2id**
- Raccomandato OWASP 2024 contro GPU/ASIC attacks
- Configurazione: `memoryCost=65536`, `iterations=3`, `parallelism=4`
- Spring Security supporta Argon2PasswordEncoder nativo

**Multi-tenancy a livello API — regola fondamentale:**
- Il `tenant_id` non viene MAI accettato da request body/path param dell'utente
- Estratto esclusivamente dal JWT token verificato → propagato in ogni layer via SecurityContext
- Questa regola previene privilege escalation cross-tenant (OWASP Broken Access Control)

**Cifratura e compliance GDPR:**
- Dati a riposo: AES-256 (PostgreSQL pgcrypto o cifratura a livello cloud provider)
- Comunicazioni: TLS 1.2+ obbligatorio
- Log/telemetria: filtro esplicito su prezzi, margini, P.IVA, dati fornitore
- Hard delete su richiesta GDPR + retention 90gg post-cancellazione account
- Immagini bolle OCR: eliminate automaticamente 30 giorni post-processing

---

### Categoria 3 — API & Comunicazione

**API Design: REST stateless**
- Prefisso: `/api/v1/`
- Naming: `kebab-case` per path, `camelCase` per JSON body
- HTTP semantici: `200 OK`, `201 Created`, `202 Accepted` (OCR async), `204 No Content`, `400/401/403/404/422/429/500`
- Nessun GraphQL per MVP

**API Documentation: SpringDoc OpenAPI 3**
- Generazione automatica Swagger UI da annotazioni Kotlin/Spring
- Disponibile in ambiente non-production su `/swagger-ui.html`
- Esporta spec OpenAPI JSON per generazione client Angular automatica (opzionale)

**OCR Async Pipeline:**
```
[Frontend] POST /api/v1/scans  (multipart/form-data: image)
    → 202 Accepted { scanId: "uuid" }
    → polling GET /api/v1/scans/{scanId} ogni 3s (max 20 tentativi = 60s)
    → risposta finale: { status: "completed|failed|partial", items: [...], confidenceScores: {...} }
    → se status=="failed": UI mostra form inserimento manuale
    → se status=="partial": UI mostra carrello revisione con item a bassa confidenza evidenziati
```
- Backend: controller riceve immagine → salva su storage temp → pubblica job asincrono → worker chiama OCR provider → aggiorna record scan
- OCR provider callable via interfaccia `OcrProvider` → implementazioni sostituibili senza toccare il core (Adapter pattern)
- Timeout OCR provider: 30s (conforme NFR PRD), retry automatico 1x prima di fallback manuale

**Error Handling Standard: RFC 7807 Problem Details**
```json
{
  "type": "https://foodcost.app/errors/ingredient-not-found",
  "title": "Ingredient not found",
  "status": 404,
  "detail": "Ingredient with id 'abc-123' does not exist in your warehouse",
  "instance": "/api/v1/ingredients/abc-123"
}
```
- Nessun stack trace nei response body di produzione
- Tutti gli errori 5xx loggati server-side con correlation ID

**Rate Limiting:**
- Endpoint OCR: max 10 scan/minuto per tenant (protezione costi OCR provider)
- Endpoint autenticazione: max 5 tentativi/minuto per IP (protezione brute force)
- Header `Retry-After` su 429 responses

---

### Categoria 4 — Frontend Architecture

**State Management: Angular Signals + rxResource**
- Nessun NgRx per MVP
- Pattern: `FeatureStore` service iniettabile (`providedIn: 'root'`) che espone signals
- `rxResource` (Angular 21) per data fetching con stato `{ value, error, status }` reattivo
- `computed()` per derivazioni locali (es. `filteredRecipes`, `reportTotals`)
- Nessun `BehaviorSubject` manuale: tutto via signals

**Routing Strategy: Feature-based Lazy Loading**
```typescript
// app.routes.ts
[
  { path: 'auth', loadChildren: () => import('./features/auth/auth.routes') },
  { path: 'dashboard', loadChildren: () => import('./features/dashboard/dashboard.routes'), canActivate: [authGuard] },
  { path: 'warehouse', loadChildren: () => import('./features/warehouse/warehouse.routes'), canActivate: [authGuard] },
  { path: 'recipes', loadChildren: () => import('./features/recipes/recipes.routes'), canActivate: [authGuard] },
  { path: 'scans', loadChildren: () => import('./features/scans/scans.routes'), canActivate: [authGuard, planGuard('pro')] },
  { path: 'report', loadChildren: () => import('./features/report/report.routes'), canActivate: [authGuard, planGuard('pro')] },
  { path: 'admin', loadChildren: () => import('./features/admin/admin.routes'), canActivate: [authGuard, roleGuard('admin')] },
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
]
```
- `authGuard`: verifica JWT valido
- `planGuard(tier)`: verifica piano sottoscrizione dal JWT claim
- `roleGuard(role)`: verifica ruolo admin dal JWT claim

**PWA & Service Worker:**
- `@angular/pwa` per manifest e service worker
- Caching: shell app (HTML/JS/CSS) in cache — dati sempre online-first
- Nessuna funzionalità offline dati per MVP
- Graceful degradation: UI mostra banner "Connessione assente" se offline, blocca azioni che richiedono server

**Performance:**
- `ChangeDetectionStrategy.OnPush` su tutti i componenti (obbligatorio)
- `@defer` per componenti pesanti non critici (es. grafici statistici post-MVP)
- `NgOptimizedImage` per immagini statiche
- Virtual scroll Angular CDK per liste lunghe (es. magazzino con 500+ ingredienti)

---

### Categoria 5 — Infrastruttura & Deployment

**Containerizzazione: Docker**
- `Dockerfile` per backend (layered Spring Boot image, JVM 21 base)
- `Dockerfile` per frontend (nginx alpine serving Angular build)
- `docker-compose.yml` per sviluppo locale: PostgreSQL 16 + backend + frontend

**CI/CD: GitHub Actions**
```
PR aperta     → lint + test + build (frontend + backend)
Merge su main → build Docker images → push registry → deploy staging auto
Tag v*.*.* release → deploy produzione (con approvazione manuale)
```

**Monitoring & Observability:**
- Spring Boot Actuator: `/actuator/health`, `/actuator/metrics`
- Sentry (backend + frontend): error tracking, no PII/dati sensibili nei payload
- Log strutturati JSON (backend): correlationId per tracciare request cross-layer
- Filtro esplicito: prezzi, margini, P.IVA, nomi fornitori NON compaiono in log

**Backup & Recovery:**
- Backup PostgreSQL giornaliero automatico → object storage EU (S3-compatible)
- Retention backup: 30 giorni
- RTO <4h (conforme NFR PRD)
- Export dati self-service per utente (GDPR data portability)

---

### Decision Impact Analysis

**Sequenza implementazione consigliata (dipendenze):**
1. Schema DB + Flyway migrations + RLS policies
2. Auth service (JWT, Argon2id, refresh token rotation)
3. Tenant middleware (propagazione tenantId dal JWT in ogni request)
4. Ingredient CRUD API + Angular feature module
5. Recipe + MacroRecipe API con eager cost recalculation
6. OCR async pipeline (upload → polling → fallback manuale)
7. Pre-service Report API
8. Admin panel (tenant management, plan assignment)
9. Subscription tier enforcement (planGuard + feature gating API)
10. PWA manifest + service worker

**Dipendenze cross-component critiche:**
- RLS deve essere attivo PRIMA di qualsiasi endpoint che legge dati tenant
- `tenant_id` propagation middleware deve essere il primo filtro nella chain Spring Security
- Eager cost recalculation deve gestire detection cicli prima di ogni altra feature ricette
- OCR Adapter interface deve essere definita prima di implementare qualsiasi provider specifico

---

## Implementation Patterns & Consistency Rules

**Aree di potenziale conflitto tra agenti AI: 8 identificate.**

---

### Naming Patterns — Database

| Elemento | Convenzione | Esempio |
|---|---|---|
| Tabelle | `snake_case` plurale | `ingredients`, `recipe_items`, `scan_results` |
| Colonne | `snake_case` | `tenant_id`, `created_at`, `food_cost_pct` |
| Primary key | `id UUID` sempre | `id UUID PRIMARY KEY DEFAULT gen_random_uuid()` |
| Foreign key | `{entity}_id` | `ingredient_id`, `recipe_id`, `tenant_id` |
| Timestamp audit | `created_at`, `updated_at` | presenti su ogni tabella |
| Indici | `idx_{table}_{column(s)}` | `idx_ingredients_tenant_id` |
| Constraint unique | `uq_{table}_{column(s)}` | `uq_ingredients_tenant_name` |

---

### Naming Patterns — API Endpoints

```
GET    /api/v1/ingredients              → lista ingredienti del tenant
POST   /api/v1/ingredients              → crea ingrediente
GET    /api/v1/ingredients/{id}         → dettaglio ingrediente
PUT    /api/v1/ingredients/{id}         → aggiorna ingrediente (sostituzione completa)
PATCH  /api/v1/ingredients/{id}         → aggiornamento parziale
DELETE /api/v1/ingredients/{id}         → elimina ingrediente

POST   /api/v1/scans                    → avvia OCR scan (async → 202 Accepted)
GET    /api/v1/scans/{id}               → polling stato scan
POST   /api/v1/scans/{id}/confirm       → conferma carrello post-OCR

GET    /api/v1/report/pre-service       → report pre-servizio

POST   /api/v1/auth/login
POST   /api/v1/auth/refresh
POST   /api/v1/auth/logout

GET    /api/v1/admin/tenants
POST   /api/v1/admin/tenants
```

**Regole path:**
- `kebab-case` plurale per resource (es. `/recipe-items`, NON `/recipeItems`)
- Path param: `{id}` sempre UUID, mai integer
- Query param: `camelCase` (es. `?pageSize=20&sortBy=name`)
- Azioni non-CRUD: verbo come sub-resource (es. `/scans/{id}/confirm`, `/ingredients/{id}/archive`)

---

### Naming Patterns — Codice

**Backend (Kotlin):**
- Classi: `PascalCase` — `IngredientService`, `RecipeCostCalculator`, `OcrProvider`
- Funzioni: `camelCase` — `calculateFoodCost()`, `findByTenantId()`
- Package: `com.foodcost.{feature}` — `com.foodcost.recipe`, `com.foodcost.ocr`, `com.foodcost.admin`
- File: `PascalCase.kt` — una classe principale per file
- Costanti: `UPPER_SNAKE_CASE`
- DTO suffix: `{Entity}Dto`, `{Entity}CreateRequest`, `{Entity}UpdateRequest`

**Frontend (Angular/TypeScript):**
- Componenti classe: `PascalCase` con suffisso `Component` — `FoodCostBadgeComponent`
- Selettore Angular: `fc-` prefix + `kebab-case` — `fc-food-cost-badge`
- Services/Stores: `PascalCase` + suffisso `Service` o `Store` — `IngredientService`, `RecipeStore`
- Signals: `camelCase` — `selectedIngredient`, `reportItems`, `isLoading`
- Interfaces/Types: `PascalCase` senza prefisso `I` — `Ingredient`, `RecipeItem`, `ScanResult`
- File: `kebab-case` — `food-cost-badge.component.ts/.html/.scss`
- Route guards: `camelCase` + suffisso `Guard` — `authGuard`, `planGuard`
- Enum values: `UPPER_SNAKE_CASE`

---

### Format Patterns — API Response

**Lista paginata:**
```json
{
  "data": [...],
  "pagination": {
    "page": 1,
    "pageSize": 20,
    "total": 127,
    "totalPages": 7
  }
}
```

**Risorsa singola:** JSON diretto (nessun wrapper `data`):
```json
{ "id": "uuid", "name": "Tonno rosso", "pricePerKg": 42.50, "createdAt": "2026-03-13T14:30:00Z" }
```

**Errore (RFC 7807):**
```json
{
  "type": "https://foodcost.app/errors/not-found",
  "title": "Resource not found",
  "status": 404,
  "detail": "Ingredient 'abc-123' not found in your warehouse",
  "instance": "/api/v1/ingredients/abc-123"
}
```

**OCR async (202 Accepted):**
```json
{ "scanId": "uuid", "status": "processing", "pollIntervalMs": 3000 }
```

**Regole formato:**
- **JSON field naming:** `camelCase` in TUTTI i body (request e response) — `tenantId`, `createdAt`, `foodCostPct`
- **Date/time:** ISO 8601 UTC sempre — `"2026-03-13T14:30:00Z"` (mai timestamp Unix)
- **Decimali monetari:** `number` JSON con 2 cifre decimali max (es. `42.50`) — mai stringa
- **UUID:** stringa lowercase con trattini — `"550e8400-e29b-41d4-a716-446655440000"`
- **Boolean:** `true`/`false` — mai `1`/`0` o stringhe
- **Null vs assente:** usare `null` esplicito, mai omettere il campo (salvo paginazione opzionale)

---

### Structure Patterns — Test

**Backend (Kotlin):**
- Unit test nella stessa struttura del main: `src/test/kotlin/com/foodcost/{feature}/`
- Naming: `{ClassName}Test.kt` — `RecipeCostCalculatorTest.kt`, `IngredientServiceTest.kt`
- Integration test: `@SpringBootTest` in `src/test/kotlin/com/foodcost/integration/`
- MockK per mocking (NON Mockito — Kotlin-native)
- Ogni service ha almeno un test unitario; ogni endpoint almeno un integration test

**Frontend (Angular):**
- Spec co-locati con il component: `feature.component.spec.ts` nella stessa cartella
- Naming: `{component-name}.component.spec.ts`
- Store/Service test: `{name}.service.spec.ts` co-locato
- Nessuna cartella `__tests__/` separata

---

### Process Patterns — Loading States (Frontend)

```typescript
// PATTERN OBBLIGATORIO per data fetching — sempre rxResource
protected readonly ingredients = rxResource({
  loader: () => this.ingredientService.getAll()
});

// TEMPLATE — sempre questi tre stati espliciti
@if (ingredients.isLoading()) {
  <fc-skeleton />
} @else if (ingredients.error()) {
  <fc-error-message [error]="ingredients.error()" />
} @else {
  @for (item of ingredients.value(); track item.id) {
    <fc-ingredient-card [ingredient]="item" />
  }
}
```

**Feedback azioni utente:**
- Conferma operazione completata: `MatSnackBar` 3s, tono neutro-informativo
- Esempio: `"Magazzino aggiornato — 8 prodotti modificati"` (NON `"Ottimo lavoro! ✅"`)
- Errori bloccanti: componente `fc-error-message` inline con messaggio RFC 7807 `detail`
- Loading inline (azioni puntuali): `MatProgressSpinner` o skeleton, MAI bloccare tutta la UI

---

### Process Patterns — Tenant Context (Backend)

```kotlin
// ✅ CORRETTO — tenantId estratto dal SecurityContext
@GetMapping("/ingredients")
fun list(): List<IngredientDto> {
    val tenantId = TenantContext.current() // da SecurityContext, mai dalla request
    return ingredientService.findAll(tenantId)
}

// ❌ SBAGLIATO — mai accettare tenantId dall'esterno
@GetMapping("/ingredients")
fun list(@RequestParam tenantId: UUID): List<IngredientDto> { ... }

// ❌ SBAGLIATO — mai accettarlo nel body
data class CreateIngredientRequest(val tenantId: UUID, val name: String, ...)
```

**Regola assoluta:** `tenant_id` non è mai un parametro accettato dall'utente. È sempre iniettato dal server dal JWT verificato.

---

### Process Patterns — Cost Graph Recalculation (Backend)

```kotlin
// Ogni modifica a un prezzo ingrediente attiva questo flusso
fun updateIngredientPrice(id: UUID, newPrice: BigDecimal, tenantId: UUID) {
    // 1. Aggiorna ingrediente
    // 2. Trova TUTTE le ricette che usano questo ingrediente (direttamente o tramite macro-ricette)
    // 3. Ricalcola in ordine topologico (foglie → radice)
    // 4. Salva tutti i calculated_cost aggiornati in una singola transazione
    // → TUTTO in @Transactional: atomi o niente
}

// Detection cicli: obbligatoria PRIMA di salvare qualsiasi relazione ricetta
fun validateNoCycle(recipeId: UUID, ingredientId: UUID, tenantId: UUID) {
    // BFS/DFS: se ingredientId è esso stesso una macro-ricetta che dipende da recipeId → errore 422
}
```

---

### Enforcement Guidelines

**Tutti gli agenti AI DEVONO:**

1. Usare `snake_case` per ogni nome di tabella e colonna PostgreSQL
2. Usare `camelCase` per ogni campo JSON in request/response
3. Restituire errori nel formato RFC 7807 `application/problem+json`
4. Estrarre `tenant_id` SOLO dal SecurityContext — mai da input utente
5. Avvolgere ogni recalculation del grafo in `@Transactional`
6. Usare `rxResource` + pattern `isLoading/error/value` nel frontend
7. Usare MockK (non Mockito) per i test Kotlin
8. Prefissare ogni selettore Angular con `fc-`
9. Usare `ChangeDetectionStrategy.OnPush` su ogni componente Angular
10. Non loggare mai prezzi, margini, P.IVA, nomi fornitori in log/telemetria

**Anti-pattern espliciti da evitare:**

- ❌ `tenantId` in `@RequestParam`, `@PathVariable`, o request body
- ❌ `NgRx`, `BehaviorSubject` manuali, `*ngIf`/`*ngFor` nei template
- ❌ `ngClass` / `ngStyle` nei template Angular
- ❌ Mockito nei test Kotlin (usa MockK)
- ❌ Calcolo del food cost fuori dalla transazione del recalculation graph
- ❌ Date come timestamp numerico Unix nel JSON
- ❌ `standalone: true` esplicito nei decoratori Angular (è default in v21)
- ❌ Constructor injection in Angular (usa `inject()`)

---

## Project Structure & Boundaries

### Requirements → Structure Mapping

| FR Category | Backend package | Frontend feature |
|---|---|---|
| Auth & Account (FR1-6) | `com.foodcost.auth` | `features/auth/` |
| Magazzino (FR7-11) | `com.foodcost.ingredient` | `features/warehouse/` |
| OCR Bolla (FR12-17) | `com.foodcost.ocr` | `features/scans/` |
| Ricette & Macro-Ricette (FR18-23) | `com.foodcost.recipe` | `features/recipes/` |
| Report Pre-Servizio (FR24-27) | `com.foodcost.report` | `features/report/` |
| Onboarding (FR28-30) | (integrato in auth + notification) | `features/onboarding/` |
| Admin Panel (FR31-34) | `com.foodcost.admin` | `features/admin/` |

---

### Monorepo Root Structure

```
foodcost/                          ← root workspace (questo repository)
├── README.md
├── docker-compose.yml             ← sviluppo locale: PostgreSQL + backend + frontend proxy
├── docker-compose.prod.yml
├── .github/
│   └── workflows/
│       ├── ci.yml
│       └── deploy.yml
├── apps/
│   ├── backend/                   ← Spring Boot Kotlin
│   └── frontend/                  ← Angular 21 PWA
└── docs/                          ← documentazione progetto
```

---

### Backend Directory Structure — `apps/backend/`

```
apps/backend/
├── build.gradle.kts
├── settings.gradle.kts
├── Dockerfile
├── README.md
├── src/
│   ├── main/
│   │   ├── kotlin/com/foodcost/
│   │   │   ├── FoodCostApplication.kt
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.kt
│   │   │   │   ├── WebConfig.kt
│   │   │   │   ├── FlywayConfig.kt
│   │   │   │   └── OpenApiConfig.kt
│   │   │   ├── common/
│   │   │   │   ├── TenantContext.kt
│   │   │   │   ├── TenantInterceptor.kt
│   │   │   │   ├── GlobalExceptionHandler.kt
│   │   │   │   ├── ProblemDetailResponse.kt
│   │   │   │   └── PageResponse.kt
│   │   │   ├── auth/
│   │   │   │   ├── AuthController.kt
│   │   │   │   ├── AuthService.kt
│   │   │   │   ├── JwtService.kt
│   │   │   │   ├── RefreshTokenService.kt
│   │   │   │   ├── UserRepository.kt
│   │   │   │   ├── model/
│   │   │   │   │   ├── User.kt
│   │   │   │   │   └── RefreshToken.kt
│   │   │   │   └── dto/
│   │   │   │       ├── LoginRequest.kt
│   │   │   │       ├── LoginResponse.kt
│   │   │   │       └── RefreshRequest.kt
│   │   │   ├── tenant/
│   │   │   │   ├── Tenant.kt
│   │   │   │   └── TenantRepository.kt
│   │   │   ├── ingredient/
│   │   │   │   ├── IngredientController.kt
│   │   │   │   ├── IngredientService.kt
│   │   │   │   ├── IngredientRepository.kt
│   │   │   │   ├── model/
│   │   │   │   │   └── Ingredient.kt
│   │   │   │   └── dto/
│   │   │   │       ├── IngredientDto.kt
│   │   │   │       ├── IngredientCreateRequest.kt
│   │   │   │       └── IngredientUpdateRequest.kt
│   │   │   ├── recipe/
│   │   │   │   ├── RecipeController.kt
│   │   │   │   ├── RecipeService.kt
│   │   │   │   ├── RecipeCostCalculator.kt
│   │   │   │   ├── RecipeCycleValidator.kt
│   │   │   │   ├── RecipeRepository.kt
│   │   │   │   ├── RecipeItemRepository.kt
│   │   │   │   ├── model/
│   │   │   │   │   ├── Recipe.kt
│   │   │   │   │   └── RecipeItem.kt
│   │   │   │   └── dto/
│   │   │   │       ├── RecipeDto.kt
│   │   │   │       ├── RecipeCreateRequest.kt
│   │   │   │       └── RecipeItemDto.kt
│   │   │   ├── ocr/
│   │   │   │   ├── OcrController.kt
│   │   │   │   ├── OcrService.kt
│   │   │   │   ├── OcrProvider.kt              ← interfaccia adapter
│   │   │   │   ├── OcrJobProcessor.kt
│   │   │   │   ├── ScanRepository.kt
│   │   │   │   ├── providers/
│   │   │   │   │   └── HttpOcrProvider.kt       ← implementazione concreta
│   │   │   │   ├── model/
│   │   │   │   │   ├── Scan.kt
│   │   │   │   │   └── ScanItem.kt
│   │   │   │   └── dto/
│   │   │   │       ├── ScanResponse.kt
│   │   │   │       └── ScanConfirmRequest.kt
│   │   │   ├── report/
│   │   │   │   ├── ReportController.kt
│   │   │   │   ├── ReportService.kt
│   │   │   │   └── dto/
│   │   │   │       └── PreServiceReportDto.kt
│   │   │   ├── notification/
│   │   │   │   ├── EmailService.kt
│   │   │   │   └── NotificationScheduler.kt
│   │   │   ├── subscription/
│   │   │   │   ├── SubscriptionService.kt
│   │   │   │   ├── PlanGuard.kt
│   │   │   │   └── model/
│   │   │   │       └── SubscriptionPlan.kt
│   │   │   └── admin/
│   │   │       ├── AdminController.kt
│   │   │       ├── AdminService.kt
│   │   │       └── dto/
│   │   │           ├── TenantAdminDto.kt
│   │   │           └── TenantCreateRequest.kt
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── db/migration/
│   │           ├── V1__create_tenants_users.sql
│   │           ├── V2__create_ingredients.sql
│   │           ├── V3__create_recipes.sql
│   │           ├── V4__create_scans.sql
│   │           ├── V5__create_subscriptions.sql
│   │           └── V6__enable_rls_policies.sql
│   └── test/
│       └── kotlin/com/foodcost/
│           ├── integration/
│           │   ├── IngredientIntegrationTest.kt
│           │   ├── RecipeIntegrationTest.kt
│           │   └── OcrIntegrationTest.kt
│           ├── ingredient/
│           │   └── IngredientServiceTest.kt
│           ├── recipe/
│           │   ├── RecipeCostCalculatorTest.kt
│           │   └── RecipeCycleValidatorTest.kt
│           └── auth/
│               └── JwtServiceTest.kt
```

---

### Frontend Directory Structure — `apps/frontend/`

```
apps/frontend/
├── package.json
├── angular.json
├── tsconfig.json
├── tsconfig.app.json
├── tailwind.config.ts
├── Dockerfile
├── nginx.conf
├── README.md
├── src/
│   ├── main.ts
│   ├── index.html
│   ├── styles.scss                      ← Tailwind base + Angular Material M3 theme
│   ├── manifest.webmanifest
│   ├── app/
│   │   ├── app.config.ts                ← bootstrapApplication config
│   │   ├── app.routes.ts                ← root lazy routes
│   │   ├── core/
│   │   │   ├── auth/
│   │   │   │   ├── auth.service.ts
│   │   │   │   ├── auth.guard.ts
│   │   │   │   ├── plan.guard.ts
│   │   │   │   ├── role.guard.ts
│   │   │   │   └── jwt.interceptor.ts
│   │   │   ├── tenant/
│   │   │   │   └── tenant.service.ts
│   │   │   ├── api/
│   │   │   │   └── api.service.ts        ← HttpClient wrapper base
│   │   │   └── error/
│   │   │       └── error-handler.service.ts
│   │   ├── shared/
│   │   │   ├── components/
│   │   │   │   ├── fc-skeleton/
│   │   │   │   │   ├── skeleton.component.ts
│   │   │   │   │   ├── skeleton.component.html
│   │   │   │   │   └── skeleton.component.scss
│   │   │   │   ├── fc-error-message/
│   │   │   │   │   ├── error-message.component.ts
│   │   │   │   │   ├── error-message.component.html
│   │   │   │   │   └── error-message.component.scss
│   │   │   │   ├── fc-food-cost-badge/
│   │   │   │   │   ├── food-cost-badge.component.ts
│   │   │   │   │   ├── food-cost-badge.component.html
│   │   │   │   │   └── food-cost-badge.component.scss
│   │   │   │   └── fc-confirm-dialog/
│   │   │   │       ├── confirm-dialog.component.ts
│   │   │   │       └── confirm-dialog.component.html
│   │   │   ├── pipes/
│   │   │   │   └── food-cost-color.pipe.ts
│   │   │   └── models/
│   │   │       ├── ingredient.model.ts
│   │   │       ├── recipe.model.ts
│   │   │       ├── scan.model.ts
│   │   │       ├── report.model.ts
│   │   │       └── pagination.model.ts
│   │   └── features/
│   │       ├── auth/
│   │       │   ├── auth.routes.ts
│   │       │   ├── login/
│   │       │   │   ├── login.component.ts
│   │       │   │   ├── login.component.html
│   │       │   │   └── login.component.scss
│   │       │   └── register/
│   │       │       ├── register.component.ts
│   │       │       ├── register.component.html
│   │       │       └── register.component.scss
│   │       ├── onboarding/
│   │       │   ├── onboarding.routes.ts
│   │       │   └── steps/
│   │       │       ├── step-ingredients.component.ts
│   │       │       ├── step-recipe.component.ts
│   │       │       └── step-report.component.ts
│   │       ├── dashboard/
│   │       │   ├── dashboard.routes.ts
│   │       │   ├── dashboard.component.ts
│   │       │   ├── dashboard.component.html
│   │       │   ├── dashboard.component.scss
│   │       │   └── dashboard.store.ts
│   │       ├── warehouse/
│   │       │   ├── warehouse.routes.ts
│   │       │   ├── warehouse.store.ts
│   │       │   ├── ingredient-list/
│   │       │   │   ├── ingredient-list.component.ts
│   │       │   │   ├── ingredient-list.component.html
│   │       │   │   └── ingredient-list.component.scss
│   │       │   ├── ingredient-form/
│   │       │   │   ├── ingredient-form.component.ts
│   │       │   │   ├── ingredient-form.component.html
│   │       │   │   └── ingredient-form.component.scss
│   │       │   └── ingredient-detail/
│   │       │       ├── ingredient-detail.component.ts
│   │       │       ├── ingredient-detail.component.html
│   │       │       └── ingredient-detail.component.scss
│   │       ├── recipes/
│   │       │   ├── recipes.routes.ts
│   │       │   ├── recipes.store.ts
│   │       │   ├── recipe-list/
│   │       │   ├── recipe-form/
│   │       │   └── recipe-detail/
│   │       ├── scans/
│   │       │   ├── scans.routes.ts
│   │       │   ├── scan.service.ts           ← polling logic
│   │       │   ├── scan-camera/
│   │       │   │   ├── scan-camera.component.ts
│   │       │   │   ├── scan-camera.component.html
│   │       │   │   └── scan-camera.component.scss
│   │       │   └── scan-review/
│   │       │       ├── scan-review.component.ts       ← carrello revisione bolla
│   │       │       ├── scan-review.component.html
│   │       │       └── scan-review.component.scss
│   │       ├── report/
│   │       │   ├── report.routes.ts
│   │       │   ├── report.store.ts
│   │       │   ├── pre-service-report.component.ts
│   │       │   ├── pre-service-report.component.html
│   │       │   └── pre-service-report.component.scss
│   │       └── admin/
│   │           ├── admin.routes.ts
│   │           ├── tenant-list/
│   │           │   ├── tenant-list.component.ts
│   │           │   ├── tenant-list.component.html
│   │           │   └── tenant-list.component.scss
│   │           └── tenant-detail/
│   │               ├── tenant-detail.component.ts
│   │               ├── tenant-detail.component.html
│   │               └── tenant-detail.component.scss
│   └── environments/
│       ├── environment.ts
│       └── environment.prod.ts
```

---

### Architectural Boundaries

**API Boundaries (HTTP):**
- Frontend → Backend: tutte le chiamate su `/api/v1/*` via `jwt.interceptor.ts` (aggiunge `Authorization: Bearer`)
- Backend → OCR Provider: via `OcrProvider` interface, chiamata HTTP interna (sostituibile senza modifiche al core)
- Backend → Email Provider: via `EmailService`, chiamata HTTP/SMTP provider esterno
- Admin portal: stesso backend, routes protette da `@PreAuthorize("hasRole('ADMIN')")`

**Monorepo Boundary:**
- `apps/backend/` e `apps/frontend/` sono applicazioni indipendenti con propri `build.gradle.kts` / `package.json`
- Nessuna dipendenza di codice diretta tra le due app — comunicano solo via HTTP API
- `docker-compose.yml` e CI/CD risiedono nella root e orchestrano entrambe
- Nessun tooling monorepo aggiuntivo (Nx, Turborepo) per MVP — semplice struttura `apps/` con script root

**Component Boundaries (Frontend):**
- Ogni feature module ha il proprio `*.store.ts` — nessun accesso diretto allo store di un'altra feature
- Le features comunicano via router navigation o via shared services in `core/`
- `fc-food-cost-badge` in `shared/` è l'unico punto di rendering del food cost % in tutta l'app
- `core/auth/` fornisce guards e interceptor a tutte le features — non è una feature navigabile

**Service Boundaries (Backend):**
- Ogni package-by-feature espone solo Controller e DTO al layer HTTP
- I Service accedono solo al proprio Repository — mai direttamente al Repository di un altro package
- Cross-feature dependency (es. `RecipeCostCalculator` ha bisogno di `IngredientRepository`): via DI, documentata esplicitamente
- `common/` contiene solo infrastruttura cross-cutting (tenant, error handling, paginazione)

**Data Boundaries:**
- Ogni tabella ha `tenant_id` — RLS policy attiva sempre
- Nessun JOIN cross-tenant possibile (PostgreSQL RLS garantisce isolamento a livello query planner)
- Immagini bolle: storage temporaneo (filesystem o object storage), TTL 30 giorni, mai nel DB
- Flyway migrations in ordine stretto `V{n}__` — nessuna migrazione fuori sequenza

---

### Data Flow — Flusso Principale OCR

```
[Camera device]
    → ScanCameraComponent (scatta foto)
    → scan.service.ts → POST /api/v1/scans (multipart/form-data)
    → OcrController → OcrService → salva Scan record (status: processing)
    → OcrJobProcessor (async) → OcrProvider.extract(image)
    → aggiorna Scan record con items + confidence scores

[Frontend polling]
    → scan.service.ts → GET /api/v1/scans/{id} (ogni 3s, max 20x)
    → status: completed → ScanReviewComponent (carrello revisione)
    → utente rivede, corregge inline, conferma
    → POST /api/v1/scans/{id}/confirm
    → OcrService → IngredientService.bulkUpdate(items, tenantId)
    → RecipeCostCalculator.recalculateAffected(updatedIngredientIds, tenantId)
    → @Transactional: tutto il grafo ricalcolato e salvato atomicamente
    → 200 OK → snackbar "Magazzino aggiornato — N prodotti modificati"
```

---

### Development Workflow

**Sviluppo locale (dalla root del monorepo):**
```bash
# Terminal 1 — PostgreSQL + backend
docker-compose up -d db          # PostgreSQL 16
cd apps/backend
./gradlew bootRun                  # Spring Boot su :8080

# Terminal 2 — frontend
cd apps/frontend
ng serve --proxy-config proxy.conf.json   # Angular su :4200, proxy /api → :8080
```

**Build produzione:**
- Backend: `cd apps/backend && ./gradlew bootJar` → Docker layered image (`eclipse-temurin:21-jre-alpine`)
- Frontend: `cd apps/frontend && ng build --configuration=production` → Docker nginx alpine serving static files

**Deployment:**
- `docker-compose.prod.yml` nella root del monorepo con backend + frontend + PostgreSQL (o managed DB)
- CI/CD: GitHub Actions nella root `.github/workflows/` — unico pipeline per entrambe le app

---

## Architecture Validation Results

### Coherence Validation ✅

**Decision Compatibility:**
Angular 21 + Tailwind CSS v4 + Angular Material M3: compatibili, nessun conflitto. Kotlin + Spring Boot + Gradle Kotlin DSL + PostgreSQL 16: stack collaudato. JWT access/refresh + Argon2id + Spring Security: chain completa. Flyway + RLS PostgreSQL: migration creano tabelle e RLS nello stesso flusso. OCR adapter pattern indipendente dal provider.

**Pattern Consistency:**
`snake_case` DB ↔ `camelCase` JSON ↔ pattern Kotlin/Angular coerenti. RFC 7807 usato sia in `GlobalExceptionHandler` backend che in `fc-error-message` frontend. `rxResource` pattern in tutti i feature store. Selettore `fc-` uniforme.

**Structure Alignment:**
Package-by-feature backend mappa 1:1 con categorie FR. Feature-based lazy routing frontend mappa 1:1 con feature module. `core/` contiene solo infrastruttura cross-cutting. Test co-locati in entrambi i repo.

---

### Requirements Coverage Validation ✅

**Functional Requirements (34/34 coperti):**

| FR | Descrizione | Copertura |
|---|---|---|
| FR1-3 | Auth (registrazione, login, reset pwd) | `auth/` BE + FE |
| FR4-6 | Trial, piani, upgrade | `subscription/` BE + `plan.guard` FE |
| FR7-11 | CRUD ingredienti + propagazione costo | `ingredient/` BE + `warehouse/` FE + `RecipeCostCalculator` |
| FR12-17 | OCR pipeline completa | `ocr/` BE (async + adapter + polling) + `scans/` FE |
| FR18-23 | Ricette, macro-ricette, food cost % | `recipe/` BE (+ cycle validator) + `recipes/` FE |
| FR24-27 | Report pre-servizio | `report/` BE + FE |
| FR28-29 | Onboarding guidato | `onboarding/` FE (usa API esistenti) |
| FR30 | Notifica reminder pre-servizio | `notification/NotificationScheduler.kt` |
| FR31-34 | Panel admin interno | `admin/` BE + FE, protetto da `roleGuard('admin')` |

**Non-Functional Requirements (15/15 coperti):**

| NFR | Requisito | Copertura |
|---|---|---|
| Performance <2s | Costo materializzato, nessun calcolo on-demand |
| Report <3s | `calculated_cost` pre-calcolato |
| OCR <60s | Polling 3s × 20 max, timeout provider 30s |
| Dashboard <1.5s | PWA cache shell + lazy loading |
| AES-256 at-rest | Documentato in decisioni security |
| TLS 1.2+ | Documentato |
| Argon2id | Selezionato esplicitamente |
| JWT persistent mobile | Refresh token 30gg con rotation |
| 300 sessioni concorrenti | Spring Boot stateless + connection pool |
| 100→10k tenant | RLS single-schema |
| Ricorsività 5 livelli | BFS/DFS con cycle detection |
| OCR sostituibile | `OcrProvider` interface + adapter |
| Email ≥98% | `EmailService` astratto |
| GDPR data residency | EU cloud obbligatorio, retention 90gg |
| Backup RTO <4h | Backup giornaliero documentato |

---

### Implementation Readiness Validation ✅

**Decision Completeness:** Tutte le decisioni critiche documentate con rationale. Stack completo specificato. Pattern di integrazione definiti (OCR async, polling, adapter, cost graph).

**Structure Completeness:** Struttura directory completa con ogni file per entrambi i repo. Mapping FR → package/feature esplicito. Confini chiari tra feature module.

**Pattern Completeness:** Naming completo (DB, API, codice). Format API con esempi concreti. Process pattern con code snippet (tenant context, loading states, cost graph). 10 enforcement rules per agenti AI.

---

### Gap Analysis

**Gap critici: 0** 🟢

**Gap importanti (non bloccanti, da affinare nelle stories):**
1. **Schema ER database non dettagliato** — struttura tabelle implicita, nessun diagramma ER formale. Creare come primo passo implementativo.
2. **OCR provider concreto TBD** — interfaccia definita, provider specifico intenzionalmente non scelto. Adapter pattern lo consente per design.
3. **Flusso Stripe non dettagliato** — menzionato come dipendenza, `subscription/` presente, ma webhook/payment flow non specificato. Post-MVP per trial→paid.

**Gap nice-to-have:**
- Diagramma di deployment (Docker topology produzione)
- Seed data per ambiente sviluppo
- Strategia logging strutturato (formato JSON, livelli per ambiente)

---

### Architecture Completeness Checklist

**✅ Requirements Analysis**
- [x] Contesto progettuale analizzato (34 FR, 7 categorie)
- [x] Scala e complessità valutate (Medium-High)
- [x] Vincoli tecnici identificati (stack, EU hosting, GDPR)
- [x] Cross-cutting concerns mappati (6 identificati)

**✅ Architectural Decisions**
- [x] Decisioni critiche documentate con rationale
- [x] Stack tecnologico specificato (Angular 21, Kotlin Spring Boot, PostgreSQL 16)
- [x] Pattern integrazione definiti (OCR async, polling, adapter)
- [x] Performance considerations indirizzate (eager cost materialization)

**✅ Implementation Patterns**
- [x] Convenzioni naming stabilite (DB, API, codice)
- [x] Pattern strutturali definiti (test co-locati, feature-based)
- [x] Pattern comunicazione specificati (RFC 7807, JSON camelCase, ISO 8601)
- [x] Pattern processo documentati (loading states, tenant context, cost graph)

**✅ Project Structure**
- [x] Struttura directory completa (backend + frontend)
- [x] Confini componenti stabiliti
- [x] Punti integrazione mappati
- [x] Mapping requirements → structure completo

**✅ Developer Guidelines**
- [x] Angular 21 dev guide con best practices, signals, control flow
- [x] Anti-pattern espliciti da evitare
- [x] 10 enforcement rules per agenti AI

---

### Architecture Readiness Assessment

**Overall Status:** ✅ READY FOR IMPLEMENTATION

**Confidence Level:** Alta

**Punti di forza:**
- Stack vincolato dal PRD elimina ambiguità
- Multi-tenancy RLS + tenant context pattern impediscono data leaking cross-tenant
- Eager cost recalculation con cycle detection garantisce consistenza
- OCR adapter pattern consente cambio provider senza impatto
- Angular 21 guidelines + enforcement rules rendono il codice prevedibile per agenti AI

**Aree da dettagliare in fase di implementazione:**
- Schema ER completo tabelle PostgreSQL
- Scelta e configurazione OCR provider concreto
- Flusso Stripe (webhook, trial expiry, upgrade)
- Seed data per sviluppo

### Implementation Handoff

**AI Agent Guidelines:**
- Seguire tutte le decisioni architetturali esattamente come documentate
- Usare i pattern di implementazione in modo consistente su tutti i componenti
- Rispettare la struttura del progetto e i confini definiti
- Riferirsi a questo documento per ogni decisione architetturale

**Prima priorità implementativa:**
1. Inizializzazione repository con starter commands documentati
2. Schema DB + Flyway migrations + RLS policies
3. Auth service (JWT, Argon2id, refresh token rotation)
4. Tenant middleware + primo endpoint CRUD (ingredients)
