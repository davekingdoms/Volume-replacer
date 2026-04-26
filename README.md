# PixelVolumeHelper

Utility app Android personale pensata per sostituire in pratica i pulsanti volume fisici non funzionanti su **Pixel 7a**.

## Obiettivo

L'app deve offrire tre cose:

1. **Widget home** in stile Pixel / Material You per:
   - aumentare e diminuire il volume
   - cambiare modalità audio (`mute -> vibrate -> normal`)
   - selezionare quale stream controllare

2. **Pulsante flottante** sopra tutte le app:
   - **tap breve**: apre il controllo del volume
   - **pressione lunga**: esegue uno screenshot

3. **App principale minimale**:
   - solo configurazione e stato permessi
   - niente dashboard complessa
   - niente UI superflua

---

## Scope

### Incluso
- app in **Kotlin**
- UI principale in **Jetpack Compose**
- widget con **Jetpack Glance**
- persistenza configurazioni con **DataStore**
- overlay flottante tramite `SYSTEM_ALERT_WINDOW`
- screenshot tramite `AccessibilityService`
- configurazione permessi e shortcut verso schermate sistema
- ottimizzazione per **Pixel 7a**

### Escluso
- Play Store / distribuzione pubblica
- compatibilità spinta con dispositivi vecchi
- slider trascinabile nel widget
- sincronizzazione cloud
- analytics
- localizzazione iniziale

---

## Specifica funzionale

## 1. Widget home

### Comportamento
- **pulsante centrale alto**: cicla tra
  - `SILENT`
  - `VIBRATE`
  - `NORMAL`
- **pulsante sinistro**: volume giù
- **pulsante destro**: volume su
- **parte bassa**: selezione stream audio

### Stream supportati
Default:
- Media
- Ring
- Alarm
- Notification

Avanzati opzionali:
- System
- Accessibility
- Voice call
- DTMF

### Nota importante
Il widget è progettato come UI a **tap/click**, non come mini activity.  
Lo **slider nel widget non è una priorità progettuale**: la UX corretta qui è fatta di pulsanti, chip e aggiornamento immediato dello stato.

---

## 2. Overlay flottante

### Azioni
- **tap breve**
  - default: apre `Settings.Panel.ACTION_VOLUME`
  - opzionale: tenta di mostrare l'HUD volume-like
- **pressione lunga**
  - screenshot tramite `AccessibilityService`

### Requisiti
- draggable
- posizione persistente
- visibile sopra le app
- opzionale auto-start al boot

---

## 3. App principale

L'app principale deve rimanere **molto piccola**.

### Sezioni previste
- stato permessi
- impostazioni overlay
- impostazioni widget
- azioni debug/test

### Permessi / accessi da gestire
- Overlay permission (`SYSTEM_ALERT_WINDOW`)
- Accessibility Service
- Notification Policy Access
- Battery optimization exclusion
- opzionale boot restore

---

## Architettura proposta

### Package logici
- `ui/`
- `settings/`
- `audio/`
- `widget/`
- `overlay/`
- `accessibility/`
- `data/`
- `boot/`

### Componenti principali
- `MainActivity`
- `SettingsScreen`
- `PreferencesRepository`
- `VolumeController`
- `VolumeWidget`
- `FloatingOverlayService`
- `ScreenshotAccessibilityService`
- `BootReceiver` (opzionale)

---

## Stack tecnico

- **Kotlin**
- **Jetpack Compose**
- **Material 3**
- **Jetpack Glance**
- **DataStore Preferences**
- Android services / overlays
- `AudioManager`
- `NotificationManager`
- `AccessibilityService`

### Target
App personale ottimizzata per **Pixel 7a**.

Scelta consigliata:
- `minSdk = 36`
- `targetSdk =` ultima stabile disponibile durante lo sviluppo

---

## Vincoli tecnici importanti

### 1. Widget Android
I widget hanno limiti strutturali: non vanno trattati come schermate complete.  
Il progetto deve privilegiare **azioni discrete** (`+`, `-`, toggle, selettore stream) e non controlli continui complessi.

### 2. Ringer mode / DND
Il ciclo `mute / vibrate / normal` può dipendere da `Notification Policy Access`.  
Questa parte va gestita con check espliciti e fallback chiari.

### 3. Overlay lifecycle
Il service overlay deve essere robusto rispetto a:
- cambio app
- rotazione
- background restrictions
- reboot

### 4. Screenshot
Lo screenshot dipende dall'`AccessibilityService` e va testato bene su casi reali.

---

## MVP

Il progetto può dirsi MVP completo quando:

- il widget è installabile e funzionante
- il bottone centrale cambia modalità audio
- `+` e `-` modificano il volume dello stream selezionato
- il selettore stream persiste lo stato
- il bottone flottante funziona sopra le app
- tap breve apre il controllo volume
- long press esegue screenshot
- l'app mostra chiaramente quali permessi mancano

---

## Ordine di sviluppo consigliato

1. setup progetto
2. `PreferencesRepository`
3. `VolumeController`
4. schermata impostazioni minima
5. wiring permessi
6. widget MVP
7. overlay MVP
8. screenshot via accessibility
9. boot restore opzionale
10. rifinitura UI
11. hardening

---

## Come usare Copilot in questo progetto

Copilot qui va usato come:

- generatore di boilerplate
- assistente per refactor
- supporto per wiring iniziale

Non va trattato come autorità tecnica su:

- manifest finale
- permessi
- service lifecycle
- widget limitations
- accessibility wiring
- audio / notification policy handling

### Regola pratica
Task piccoli e verificabili:

- “implementa il repository DataStore”
- “crea il widget MVP con 4 action callback”
- “scrivi il service overlay draggable”

Non:
- “fammi tutta l’app”

---

## Stato attuale

Repository in fase di scaffold iniziale.

Prossimi step attesi:
- verificare la struttura generata da Copilot
- controllare `PreferencesRepository`
- controllare `VolumeController`
- pulire il manifest
- partire con widget MVP
