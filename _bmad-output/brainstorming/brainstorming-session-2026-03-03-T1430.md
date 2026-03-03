---
stepsCompleted: [1, 2, 3, 4]
inputDocuments: []
session_topic: 'Web app per il calcolo del food cost per ristoranti singoli'
session_goals: 'Definire funzionalità, struttura e opportunità del prodotto partendo da zero (nessuno strumento digitale attuale)'
selected_approach: 'progressive-flow'
techniques_used: ['what-if-scenarios', 'mind-mapping', 'six-thinking-hats', 'solution-matrix']
ideas_generated: []
context_file: ''
---

# Brainstorming Session Results

**Facilitator:** Intre
**Date:** 2026-03-03

## Session Overview

**Topic:** Web app per il calcolo del food cost per ristoranti singoli
**Goals:** Definire funzionalità, struttura e opportunità del prodotto; partire da zero (carta e penna → digitalizzazione completa)

### Session Setup

- **Target utente:** Singolo ristorante (chef/gestore)
- **Piattaforma:** Web app
- **Punto di partenza:** Nessuno strumento digitale, solo carta e penna
- **Core features identificate dal brief iniziale:**
  1. Database ingredienti con prezzo per kg (food e beverage in peso)
  2. Calcolo automatico costo per grammatura
  3. Composizione ricette con somma costi ingredienti
  4. Archivio macro-ricette riutilizzabili (fondo bruno, salse, basi)
  5. Pannello costi operativi: energia elettrica (€/kWh) + personale (n. dipendenti × stipendio medio → costo orario)
  6. Calcolo food cost finale del piatto (ingredienti + energia + personale proporzionale al tempo)

## Technique Selection

**Approccio:** Progressive Technique Flow
**Journey Design:** Sviluppo sistematico dall'esplorazione all'azione

**Tecniche Progressive:**
- **Fase 1 - Esplorazione:** What If Scenarios — generazione massima di idee senza vincoli
- **Fase 2 - Pattern:** Mind Mapping — organizzazione dei cluster tematici
- **Fase 3 - Sviluppo:** Six Thinking Hats — approfondimento da 6 angolazioni
- **Fase 4 - Azione:** Solution Matrix — griglia feature × valore/complessità

---

## FASE 1 — Esplorazione Espansiva (What If Scenarios)

### Idee Generate

**[Feature #1]: Menu Intelligence Stagionale**
_Concept:_ L'app suggerisce proattivamente variazioni di menu basate sulla stagionalità degli ingredienti e sull'andamento dei prezzi. Se il branzino costa troppo questa settimana, l'app suggerisce di metterlo come piatto speciale il mese prossimo quando il prezzo scende.
_Novelty:_ Trasforma uno strumento di calcolo passivo in un consulente di menu attivo.
_Priority:_ Post-MVP

**[Feature #2]: Gestione Bolla Fornitore → Magazzino**
_Concept:_ L'utente carica la bolla di consegna del fornitore aggiornando automaticamente quantità e prezzi a magazzino. Ogni calcolo di food cost usa i prezzi aggiornati dall'ultima bolla.
_Novelty:_ Il magazzino è sempre allineato alla realtà — nessun disallineamento tra prezzi teorici e prezzi reali pagati.
_Priority:_ Core MVP

**[Feature #3]: Modifica Manuale Prezzi e Quantità**
_Concept:_ Ogni ingrediente a magazzino è editabile manualmente — prezzo unitario, quantità disponibile, unità di misura. Storico delle modifiche visibile per tracciare variazioni nel tempo.
_Novelty:_ Flessibilità totale senza dipendere da integrazioni esterne.
_Priority:_ Core MVP

**[Feature #4]: Costo Menu per Periodo**
_Concept:_ Selezioni un menu e un periodo (es. marzo 2026), l'app calcola il costo totale aggregato di tutti i piatti tenendo conto dei prezzi reali in quel periodo.
_Novelty:_ Dalla singola ricetta alla visione d'insieme — utile per eventi, stagioni, offerte speciali.
_Priority:_ Core MVP

**[Feature #5]: Allerta Variazione Costo Ingrediente**
_Concept:_ Notifica automatica quando il prezzo di un ingrediente è cambiato rispetto all'ultimo utilizzo in ricetta, con ricalcolo automatico del food cost impattato.
_Novelty:_ Il food cost delle ricette rimane sempre aggiornato senza intervento manuale.
_Priority:_ Post-MVP

**[Feature #6]: Gestione Multi-Fornitore**
_Concept:_ Stesso ingrediente associato a più fornitori con prezzi diversi, con possibilità di scegliere quale usare nel calcolo.
_Priority:_ Post-MVP

**[Feature #7 — STAR]: Import Bolla via Foto (OCR)**
_Concept:_ L'utente fotografa la bolla del fornitore con il telefono, l'app estrae automaticamente tramite OCR/AI: nome ingrediente, quantità, unità di misura, prezzo unitario. L'utente rivede i dati estratti, corregge eventuali errori e conferma.
_Novelty:_ Abbatte la barriera di adozione principale — da "devo stare al computer a digitare" a "faccio una foto e in 30 secondi ho il magazzino aggiornato."
_Priority:_ Core MVP

**[Feature #8]: Sistema Ruoli Utente**
_Concept:_ Almeno due ruoli: Admin (chef/gestore — accesso completo) e Staff (sous chef/cucina — solo ricette, senza vedere i costi).
_Novelty:_ Lo chef protegge le informazioni strategiche sui costi senza escludere il team dalla parte operativa.
_Priority:_ Core MVP

**[Feature #9]: Pattern Recognition Bolle per Fornitore**
_Concept:_ Con ~10 bolle/settimana da fornitori diversi, l'app impara il layout di ogni fornitore dopo i primi utilizzi. Al terzo caricamento dello stesso fornitore, l'OCR ha già un modello di riferimento e l'estrazione diventa quasi automatica.
_Novelty:_ Il sistema migliora con l'uso — dopo il primo mese il caricamento di una bolla è questione di secondi.
_Priority:_ Core MVP

**[Feature #10 — STAR]: Costo Reale Totale per Preparazione**
_Concept:_ Per ogni ricetta si inserisce il tempo di preparazione in ore. L'app calcola: costo ingredienti + (costo orario personale × ore) + (kWh stimati × costo energia). Risultato: costo reale al kg/litro della preparazione finita.
_Novelty:_ Il fondo bruno diventa un ingrediente valorizzato al suo costo reale — quando lo usi nel risotto, il costo del risotto riflette la verità economica.
_Priority:_ Core MVP

**[Feature #11 — STAR]: Macro-Ricette come Ingredienti**
_Concept:_ Il fondo bruno salvato nell'archivio non è solo una ricetta — diventa un ingrediente riutilizzabile con il suo costo per litro/kg calcolato. "200ml di fondo bruno" nel brasato porta con sé il suo costo reale.
_Novelty:_ La catena di costo è completa — dalla materia prima grezza al piatto finito, passando per tutte le preparazioni intermedie.
_Priority:_ Core MVP

**[Feature #12]: Multi-Tenancy SaaS**
_Concept:_ Ogni ristorante ha il proprio account isolato — i propri ingredienti, ricette, fornitori, costi del personale. Un unico sistema cloud che serve N ristoranti contemporaneamente.
_Priority:_ Core MVP (architettura base)

**[Feature #13]: Abbonamento SaaS a Tier**
_Concept:_ Pricing mensile per ristorante — tier Base, Pro, Premium con feature crescenti.
_Priority:_ Business model

**[Decisione Strategica #1]: Configurazione da Zero per Ogni Ristorante**
_Concept:_ Nessun database condiviso — ogni ristorante costruisce il proprio magazzino da zero, rispecchiando la sua realtà specifica.

**[Decisione Strategica #2]: Posizionamento "Fatto da un Chef per i Chef"**
_Concept:_ Vantaggio competitivo esperienziale — chi ha costruito questo strumento conosce la cucina reale. Marketing autentico e differenziante.

**[Feature #15 — STAR]: Carrello di Revisione Bolla (post-OCR)**
_Concept:_ Dopo lo scan OCR, i dati appaiono come un "carrello della spesa" — lista di righe con nome, quantità, prezzo. Ingredienti già a magazzino mostrano il prezzo precedente a confronto. Variazioni superiori alla soglia configurabile (es. +15%) evidenziate con warning ⚠️. L'utente conferma, modifica o rimuove ogni riga prima di importare.
_Novelty:_ Interfaccia familiare + controllo totale + allerta proattiva sui cambi di prezzo anomali.
_Priority:_ Core MVP

**[Feature #16]: Storico Modifiche con Tracciabilità Utente**
_Concept:_ Ogni modifica a prezzi e quantità registra: utente, data/ora, valore precedente, valore nuovo.
_Priority:_ Core MVP

**[Feature #17]: Costi Operativi come Stime Dichiarate**
_Concept:_ Il pannello energia e personale mostra esplicitamente che sono stime configurabili. Il food cost finale riporta sempre la distinzione: "€4,20 ingredienti + €1,80 stimato (personale+energia)".
_Priority:_ Core MVP

**[Feature #18]: Calibrazione Costi Energetici da Bolletta Reale**
_Concept:_ L'utente carica il dato reale dalla bolletta mensile e l'app ricalibra automaticamente il costo kWh usato nelle stime precedenti.
_Priority:_ Post-MVP

---

## FASE 2 — Mind Mapping: Cluster Tematici

### Mappa dei 6 Cluster

```
                    FOOD COST WEB APP
                          │
   ┌──────────┬───────────┼──────────┬──────────┬──────────┐
   │          │           │          │          │          │
MAGAZZINO  RICETTE    COSTI      HACCP &    SISTEMA   BUSINESS
& BOLLE    & MENU   OPERATIVI  SCADENZE    & UX
   ⭐
```

**Cluster 1 — Magazzino & Fornitori** *(cuore critico)*
- Import bolla via foto OCR ⭐
- Carrello revisione post-OCR con warning variazioni ⭐
- Modifica manuale prezzi e quantità
- Storico modifiche con tracciabilità
- *(Post-MVP)* Multi-fornitore, allerta variazioni automatiche

**Cluster 2 — Ricette & Menu**
- Composizione ricette con calcolo food cost per grammatura
- Macro-ricette come ingredienti riutilizzabili ⭐
- Archivio ricette salvate
- Costo menu aggregato per periodo
- *(Post-MVP)* Menu intelligence stagionale

**Cluster 3 — Costi Operativi**
- Pannello configurazione: energia (€/kWh) + personale (n. dipendenti × stipendio medio)
- Costo reale totale per preparazione *(ingredienti + personale × ore + energia)* ⭐
- Costi operativi etichettati come "stime" nella UI
- *(Post-MVP)* Calibrazione da bolletta reale

**Cluster 4 — HACCP & Scadenze** *(nuovo cluster, compliance obbligatoria)*
- Acquisizione data scadenza/lotto da OCR bolla
- Semaforo scadenze a magazzino
- Generazione etichette HACCP stampabili
- Report HACCP PDF per ispezione ASL
- *(Post-MVP)* Tracciabilità lotti in caso di richiamo prodotto

**Cluster 5 — Sistema & UX**
- Multi-tenancy SaaS *(ogni ristorante isolato)*
- Sistema ruoli: Admin vs Staff
- Interfaccia mobile-first *(foto bolle da smartphone)*
- Pattern recognition OCR per fornitore *(migliora con l'uso)*
- *(Post-MVP)* Modalità offline

**Cluster 6 — Business & Posizionamento**
- SaaS a tier *(Base / Pro / Premium)*
- Posizionamento "Fatto da un Chef per i Chef"
- Ogni ristorante parte da zero, configurazione propria

### Pattern Emergenti

- **Pattern 1 — La catena del costo:** materia prima → preparazione base → piatto finito → costo reale totale completo
- **Pattern 2 — Fiducia nel dato:** OCR con revisione, storico modifiche, stime dichiarate = "puoi fidarti di questi numeri"
- **Pattern 3 — Risparmio di tempo operativo:** OCR + pattern fornitore + macro-ricette = selling point commerciale principale

---

## FASE 3 — Six Thinking Hats

### 🎩 Cappello Bianco — Fatti
- ~10 bolle/settimana per ristorante medio *(dato verificato)*
- Food cost target ristorazione: **28-35%** del prezzo di vendita
- HACCP obbligatorio per legge in Italia (D.Lgs. 193/2007)
- Fondo bruno: 8-12 ore cottura — costo operativo reale multiplo del costo ingredienti
- ~330.000 ristoratori italiani — mercato potenziale enorme per SaaS verticale
- OCR moderno (Google Vision, AWS Textract): >95% accuratezza su documenti strutturati

### 🎩 Cappello Rosso — Emozioni
Pain point confermati dall'utente:
- **Frustrazione** nella gestione burocratica HACCP
- **Fastidio** nel calcolare manualmente i costi tenendo conto di tutti i parametri
- Emozione risolta dal prodotto: **controllo e fiducia** sui propri numeri, **sollievo** dall'ansia delle ispezioni

### 🎩 Cappello Giallo — Benefici
| Beneficio | Per lo Chef | Per il Business |
|---|---|---|
| OCR bolla | -30/60 min/settimana inserimento dati | Riduce errori sui prezzi |
| Food cost automatico | Sa sempre se un piatto è redditizio | Margini controllati |
| HACCP integrato | Ispezioni senza ansia | Compliance automatica, zero sanzioni |
| Macro-ricette come ingredienti | Costo reale di ogni piatto finalmente vero | Decisioni di menu basate su dati |
| Ruoli utente | Team lavora, costi restano privati | Protezione informazioni strategiche |

**Beneficio meta:** Non è un software di food cost — è controllo totale sulla propria cucina senza diventare un ragioniere.

### 🎩 Cappello Nero — Rischi
- **OCR su bolle di qualità scadente** (foto mosse, carta stropicciata) → accuratezza cala
- **Resistenza all'adozione** *(preoccupazione principale)* — abbandono nelle prime 2 settimane
- **Prezzi SaaS vs Excel gratuito** *(preoccupazione principale)* — il ristoratore vede un costo in più
- **Dati HACCP sbagliati** inseriti per fretta → falsa sicurezza, rischio legale
- **Connessione internet** — se cade il WiFi l'app non funziona durante il servizio

**Risposta strategica ai rischi principali:**
- Resistenza adozione → dare "wow" immediato con OCR + compliance HACCP dal giorno 1
- SaaS vs Excel → pricing giustificato da risparmio di tempo + copertura legale obbligatoria (non confrontabile con Excel)

### 🎩 Cappello Verde — Idee Creative Aggiuntive
- **[Feature #21 — Post-MVP]:** Modalità offline per operazioni core, sincronizzazione al ritorno del WiFi
- **[Feature #22 — Sprint 3]:** Report HACCP PDF automatico per ispezione — un click, tutto il registro del mese
- **[Feature #23 — Post-MVP]:** "Costo Piatto del Giorno" — dashboard mattutina con food cost aggiornato dei 5 piatti più usati

### 🎩 Cappello Blu — Sequenza di Sviluppo
*Ordine MVP rivisto e confermato dall'utente:*
1. Magazzino manuale + calcolo food cost ricette *(nucleo)*
2. OCR bolla + carrello revisione *(wow immediato, anti-churn)*
3. HACCP integrato *(compliance = obbligo = retention)*
4. Archivio macro-ricette come ingredienti
5. Pannello costi operativi
6. Post-MVP tutto il resto

---

## FASE 4 — Solution Matrix: Roadmap MVP

### Griglia Feature × Valore/Complessità

| # | Feature | Valore Utente | Complessità | Sprint |
|---|---|---|---|---|
| 2 | Magazzino manuale + food cost ricette | ⭐⭐⭐⭐⭐ | 🔧🔧 | **SPRINT 1** |
| 3 | Modifica manuale prezzi/quantità | ⭐⭐⭐⭐⭐ | 🔧 | **SPRINT 1** |
| 8 | Ruoli Admin / Staff | ⭐⭐⭐⭐ | 🔧🔧 | **SPRINT 1** |
| 12 | Multi-tenancy SaaS | ⭐⭐⭐⭐⭐ | 🔧🔧🔧 | **SPRINT 1** *(architettura)* |
| 7 | OCR bolla via foto | ⭐⭐⭐⭐⭐ | 🔧🔧🔧🔧 | **SPRINT 2** |
| 15 | Carrello revisione + warning variazioni | ⭐⭐⭐⭐⭐ | 🔧🔧 | **SPRINT 2** |
| 9 | Pattern recognition fornitore | ⭐⭐⭐⭐ | 🔧🔧🔧 | **SPRINT 2** |
| 19 | HACCP da bolla (scadenze + lotti) | ⭐⭐⭐⭐⭐ | 🔧🔧🔧 | **SPRINT 3** |
| 20 | Semaforo scadenze + alert | ⭐⭐⭐⭐⭐ | 🔧🔧 | **SPRINT 3** |
| 22 | Report HACCP PDF per ispezione | ⭐⭐⭐⭐⭐ | 🔧🔧 | **SPRINT 3** |
| 11 | Macro-ricette come ingredienti | ⭐⭐⭐⭐⭐ | 🔧🔧🔧 | **SPRINT 4** |
| 10 | Costo reale totale preparazione | ⭐⭐⭐⭐⭐ | 🔧🔧🔧 | **SPRINT 4** |
| 4 | Costo menu aggregato per periodo | ⭐⭐⭐⭐ | 🔧🔧 | **SPRINT 4** |
| 17 | Pannello costi operativi (stime) | ⭐⭐⭐⭐ | 🔧🔧 | **SPRINT 5** |
| 16 | Storico modifiche tracciabilità | ⭐⭐⭐⭐ | 🔧🔧 | **SPRINT 5** |
| 21 | Modalità offline | ⭐⭐⭐ | 🔧🔧🔧🔧 | **Post-MVP** |
| 5 | Allerta variazione costo automatica | ⭐⭐⭐ | 🔧🔧🔧 | **Post-MVP** |
| 6 | Multi-fornitore stesso ingrediente | ⭐⭐⭐ | 🔧🔧 | **Post-MVP** |
| 1 | Menu intelligence stagionale | ⭐⭐⭐ | 🔧🔧🔧🔧🔧 | **Post-MVP** |

### MVP = Sprint 1 + 2 + 3

Prodotto vendibile, differenziante e legalmente utile che nessun Excel può replicare.

---

## DECISIONI E CONCLUSIONI

### Decisioni Strategiche Chiave
1. **Piattaforma:** Web app mobile-first
2. **Target:** Singolo ristorante — configurazione da zero, nessun database condiviso
3. **Business model:** SaaS multi-tenant a tier (Base / Pro / Premium)
4. **Posizionamento:** "Fatto da un Chef per i Chef" — differenziatore esperienziale unico
5. **Anti-churn:** OCR + HACCP nelle prime settimane → valore immediato visibile

### Il Pitch in Una Frase
> *"Fotografa la bolla del fornitore, calcola il food cost di ogni piatto, tieni l'HACCP in ordine — tutto in meno di 5 minuti al giorno."*

### Prossimi Passi Suggeriti
- Passare all'agente **PM (John)** per trasformare questo brainstorming in un PRD formale
- Definire stack tecnologico con l'agente **Architect (Winston)**
- Pianificare Sprint 1 con l'agente **SM (Bob)**
