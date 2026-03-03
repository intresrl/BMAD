---
stepsCompleted: ['step-01-init', 'step-02-discovery', 'step-02b-vision', 'step-02c-executive-summary', 'step-03-success', 'step-04-journeys', 'step-05-domain', 'step-06-innovation', 'step-07-project-type', 'step-08-scoping', 'step-09-functional', 'step-10-nonfunctional', 'step-11-polish', 'step-12-complete']
workflowStatus: 'complete'
completedAt: '2026-03-03'
inputDocuments:
  - '_bmad-output/planning-artifacts/research/market-food-cost-webapp-restaurants-research-2026-03-03.md'
  - '_bmad-output/brainstorming/brainstorming-session-2026-03-03-T1430.md'
workflowType: 'prd'
project_name: BMAD
user_name: Intre
date: '2026-03-03'
documentCounts:
  briefs: 0
  research: 1
  brainstorming: 1
  projectDocs: 0
classification:
  projectType: 'SaaS SMB Web App (mobile-first PWA)'
  domain: 'Food Service / SMB Operations + HACCP Compliance'
  complexity: 'Medium-High'
  projectContext: 'greenfield'
  market: 'Italia (MVP) - architettura ready per EU expansion'
---

# Product Requirements Document — Food Cost SaaS

> **Nota:** Nome prodotto finale da definire. Placeholder: *FoodCost App*.

**Author:** Intre
**Date:** 2026-03-03

---

## Executive Summary

Web app SaaS mobile-first per ristoratori italiani singoli che restituisce controllo finanziario operativo a chi lavora 14+ ore al giorno senza visibilità sui margini. Il prodotto risolve tre problemi interconnessi: impossibilità di calcolare il costo reale di un piatto (ingredienti + energia + manodopera), mancanza di aggiornamento automatico dei prezzi alla ricezione della merce, e assenza di compliance HACCP digitalizzata accessibile a piccole realtà. Target primario: chef-imprenditori e gestori di ristoranti singoli nel mercato italiano (≈330.000 strutture), in particolare segmento fine dining / medio-alto dove la pressione sui margini è massima e la professionalizzazione è in crescita. Modello di business: SaaS multi-tier (Base / Pro / Premium) con pricing mensile accessibile, progettato per scalare verso EU senza re-architetture significative.

### What Makes This Special

La differenziazione primaria è l'**OCR bolla fornitore via fotocamera mobile**: il ristoratore fotografa la bolla di consegna in cucina e prezzi + quantità si aggiornano automaticamente nel magazzino digitale — zero inserimento manuale, zero disallineamento tra prezzi teorici e reali pagati. Feature secondaria strutturale: le **macro-ricette come ingredienti** (fondo bruno, salse base, impasti) permettono di calcolare il costo reale di preparazioni complesse con precisione industriale, non approssimazioni. Il posizionamento *"pensato da chi lavora in cucina, non da un commercialista"* elimina la distanza cultura-strumento che ha reso inutilizzabili le soluzioni esistenti. La compliance HACCP, normalmente gestita con carta o consulenti esterni, diventa automatica e integrata nel flusso di lavoro quotidiano.

## Project Classification

| Dimensione | Valore |
|---|---|
| **Project Type** | SaaS SMB Web App (mobile-first PWA) |
| **Domain** | Food Service / SMB Operations + HACCP Compliance |
| **Complexity** | Medium-High |
| **Project Context** | Greenfield |
| **Mercato MVP** | Italia — architettura ready per espansione EU |

---

## Success Criteria

### Journey 1: Matteo — Il Primo Controllo Vero (Happy Path)

**Persona:** Matteo, 27 anni. Chef-proprietario di un piccolo fine dining mediterraneo a Milano. La cucina mescola materie prime locali con influenze giapponesi e nordafricane — significa 3-4 fornitori diversi, ingredienti stagionali non standard, e preparazioni base elaborate (dashi, chermoula, bisque). Usa WhatsApp, Instagram e Google Maps senza problemi. Non ha mai usato un gestionale.

**Opening Scene — Il Dolore:**
Sono le 22:30, fine servizio. Matteo ha appena chiuso una serata da 30 coperti. Si siede con un bicchiere di vino e un quaderno a spirale sgualcito. Deve aggiornare i prezzi del tonno — il fornitore ha alzato i costi questa settimana — e vuole capire se il *crudo di ricciola con ponzu e finocchietto* sta ancora rendendo. Fa i calcoli a mano. Ci mette 40 minuti. Trova un errore. Li rifà. Va a letto all’1:00.

**Rising Action — La Scoperta:**
Matteo scarica l’app. In onboarding inserisce i suoi ingredienti principali e carica le prime ricette. Crea il *fondo dashi* come macro-ricetta — la usa in 4 piatti diversi. La prima volta che aggiorna il prezzo del kombu, vede tutti e 4 i piatti aggiornarsi in tempo reale. Si ferma. Lo fa di nuovo. Funziona.

**Climax — Il Momento OCR:**
Arriva la bolla del suo fornitore di pesce. Matteo apre l’app, tocca “Scansiona bolla”, fotografa il documento con il telefono — ancora in cucina, grembiule addosso. In 45 secondi l’app mostra: *“Trovati 8 prodotti — vuoi aggiornare il magazzino?”*. Conferma. Prezzi e quantità aggiornati. Il crudo di ricciola ora costa €4,20 invece di €3,80. Il food cost è salito al 34%. L’app lo evidenzia in arancione.

**Resolution — La Nuova Realtà:**
Prima del servizio serale, Matteo apre il Report Servizio. Vede tutti i 12 piatti in carta, il costo unitario, il food cost % e il margine stimato. Il *gyoza di maiale con ponzu* ha un food cost del 28% — il suo migliore. La *tagliata di fassona con miso butter* è al 41% — da rivedere. Questa informazione prima non esisteva. Ora la legge ogni giorno in 30 secondi.

**Requisiti rivelati:** OCR bolla, macro-ricette come ingredienti, alert variazione food cost, report pre-servizio, calcolo ingredienti esotici con unità di misura non standard

---

### Journey 2: Matteo — La Bolla Illeggibile (Edge Case)

**Opening Scene:**
Il fornitore di spezie ha cambiato il formato della bolla — adesso è scritta a mano su un modulo carbonato sbiadito. Matteo fotografa la bolla in condizioni di luce scarsa in magazzino.

**Rising Action:**
L’OCR non riesce a parsare correttamente 3 prodotti su 9. L’app mostra: *“3 prodotti non riconosciuti — revisione manuale richiesta.”* Presenta i campi precompilati con quanto ha letto, con i 3 non riconosciuti evidenziati.

**Climax:**
Matteo corregge manualmente i 3 prodotti direttamente nell’interfaccia — nome, quantità, prezzo. Ci mette 2 minuti invece di 15.

**Resolution:**
L’app memorizza il pattern del fornitore. La prossima bolla dello stesso fornitore avrà un riconoscimento migliorato.

**Requisiti rivelati:** Fallback revisione manuale OCR, UI editing inline post-scan, apprendimento pattern per fornitore, feedback chiaro su confidence score

---

### Journey 3: Admin Interno — Onboarding Nuovo Cliente

**Persona:** Intre, product owner. Deve attivare un nuovo account ristorante, assegnare il piano, e verificare che l’onboarding funzioni.

**Journey:**
Dal pannello admin, crea il tenant per *“Ristorante Da Luca - Roma”*, assegna il piano Base (trial 14 giorni), invia l’email di attivazione. Monitora che il nuovo utente completi i primi 3 step di onboarding (ingredienti > prima ricetta > primo report). Vede il funnel di attivazione in tempo reale.

**Requisiti rivelati:** Pannello admin multi-tenant, gestione piani/trial, funnel onboarding tracciabile, email automatiche di attivazione

---

### Journey Requirements Summary

| Journey | Capacità Rivelate |
|---|---|
| Matteo Happy Path | OCR bolla, macro-ricette, calcolo costi compositi, report pre-servizio, alert food cost %, unità di misura flessibili |
| Matteo Edge Case | Fallback manuale OCR, confidence score, apprendimento pattern fornitore, UI editing inline |
| Admin Onboarding | Multi-tenant, gestione piani/trial, tracking onboarding, email automatiche |

---

## Domain-Specific Requirements

### Compliance & Regulatory

- **HACCP (D.Lgs. 193/2007 + Reg. CE 852/2004):** L'app MVP non gestisce documentazione HACCP. L'architettura deve permettere l'aggiunta di moduli HACCP post-MVP senza re-architetture. Priorità: Growth Phase 2.
- **GDPR:** Le bolle fornitori contengono dati commerciali e P.IVA soggetti a GDPR. Obbligatorio: DPA con cloud provider EU, privacy policy conforme, diritto alla cancellazione dati, retention massima 90 giorni post-cancellazione account.

### Technical Constraints & Architecture

- **Hosting:** Cloud provider EU obbligatorio (data residency). Criteri: costo contenuto, conformità GDPR nativa, compatibilità API OCR. Candidati: Hetzner + OCR esterno, OVHcloud, AWS/GCP Frankfurt.
- **Frontend:** Angular PWA mobile-first — ottimizzata per uso su smartphone in cucina. Angular Material per UI component consistency.
- **Backend:** Kotlin (Spring Boot o Ktor) — API REST stateless, multi-tenant con row-level security a livello database. PostgreSQL consigliato per isolamento tenant.
- **Auth:** JWT con refresh token, sessioni persistenti su mobile (nessuna ri-autenticazione in cucina).
- **OCR:** Layer di astrazione sostituibile tra app e provider. Accuracy ≥90% su DDT italiani (standard + manoscritti). Benchmark su campione reale pre-lancio obbligatorio. Fallback manuale per documenti sotto soglia di confidenza.
- **Storage:** Immagini bolle conservate 30 giorni post-processing, poi eliminate. Dati a riposo cifrati AES-256. Comunicazioni TLS 1.2+.

### Integration Requirements

- Nessuna integrazione esterna obbligatoria per MVP — sistema chiuso e autosufficiente.
- **OCR provider** (TBD post-scelta cloud): integrazione API con gestione errori e fallback manuale.
- **Email transazionale:** provider SMTP/API standard (Resend, Postmark o equivalente).

### Risk Mitigations

| Rischio | Probabilità | Impatto | Mitigazione |
|---|---|---|---|
| OCR accuracy insufficiente su bolle atipiche | Media | Alto | Fallback manuale + apprendimento pattern per fornitore |
| Cambio prezzi cloud provider OCR | Bassa | Medio | Architettura con layer di astrazione OCR sostituibile |
| Violazione GDPR per data residency | Bassa | Alto | Cloud EU-only obbligatorio dal giorno 1, DPA firmato |
| Perdita dati magazzino utente | Bassa | Critico | Backup automatico giornaliero, export dati disponibile per utente |

---

## Innovation & Novel Patterns

### Detected Innovation Areas

**1. OCR DDT Italiano con Apprendimento per Fornitore**
Nessun tool verticale per ristoratori italiani SMB offre oggi OCR su bolle di consegna (DDT) con apprendimento incrementale per fornitore. La combinazione *foto in cucina → magazzino aggiornato in <60 secondi* elimina il principale collo di bottiglia del food cost management (aggiornamento manuale dei prezzi). Il vantaggio competitivo si rafforza nel tempo: più bolle scansionate, maggiore accuracy per quel fornitore specifico.

**2. Ricette Composte Ricorsive (Macro-Ricette come Ingredienti)**
Il modello dati permette di usare una preparazione base (fondo bruno, dashi, chermoula) come ingrediente di una ricetta, che a sua volta può essere ingrediente di un piatto degustazione. Questo consente il calcolo esatto del costo di preparazioni complesse di fine dining — impossibile con i tool tradizionali che trattano ogni ricetta come lista piatta di ingredienti.

**3. Report Pre-Servizio: da Contabilità a Operatività**
Il paradigma tradizionale del food cost è retrospettivo (*quanto mi è costato quel piatto il mese scorso*). Questo prodotto introduce un layer decisionale pre-turno (*quanto mi costerà stasera questo servizio, con i prezzi reali di oggi*) — trasformando uno strumento contabile in un assistente operativo quotidiano.

### Validation Approach

| Innovazione | Come Validarla | Timeline |
|---|---|---|
| OCR accuracy su DDT italiani | Benchmark su 50+ bolle reali di tipologie diverse prima del lancio | Pre-launch |
| Macro-ricette ricorsive | Beta con 5-10 chef di fine dining, misurare riduzione errori di calcolo | Beta closed |
| Adozione report pre-servizio | Tracking apertura report nelle 2h pre-servizio (DAU/MAU proxy) | Post-launch mese 1 |

### Risk Mitigation

| Rischio Innovazione | Mitigazione |
|---|---|
| OCR non raggiunge 90% su DDT atipici | Fallback manuale sempre disponibile; rilascio OCR come feature opzionale in beta |
| Ricette ricorsive troppo complesse per SMB | UX progressiva: ricette semplici di default, macro-ricette come feature avanzata opt-in |
| Report pre-servizio non adottato | Onboarding con tutorial guidato + notifica push reminder 2h prima del servizio |

---

## SaaS SMB Specific Requirements

### Project-Type Overview

Web app SaaS multi-tenant per ristoratori italiani singoli. Ogni ristorante è un tenant isolato. Architettura scalabile da single-user (MVP) a team multi-ruolo (Growth) senza re-architetture del modello dati.

### Tenant Model

- **Isolamento dati:** magazzino, ricette, bolle e report sono completamente separati per tenant
- **Onboarding self-service:** registrazione, selezione piano, trial 14 giorni senza intervento umano
- **Admin panel:** gestione tenant, piani, monitoraggio funnel onboarding

### Subscription Tiers

| Feature | Base | Pro | Premium |
|---|---|---|---|
| Magazzino ingredienti | ✅ | ✅ | ✅ |
| Calcolo costo piatto | ✅ | ✅ | ✅ |
| Macro-ricette come ingredienti | ✅ | ✅ | ✅ |
| OCR bolla fornitore | ❌ | ✅ | ✅ |
| Report pre-servizio | ❌ | ✅ | ✅ |
| HACCP compliance (post-MVP) | ❌ | ❌ | ✅ |
| Multi-utente con ruoli | ❌ | ❌ | ✅ |
| Export PDF report | ❌ | ✅ | ✅ |

### RBAC Matrix (Post-MVP, tier Premium)

| Ruolo | Magazzino | OCR Bolla | Ricette | Report | Impostazioni account |
|---|---|---|---|---|---|
| Chef / Admin | ✅ | ✅ | ✅ | ✅ | ✅ |
| Sous Chef / Staff cucina | ❌ | ✅ | Sola lettura | ❌ | ❌ |
| Gestore / Contabile | ❌ | ❌ | ❌ | ✅ | ❌ |

### Technical Architecture Considerations

Vedi **Domain-Specific Requirements → Technical Constraints & Architecture** per stack completo (Angular, Kotlin, PostgreSQL, OCR layer, hosting EU).

### Implementation Considerations

- Trial 14 giorni senza carta di credito — attivazione automatica piano Base a scadenza o downgrade a free-tier limitato
- Upgrade/downgrade piano self-service in-app
- Email automatica 3 giorni prima della scadenza trial

---

## Project Scoping & Phased Development

### MVP Strategy & Philosophy

**MVP Approach:** Revenue MVP — il prodotto deve generare abbonamenti paganti dal giorno 1. Nessun free tier permanente. Trial 14 giorni → conversione o stop.  
**Team MVP stimato:** 1-2 sviluppatori full-stack (Angular + Kotlin), 1 designer part-time, 1 PM. Stack moderno e ben documentato permette sviluppo rapido senza team numeroso.

### MVP Feature Set (Phase 1)

**Core User Journeys Supportati:**
- Matteo — Happy Path completo (onboarding → magazzino → ricette → macro-ricette → report pre-servizio)
- Matteo — OCR Edge Case (fallback manuale post-scan)
- Admin — Onboarding nuovo tenant

**Must-Have Capabilities:**
1. Autenticazione base (registrazione, login, reset password)
2. Magazzino ingredienti (CRUD, prezzi per kg/unità, categorie)
3. OCR bolla fornitore (foto → parsing → revisione manuale → aggiornamento magazzino)
4. Macro-ricette come ingredienti (ricorsività, calcolo costo composito)
5. Calcolo costo piatto (ingredienti + macro-ricette + energia + manodopera)
6. Report pre-servizio (lista piatti in carta, costo unitario, food cost %, margine)
7. Onboarding guidato (tutorial step-by-step per i primi 3 step critici)
8. Pannello admin interno (gestione tenant, piani, monitoraggio funnel)

### Post-MVP Features

**Phase 2 — Growth (mesi 4-9):**
- HACCP compliance automatica (registri, checklist ispezione ASL)
- Export PDF report food cost e report servizio
- Multi-utente con ruoli RBAC (Chef/Admin, Sous Chef, Gestore)
- Notifiche alert variazione prezzi fornitore
- Menu Intelligence stagionale

**Phase 3 — Expansion (mese 10+):**
- Consulente finanziario automatico (previsione margini, trend)
- Benchmark anonimizzato inter-ristorante
- Integrazione diretta ordini fornitori
- Localizzazione EU (Francia, Spagna)

### Risk Mitigation Strategy

| Tipo Rischio | Rischio Specifico | Mitigazione |
|---|---|---|
| **Tecnico** | OCR accuracy <90% su DDT atipici | Beta chiusa con 10+ ristoratori reali pre-lancio; fallback manuale sempre disponibile |
| **Tecnico** | Ricette ricorsive: complessità UX | UX progressiva con onboarding guidato; macro-ricette opt-in, non obbligatorie |
| **Mercato** | Bassa conversione trial → pagante | Onboarding che porta l'utente al "momento aha" (primo report pre-servizio) entro 15 minuti |
| **Mercato** | Churn alto per scarso engagement | Notifica pre-servizio come retention hook; DAU/MAU tracking dal giorno 1 |
| **Risorse** | Team piccolo, scope ambizioso | OCR come unica dipendenza esterna critica; tutto il resto è CRUD ben strutturato |

---

## Functional Requirements

### Autenticazione & Account

- **FR1:** Un utente può registrarsi con email e password per creare un account ristorante
- **FR2:** Un utente può accedere al proprio account con email e password
- **FR3:** Un utente può richiedere il reset della password via email
- **FR4:** Un utente può avviare un trial gratuito di 14 giorni senza inserire dati di pagamento
- **FR5:** Un utente può selezionare e sottoscrivere un piano (Base / Pro / Premium)
- **FR6:** Un utente può aggiornare o cambiare il proprio piano di abbonamento in autonomia

### Magazzino Ingredienti

- **FR7:** Un utente può aggiungere un ingrediente al magazzino con nome, unità di misura e prezzo di acquisto
- **FR8:** Un utente può modificare prezzo, quantità e unità di misura di un ingrediente esistente
- **FR9:** Un utente può organizzare gli ingredienti per categoria (es. carni, pesce, spezie, bevande)
- **FR10:** Un utente può visualizzare l'elenco completo del magazzino con prezzi aggiornati
- **FR11:** Il sistema aggiorna automaticamente il costo di tutte le ricette che usano un ingrediente quando il suo prezzo cambia

### OCR Bolla Fornitore

- **FR12:** Un utente può fotografare una bolla di consegna (DDT) con la fotocamera del dispositivo
- **FR13:** Il sistema estrae automaticamente prodotti, quantità e prezzi dalla foto della bolla
- **FR14:** Un utente può revisionare e correggere manualmente i dati estratti dall'OCR prima di confermare
- **FR15:** Un utente può confermare l'aggiornamento del magazzino con i dati della bolla revisionati
- **FR16:** Il sistema segnala chiaramente i prodotti non riconosciuti con bassa confidenza per revisione manuale
- **FR17:** Il sistema migliora il riconoscimento delle bolle di uno stesso fornitore nel tempo

### Ricette & Macro-Ricette

- **FR18:** Un utente può creare una ricetta aggiungendo ingredienti con le rispettive grammature
- **FR19:** Un utente può aggiungere costi operativi a una ricetta (energia in €/kWh, manodopera in ore × costo orario)
- **FR20:** Un utente può creare una macro-ricetta (preparazione base) e usarla come ingrediente in altre ricette
- **FR21:** Il sistema calcola automaticamente il costo totale di una ricetta incluse le macro-ricette composte
- **FR22:** Un utente può visualizzare il food cost percentuale di ogni ricetta rispetto al prezzo di vendita
- **FR23:** Un utente può aggiornare il prezzo di vendita di un piatto e vedere il food cost % ricalcolato

### Report Pre-Servizio

- **FR24:** Un utente può generare un report pre-servizio con tutti i piatti attualmente in carta
- **FR25:** Il report mostra per ogni piatto: costo unitario, food cost %, prezzo di vendita e margine stimato
- **FR26:** Il report evidenzia visivamente i piatti con food cost % superiore a una soglia configurabile
- **FR27:** Un utente può aggiungere o rimuovere piatti dalla carta attiva per il servizio corrente

### Onboarding & Navigazione

- **FR28:** Il sistema guida un nuovo utente attraverso i primi 3 step fondamentali (primo ingrediente → prima ricetta → primo report)
- **FR29:** Un utente può accedere a tutte le sezioni principali dell'app da una navigazione mobile-first
- **FR30:** Il sistema invia una notifica reminder 2 ore prima del servizio per aprire il report pre-servizio

### Pannello Admin Interno

- **FR31:** Un amministratore può creare e attivare un nuovo account tenant (ristorante)
- **FR32:** Un amministratore può assegnare o modificare il piano di abbonamento di un tenant
- **FR33:** Un amministratore può visualizzare il funnel di onboarding per ogni tenant (step completati)
- **FR34:** Il sistema invia automaticamente email di attivazione al nuovo utente dopo la creazione del tenant

---

## Non-Functional Requirements

### Performance

- Le azioni dell'utente (navigazione, salvataggio, calcoli) si completano in **<2 secondi** su connessione mobile 4G
- Il report pre-servizio si genera in **<3 secondi** anche con carta da 20+ piatti e ricette composte
- Il processo OCR (foto → risultati parsing) si completa in **<60 secondi** dalla conferma della foto
- La dashboard principale carica in **<1.5 secondi** al primo accesso dopo login

### Security

- Tutti i dati a riposo cifrati con AES-256 o equivalente
- Tutte le comunicazioni client-server protette con TLS 1.2+
- Nessun dato commerciale sensibile (prezzi, margini, fornitori) esposto in log, telemetria o error reporting
- Password utente conservate con hashing sicuro (bcrypt o Argon2)
- Sessioni JWT con refresh token e scadenza configurabile; sessioni persistenti su mobile per evitare ri-autenticazione in cucina
- Conformità GDPR: diritto alla cancellazione dati, DPA firmato con cloud provider EU, retention massima 90 giorni post-cancellazione account

### Scalability

- Il sistema supporta **300 sessioni concorrenti** (picco pre-servizio) senza degradazione delle performance
- L'architettura multi-tenant supporta crescita da 100 a 10.000 tenant senza re-architetture
- Il modello dati delle ricette ricorsive supporta **profondità di composizione fino a 5 livelli** senza impatto sulle performance di calcolo
- Backup automatico giornaliero dei dati di ogni tenant; RTO (Recovery Time Objective) <4 ore

### Integration

- Il layer OCR è astratto da un'interfaccia sostituibile — cambio di provider senza modifiche al core applicativo
- Il provider OCR deve supportare **chiamate API con timeout massimo 30 secondi** e gestione errori con retry automatico
- Il provider email transazionale (onboarding, notifiche) deve garantire delivery rate **≥98%** e supportare template HTML
- L'integrazione pagamenti (abbonamenti) deve supportare Stripe o equivalente con gestione trial, upgrade e cancellazione self-service
