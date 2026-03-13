# Food Cost SaaS — UX Design Session Notes

**Workspace:** `c:\Users\nam\workspace\gilde\signore_degli_agenti\BMAD`
**Session date:** 2026-03-13
**Workflow completed:** `create-ux-design` (14/14 steps)

---

## Key Artifacts

- **UX Spec:** `_bmad-output/planning-artifacts/ux-design-specification.md` — `workflowStatus: complete`
- **Design Directions HTML:** `_bmad-output/planning-artifacts/ux-design-directions.html` — 6 mockup interattivi
- **PRD:** `_bmad-output/planning-artifacts/prd.md` — `workflowStatus: complete`
- **Research:** `_bmad-output/planning-artifacts/research/market-food-cost-webapp-restaurants-research-2026-03-03.md`
- **Brainstorming:** `_bmad-output/brainstorming/brainstorming-session-2026-03-03-T1430.md`

---

## Decisioni Tecniche

- **Framework:** Angular (SPA/PWA) + Angular Material (M3 theming) + Tailwind CSS
- **NON usare:** Shadcn/UI (React-only), Spartan UI — utente ha scelto esplicitamente AM + Tailwind
- **Backend:** Kotlin (Spring Boot o Ktor), PostgreSQL, hosting EU
- **Design system:** Angular Material per componenti base, Tailwind per layout/spacing/colori/breakpoint

## Design Direction Scelta

- **Mobile (< 768px):** Direction 1 "Comando" — bottom nav persistente + FAB OCR centrato + card KPI
- **Desktop (≥ 768px):** Direction 6 "Zero" — topbar nav + tabular list densa + scan button in topbar
- **Breakpoint unico:** 768px — sotto = layout mobile, sopra = layout desktop
- **Single breakpoint philosophy:** solo prefisso `md:` in Tailwind, nessun `sm:`, `lg:`, `xl:` in MVP

## Tema Visivo — "Nero di Cucina"

- Dark mode only — nessun light mode in MVP
- `bg-base: #111827`, `bg-surface: #1F2937`, `bg-elevated: #374151`
- `accent/success: #22C55E`, `warning: #F59E0B`, `danger: #EF4444`
- `text-primary: #F9FAFB`, `text-secondary: #9CA3AF`
- Font: Inter (Google Fonts), `tabular-nums` su tutti i valori numerici

## Flusso OCR — Variante A (scelta dall'utente)

- Revisione esplicita obbligatoria di tutti gli item prima della conferma
- Confidence < 90% → item in amber warning + editing inline
- Snackbar 4s post-conferma: "N prodotti aggiornati · N ricette ricalcolate"
- Ricalcolo food cost in background silenzioso, no loading indicator

## Componenti Custom Richiesti

1. `FoodCostBadge` — % + threshold logic tricolore (verde/ambra/rosso), `OnPush`
2. `OcrCartItem` — riga carrello revisione, editing inline, stato confidence
3. `RecipeCardCompact` — card mobile lista ricette con food cost badge
4. `KpiTile` — metric tile dashboard, varianti primary/neutral/alert
5. `OnboardingTracker` — progress display 3-step per admin (non mat-stepper)
6. `MacroRecipeBadge` — pillola "M" verde distingue macro-ricette in search

## Regole UX Critiche

- **Dashboard fissa:** stessa indipendentemente dall'orario — nessuna logica situazionale
- **FAB OCR:** riservato SOLO al gesto OCR scan — mai usare mat-fab per altro
- **1 bottone primario per schermata** — mai più di uno
- **Errori OCR in ambra, non rosso** — non è un fallimento
- **Nessuna celebrazione del banale** — toast sobri, no emoji, no "Ottimo lavoro!"
- **Food cost % sempre visibile** — mai nascosto dietro "Vedi di più"
- **Modal solo per azioni distruttive** — snackbar 4s per i successi
- **Validazione form:** on-submit per form multi-campo, on-blur per editing inline

## Accessibility — WCAG 2.1 AA

- Tutti i tap target ≥ 44×44px (FAB 56×56px)
- `FoodCostBadge`: mai solo colore — sempre valore numerico + icona
- `aria-label` su tutti i bottoni icon-only
- Skip link in `app.component.html`
- `@angular-eslint/template-accessibility` abilitato al build

## Tono Comunicativo (UI copy)

- Neutro-informativo sugli errori: "3 prodotti non riconosciuti — revisione richiesta"
- Mai: "Oops!", punti esclamativi, emoji decorative
- Feedback errore: descrittivo, nessuna colpa — "Inserisci un prezzo valido (es. 4.50)"

## Journey Principali

1. OCR Bolla Happy Path: scan → carrello revisione → conferma → snackbar → dashboard
2. OCR Bolla Edge Case: item amber → editing inline → conferma → learn pattern fornitore
3. Report Pre-Servizio: nav Report → lista piatti ordinata per food cost % desc → tap anomalia → modifica ricetta
4. Gestione Ricetta + Macro-Ricette: search unificata ingredienti + macro (badge M) → food cost % real-time in footer
5. Admin Onboarding Tenant: crea tenant → email attivazione → funnel 3 step

## Prossimi Step Raccomandati

- **Create Architecture** — Winston (Architect agent)
- **Epic Creation** — Bob (SM agent)
- **Wireframe / Prototype** — da definire
