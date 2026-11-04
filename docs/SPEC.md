# Spezifikation: grit-motivation (privater Fork von Grit)

Privater Fork von [shub39/Grit](https://github.com/shub39/Grit) (GPL-3.0, Kotlin Multiplatform,
Compose Material 3, Room). Ziel: kontextsensitives Motivations-/Sarkasmus-Pop-up beim Abhaken von
Habits (Phase 1); danach Energie & Aktiv-Serie (Phase 2, Abschnitt 4), Statistiken (Phase 3, Abschnitt 6) und ein
Avatar (Phase 4, Abschnitt 7) und eine aufgeräumte Oberfläche mit Avatar-Reiter und farbigen
Routinen (Phase 5, Abschnitt 8; Roadmap in Abschnitt 5).

Basis-Stand: Upstream `master` @ `3e203449` (Release 6.2.2).

---

## 1. Ergebnisse der Vor-Analyse (Step 0)

### 1.1 Build-Konfiguration

| Punkt | Befund |
|---|---|
| Flavors | `play` und `foss` (`androidApp/build.gradle.kts`, Dimension `version`) |
| Billing im FOSS-Flavor | `androidApp/src/foss/.../billing/BillingHandler.kt` liefert fest `isPlus = true` → alle Plus-Features frei, **kein RevenueCat-Key nötig** |
| Analytics im FOSS-Flavor | No-op (`androidApp/src/foss/.../analytics/AnalyticsImpl.kt`), PostHog nur im `play`-Flavor |
| Build-Befehl | `./gradlew :androidApp:assembleFossDebug` |
| Toolchain | JDK 21, Gradle 9.8, AGP 9.4.1, Kotlin 2.4.21, compileSdk 37, minSdk 29 |
| Formatierung | Spotless + ktfmt (kotlinlang-Style) mit GPL-Lizenz-Header → `./gradlew spotlessApply` |
| Desktop-Hot-Reload | `./gradlew :desktopApp:hotRunJvm --auto` (schnelles UI-Testen ohne Emulator) |

**Wir arbeiten ausschließlich mit dem `foss`-Flavor.** Der `play`-Flavor bleibt unverändert.

### 1.2 Relevante Pfade

Präfix `shared/ui/src/commonMain/kotlin/com/shub39/grit/shared/ui/` = `UI/`

| Was | Wo |
|---|---|
| Habit-Liste (Screen-Section) | `UI/habit/ui/sections/HabitsList.kt` (`HabitsList` → `HabitsListContent`) |
| Habit-Karte mit Check-Button | `UI/habit/ui/component/HabitCard.kt` (Callback `onInsertStatus`) |
| Abhak-Actions | `UI/habit/HabitsAction.kt`: `InsertStatus(habit, date)` (Toggle) und `InsertMeasuredStatus(habit, date, value)` |
| Toggle-Logik (an/ab) | `UI/viewmodel/HabitViewModel.kt` → `insertHabitStatus(...)` – entscheidet bereits, ob Status gelöscht oder eingefügt wird |
| Screen-State | `UI/habit/HabitState.kt` |
| Streak-Berechnung | `shared/logic/.../habits/repository/Util.kt` → `countCurrentStreak(dates, eligibleWeekdays)` |
| Streak im Domain-Modell | `shared/core/.../habits/HabitWithAnalytics.kt` → `currentStreak` (befüllt in `HabitRepository`) |

### 1.3 Modul-Abhängigkeiten (wichtig für die Architektur)

```
androidApp ──► shared:ui ──► shared:core
          └──► shared:logic ──► shared:core
```

`shared:ui` hängt **nicht** von `shared:logic` ab – `countCurrentStreak` ist aus dem ViewModel also
nicht direkt aufrufbar.

---

## 2. Korrekturen gegenüber der Ursprungs-Spezifikation

Die ursprüngliche Spec (andere KI) wurde gegen den echten Code geprüft. Abweichungen:

1. **Check-In-Erkennung im ViewModel statt im Composable.**
   Die Spec wollte `remember { mutableStateOf(...) }` im Screen und dort `completedDates.contains(...)`
   prüfen. Das ViewModel trifft diese Entscheidung aber bereits in `insertHabitStatus`. Die doppelte
   Logik im UI würde UDF verletzen und kann auseinanderlaufen.
   → Popup-Daten werden Teil von `HabitState`, Schließen läuft über eine neue Action.

2. **Streak nicht als `currentStreak + 1` berechnen.**
   Falsch u. a. bei: Abhaken eines vergangenen Tages, Lückenschluss zwischen zwei Serien,
   Habits mit eingeschränkten Wochentagen.
   → Nach dem Insert den aktualisierten `HabitWithAnalytics.currentStreak` aus dem Repository-Flow
   lesen (`repo.getHabitsWithAnalytics().first()`). Damit gilt **eine** Wahrheit: dieselbe Zahl wie
   auf der Habit-Karte.

3. **Messbare Habits** (`InsertMeasuredStatus`) wurden nicht berücksichtigt.
   → Pop-up auch dort, aber nur wenn für das Datum vorher **kein** Status existierte und `value > 0`
   (also kein Pop-up beim Korrigieren eines Werts).

4. **Dateiaufteilung:** Statt einer Datei `HabitMotivationDialog.kt` für alles:
   - Engine + Modelle (reines Kotlin, ohne Compose/Room) in `shared:core`
     → Package `com.shub39.grit.core.motivation` – testbar per JVM-Unit-Test, strikt getrennt von DAOs.
   - Composable in `shared:ui` → `UI/habit/ui/component/HabitMotivationPopup.kt`.

5. **Zufall injizierbar:** `GritQuoteEngine.getQuote(activeDays, random: Random = Random.Default, ...)`
   für deterministische Tests.

6. **Grammatik:** „1 Tag aktiv“ / „n Tage aktiv“ statt „Tag(e)“.

7. **Übersetzbare Sprüche (Entscheidung aus dem Design-Interview):** `shared:core` kennt keine
   Compose-Resources. Die Engine liefert deshalb nur eine **ID** (plus Ton und Kategorie), den Text
   holt die UI aus String-Resources und setzt die Tage per Formatargument ein (`%1$d`).
   `MotivationQuote` hat daher kein Feld `text`, und die Engine ersetzt `{days}` nicht selbst.

8. **Kategorien statt loser Intervalle:** Jeder Spruch gehört zu einer `StreakTier`
   (Streak-Bereich je Stufe an einer einzigen Stelle definiert).

9. **Kein doppelter Spruch hintereinander:** Optionaler Parameter `previous`; das ViewModel merkt
   sich den zuletzt gezeigten Spruch.

---

## 3. Phase 1 – Anforderungen

### 3.1 Domain (`shared:core`)
Package `com.shub39.grit.core.motivation`, reines Kotlin ohne Compose/Room.

- `enum class QuoteTone { MOTIVATING, SARCASTIC }`
- `enum class StreakTier(val range: IntRange)` mit `STARTER (1..1)`, `BUILDING (2..4)`,
  `ESTABLISHED (5..13)`, `LEGEND (14..Int.MAX_VALUE)` und `fun forStreak(days: Int)`
  (`days < 1` → `STARTER`).
- `data class MotivationQuote(id: String, tone: QuoteTone, tier: StreakTier)` – **kein Text**.
- `object QuoteCatalog` (eigene Datei): Liste aller Sprüche (nur ID, Ton, Stufe) und ein fester
  Fallback-Spruch.
- `object GritQuoteEngine`
  - `getQuote(activeDays: Int, random: Random = Random.Default, previous: String? = null,
    quotes: List<MotivationQuote> = QuoteCatalog.all): MotivationQuote`
  - Wählt rein zufällig unter den Sprüchen der passenden `StreakTier` (keine Gewichtung der Töne).
  - `previous` (ID) wird ausgeschlossen, solange es Alternativen gibt.
  - Kein passender Spruch (z. B. leere Liste) → fester Fallback-Spruch aus dem Katalog.
  - Zustandslos; kennt weder Room noch Compose.
- Jede Stufe enthält im Katalog mindestens einen Spruch **pro Ton** (per Test abgesichert).

### 3.1a Texte (`shared:ui`, Compose-Resources)
- Eigene Dateien `composeResources/values/motivation_strings.xml` (Englisch) und
  `values-de/motivation_strings.xml` (Deutsch). Die `strings.xml` des Originals bleibt unberührt
  (dort ändern Übersetzungs-Tools laufend, eigene Einträge würden Merge-Konflikte erzeugen).
- Spruch-ID = Resource-Name (`quote_<stufe>_<ton>_<nr>`, `quote_fallback`). Die UI schlägt den Text
  über `Res.allStringResources[id]` nach, keine handgepflegte Zuordnung.
- 4 Sprüche pro Stufe und Ton (32) plus Fallback. Anrede „du“ / „you“.
- Sarkasmus leicht steigend: STARTER ~3, BUILDING/ESTABLISHED ~4, LEGEND ~5 von 10. Nie beschämend,
  keine Kraftausdrücke. Englisch frei übertragen, nicht wörtlich.
- Die Tage kommen immer als Formatargument `%1$d`; ein Spruch darf sie nutzen, muss aber nicht. Keine
  Plurals nötig: STARTER ist immer 1 Tag, alle anderen Stufen ≥ 2.
- Fallback ohne Zahl: „Abgehakt! Weiter so.“ / „Done! Keep it up.“
- Tests in `shared:ui` (`commonTest`): jede Katalog-ID hat einen String, jeder `quote_*`-String hat
  eine Katalog-ID, Deutsch und Englisch haben dieselben Schlüssel.

### 3.2 UI (`shared:ui`)
- `@Composable fun HabitMotivationPopup(habitTitle, activeDays, quote, onDismiss, modifier = Modifier)`
  in `UI/habit/ui/component/HabitMotivationPopup.kt`, zustandslos.
- Basis ist der projekteigene `GritDialog` (wie die übrigen Dialoge der App), nicht `AlertDialog`.
  `GritDialog` hat dafür einen optionalen Parameter `properties` bekommen (rückwärtskompatibel).
- Schließt per Button und Zurück-Taste, **nicht** durch Tippen daneben und nicht automatisch.
- Aufbau von oben nach unten, ohne Symbol:
  - Unterzeile je Ton: `SARCASTIC` → „Na sieh mal einer an…“ / „Well, look at that…“,
    `MOTIVATING` → „Starkes Ding!“ / „Nice one!“
  - Titel (fett, beliebig viele Zeilen): Plural-Resource „%1$s • %2$d Tag(e) aktiv“
  - Spruch: `stringResource(quote.textResource(), activeDays)`
  - Button: „Weiter geht’s“ / „Let’s go“
- Alle Texte in `motivation_strings.xml` (DE + EN). Der Übersetzungstest prüft Strings und Plurals.
- Zwei Previews (je Ton) mit `GritPreviewWrapper`; keine UI-Tests.

### 3.3 State & Integration
Umgesetzt in einem PR für #5, #6 und #7.

- `MotivationPopupState(habitTitle, activeDays, quote)` (`@Immutable`) in
  `UI/habit/motivation/MotivationPopupState.kt`; `HabitState.motivationPopup: MotivationPopupState?`.
- `HabitsAction.DismissMotivationPopup` setzt den State auf `null`.
- `HabitViewModel.insertHabitStatus(...)`:
  - Pop-up nur beim **Abhaken** (für das Datum existierte vorher kein Status). Abwählen zeigt keins.
  - Messbare Habits: nur der **erste** Wert eines Tages (`value > 0`) zählt als Check-In;
    Korrigieren oder auf 0 setzen zeigt keins.
  - Streak = `currentStreak` aus `repo.getHabitsWithAnalytics().first()` (dieselbe Zahl wie auf der
    Karte, nicht `+ 1`). Bei Streak 0 (Nachtragen eines alten Tages ohne laufende Serie) kein Pop-up.
  - Das ViewModel merkt sich die ID des letzten Spruchs (nur im Speicher) und übergibt sie als
    `previous`.
  - Nur Check-Ins für **heute oder gestern** zeigen ein Pop-up; Nachträge älterer Tage bleiben still
    (#8).
- Ein offenes Pop-up überlebt das Beenden der App im Hintergrund nicht (bewusst hingenommen).
- Tests: `shared/ui/src/commonTest/.../viewmodel/HabitViewModelMotivationTest.kt` über `onAction` → `state`,
  mit Fakes in `shared/ui/src/commonTest/.../fakes/`. Der Streak wird im Fake vorgegeben.
- Anzeige (#8): `MotivationPopupHost` (`UI/habit/motivation/`) ist **einmal zentral** in
  `GritNavDisplay` eingebunden, damit das Pop-up beim Abhaken in der Liste, auf der Detailseite und
  im Kalender sofort erscheint (alle Habit-Screens teilen sich dieselbe `HabitViewModel`-Instanz).
  Der Host beobachtet den eigenen Flow `HabitViewModel.motivationPopup`, nicht `state`, damit die
  Habit-Beobachter nicht auch auf dem Aufgaben-Tab laufen.
- Abhaken über Widget oder Benachrichtigung zeigt kein Pop-up (läuft am ViewModel vorbei).
- Abschaltbar (#9): Schalter „Motivations-Pop-up“ auf der Einstellungsseite, Standard **an**.
  Schnittstelle `MotivationSettings` in `core.motivation`, `MotivationSettingsImpl`
  (`logic/motivation/`) nutzt die bestehende Preferences-Datei mit dem Schlüssel
  `motivation_popup_enabled`. `HabitViewModel` liest den Wert beim Abhaken; ist er aus, wird
  `motivationPopup` nicht gesetzt.

### 3.4 Definition of Done (Phase 1)
Abgenommen am 2026-10-18 auf einem Pixel 8a (Android 17), Build aus #8.

- [x] `./gradlew :androidApp:assembleFossDebug` grün, ohne Billing-/Analytics-Keys (lokal + CI).
- [x] Abhaken eines Habits öffnet sofort das Pop-up.
- [x] Pop-up zeigt korrekten Habit-Namen und denselben Streak wie die Habit-Karte.
- [x] Abwählen eines gesetzten Häkchens zeigt **kein** Pop-up.
- [x] Nachtrag eines älteren Tages zeigt kein Pop-up; gestern abhaken zeigt eins.
- [x] Zurück und Button schließen das Pop-up, Tippen daneben nicht.
- [x] Unit-Tests (`shared:core`, `shared:ui`) grün; `spotlessCheck` grün.

---

## 4. Phase 2 – Energie & Aktiv-Serie

Design-Interview abgeschlossen am 2026-10-18. Epic #14, Meilenstein „Phase 2 – Energie & Aktiv-Serie“.

### 4.1 Grundidee
Belohnt wird die **Nutzung der App**, nicht die Serie einzelner Routinen.

- Ein Tag ist **aktiv**, wenn an diesem Tag mindestens ein Eintrag gemacht wurde – egal für welche
  Routine und für welches Datum (ein Nachtrag für gestern zählt für heute), auch über Widget oder
  Erinnerung. Wird das Häkchen wieder entfernt, bleibt der Tag aktiv.
- Die globale **Aktiv-Serie** zählt aktive Tage in Folge.
- Die Serien der einzelnen Routinen bleiben **unverändert** und ohne Freeze. Streak-Berechnung im
  Repository, Widgets und Kalender des Originals werden nicht angefasst.
- Das System gilt **ab Installation** der neuen Version (Startdatum); bisherige Zahlen bleiben.

### 4.2 Energie und Freezes
- **+1 ⚡** pro aktivem Tag, **keine Obergrenze** (Energie wird in Phase 4 für den Avatar ausgegeben).
- **Anzeige als Wasser (#112):** In der App heißt die Energie „Wasser“ mit Symbol 💧 (passend zum Baum). Code, Schlüssel und Backup-Format behalten den Namen *energy*; ⚡ in dieser SPEC meint 💧.
- **Tagesbonus (#111):** +1 ⚡ zusätzlich an einem aktiven Tag, an dem mindestens 2 verschiedene Routinen für dieses Datum erledigt sind (messbare ab Zielwert). Höchstens 1 Bonus pro Tag. Wird wie die Tagesenergie aus den Einträgen berechnet (`DailyBonus`, `BonusDaySource`): Haken zurücknehmen nimmt den Bonus zurück, Backups bringen ihn mit.
- **Bonus-Popup (#136):** Das Abhaken, das den Tagesbonus auslöst, zeigt nach dem Motivations-Popup ein eigenes goldenes Vollbild-Popup (`BonusPopup`, `BonusPopupHost`) mit Wassermünze, „+1“ und neuem Wasserstand. Gleiche Regeln wie das Motivations-Popup: nur heute/gestern, aus mit dem Popup-Schalter.
- **Freeze = 7 ⚡**, überbrückt einen verpassten Tag der Aktiv-Serie, **max. 2 Tage am Stück**.
- Freezes werden **automatisch** eingesetzt, sobald genug Energie da ist (Abschalten: Backlog #30).
- Heute noch kein Eintrag → die Serie gilt bis Tagesende als nicht gerissen.
- Ein gefrorener Tag **hält** die Serie, **zählt aber nicht mit** (7 aktiv + 1 gefroren + 1 aktiv = 8).
- Eine Lücke wird nur **als Ganzes** gefroren: höchstens 2 Tage und genug Energie für alle Tage der
  Lücke. Sonst reißt die Serie, und es wird **keine** Energie verbraucht. Weil alles berechnet wird,
  gilt das auch rückwirkend: Wird aus einer 2-Tage-Lücke eine 3-Tage-Lücke, sind die beiden
  Freezes wieder frei.
- Reißt die Serie, **bleibt die Energie** erhalten.

### 4.3 Datenhaltung
- **Alles berechnet:** `ActivityPlanner` (reine Funktion in `shared:core`) spielt die aktiven Tage ab
  dem Startdatum chronologisch ab und liefert Aktiv-Serie, längste Aktiv-Serie, Kontostand,
  gefrorene Tage und den Tagesstatus der letzten Tage (#23).
- **Gespeichert** wird nur: Startdatum, Liste der aktiven Tage, bereits gesehene Freezes – in einem
  **eigenen DataStore** des Forks, nicht in der Datenbank des Originals (keine Kollision mit dessen
  Schema-Versionen) (#24).
- Ein Häkchen speichert nur, *für* welchen Tag es gilt, nicht *wann* es gesetzt wurde. Deshalb
  vermerkt das Repository beim Speichern eines Häkchens den **heutigen** Tag als aktiv
  (`HabitRepository.insertHabitStatus`, kleine Änderung im Original) (#24).
- Die drei gespeicherten Werte kommen ins Backup (#28).
- Umsetzung (#28): `ExportSchema` hat das optionale Feld `activity` (`ActivitySchema` in
  `logic/core/backup/`, Daten als ISO-Text). Ältere Backups und Backups des Originals haben es
  nicht und bleiben importierbar; umgekehrt ignoriert das Original das Feld. Beim Import wird der
  Stand vor dem Import mit dem Backup **zusammengeführt** (`ActivityData.mergedWith`: früheres
  Startdatum, Tage vereinigt), wie beim Import der Routinen. Weil das Wiederherstellen der Häkchen
  über `insertHabitStatus` läuft und dabei heute als aktiv vermerkt, ersetzt `RestoreImpl` danach
  den gespeicherten Stand (`ActivityStore.replaceAll`): Ein Import allein macht heute nicht aktiv.
- Umsetzung (#24): Schnittstellen `ActivityStore` und `ActivityRepo` in `core.activity`;
  `ActivityStoreImpl` nutzt die **bestehende** Preferences-Datei der App mit den Schlüsseln
  `activity_start_date`, `activity_days`, `activity_seen_freezes`. `ActivityRepository` liefert den
  Bericht als Flow.
- Startdatum: wird beim ersten Abruf des Berichts gesetzt oder – falls zuerst über Widget/Erinnerung
  eingetragen wird – beim ersten aktiven Tag.
- Der Bericht wird neu berechnet, wenn sich die gespeicherten Tage ändern oder die App ihn neu
  abruft. Über Mitternacht offen gelassen zeigt die App bis dahin den Stand von gestern (bewusst
  hingenommen).

### 4.4 UI
- **Kopfzeile der Routinen-Seite:** „🔥 12 Tage aktiv · ⚡ 14“; Antippen öffnet die Energie-Seite (#25).
- **Energie-Seite:** Kontostand, aktuelle und längste Aktiv-Serie, Leiste der letzten 30 Tage
  (✓ aktiv / ❄️ gefroren / – verpasst), kurze Erklärung der Regeln. Kein Shop-Platzhalter, aber so
  aufgebaut, dass in Phase 4 ein Shop darunter passt. Navigation des Originals bleibt unverändert (#25).
- Umsetzung (#25): `HabitViewModel` beobachtet `ActivityRepo` und legt den Bericht in
  `HabitState.activity`. Die Zeile steht als `ActivityHeaderLine` unter „x/y erledigt“ in der
  Kopfzeile von `HabitsList`. Die `EnergyPage` (`UI/habit/ui/sections/`) ist eine Detailseite der
  Routinen (Route `Energy` in `navigation/ForkRoutes.kt`), wie die Gesamt-Statistik. Die Leiste zeigt
  10 × 3 Felder, älteste oben links, heute unten rechts (Rahmen, solange heute noch offen); Tage vor
  dem Startdatum bleiben leer (`recentDays()`). Die Regeln nehmen die Zahlen aus `ActivityPlanner`.
  Texte in `energy_strings.xml` (DE + EN), `ForkTranslationTest` prüft alle eigenen String-Dateien.
- **Freeze-Hinweis beim Öffnen**, nur wenn ein Freeze die Serie gerettet hat; kein Hinweis, wenn die
  Serie gerissen ist (#26).
- Umsetzung (#26): `FreezeNotice.of()` (`core.activity`) sammelt die gefrorenen Tage der **aktuellen**
  Serie, die noch nicht in `activity_seen_freezes` stehen, zu einem gemeinsamen Hinweis (Serie,
  −Energie, Rest). Serie 0 oder Freezes vor dem letzten verpassten Tag → kein Hinweis.
  `ActivityRepo.getFreezeNotice()` liefert ihn als Flow, `HabitViewModel.freezeNotice` reicht ihn an
  den app-weiten `FreezeNoticeHost` (neben `MotivationPopupHost` in `GritNavDisplay`). Der Knopf
  sendet `DismissFreezeNotice`, das die Tage als gesehen speichert. Weil der Planer eine offene Lücke
  bis gestern schon beim Öffnen einfriert, erscheint der Hinweis am Tag nach dem verpassten Tag.
- **Motivations-Pop-up:** beim ersten Eintrag eines Tages zusätzlich „+1 ⚡ · 13 Tage aktiv“ (#27).
- Umsetzung (#27): `HabitViewModel` prüft vor dem Speichern eines Häkchens, ob heute im Bericht von
  `ActivityRepo` schon aktiv ist. Wenn nicht, liest es nach dem Speichern die Aktiv-Serie und legt
  sie in `MotivationPopupState.activityStreak`; `HabitMotivationPopup` zeigt dann die Zeile
  `energy_popup_line` unter dem Titel. Gilt auch für Nachträge für gestern (sie machen heute aktiv).
  Bleibt das Pop-up stumm (abgeschaltet, alter Nachtrag, Serie 0), entfällt auch die Zeile.

### 4.5 Issues
| # | Inhalt |
|---|---|
| #23 | `ActivityPlanner` (Domain, test-first) |
| #24 | DataStore + Erfassung aktiver Tage |
| #25 | Kopfzeile + Energie-Seite |
| #26 | Freeze-Hinweis beim Öffnen |
| #27 | „+1 ⚡“ im Motivations-Pop-up |
| #28 | Backup |
| #29 | Abnahme auf dem Gerät |
| #30 | Backlog: Freezes abschaltbar (Phase 4) |

---

## 5. Roadmap

| Phase | Inhalt | Tracking |
|---|---|---|
| 1 | Motivations-Pop-up | abgeschlossen (Meilenstein „Phase 1“, #1–#9) |
| 2 | Energie & Aktiv-Serie | Meilenstein „Phase 2“, Epic #14, Abschnitt 4 |
| 3 | Statistiken | Meilenstein „Phase 3“, Epic #10, Abschnitt 6 |
| 4 | Avatar (wachsende Pflanze) | Meilenstein „Phase 4“, Epic #15, Abschnitt 7 |
| 5 | Avatar-Reiter, Routinen mit Farbe und Icon, Aufgaben entfernen | Epic #76, Abschnitt 8 |

Reihenfolge und Abhängigkeiten:

- **Phase 4 nach Phase 2:** Der Avatar baut auf der Energie und den aktiven Tagen auf; Energie wird
  dort für Gegenstände ausgegeben (gespeicherte Ausgaben, Kontostand bleibt berechnet).
  Klein anfangen (Stufen/Aussehen), Training und Anpassung später.
- Phase 3 nutzt für zwei Statistiken (aktive Tage, Energie-Verlauf) den Aktiv-Bericht aus Phase 2;
  die Serien der einzelnen Routinen bleiben unverändert.
- Epics werden erst kurz vor Beginn der jeweiligen Phase per Design-Interview in Teilaufgaben
  zerlegt.

### Architektur-Regeln für alle Phasen

- `core.motivation` kennt weder Room noch DAOs. Neue SQL-Abfragen, Tabellen und State-Felder
  späterer Phasen dürfen die Motivations-Engine nicht berühren.
- Änderungen an Upstream-Dateien (z. B. Streak-Funktionen, Datenbank-Definition) möglichst klein
  halten, neue Logik in neue Dateien legen (siehe `docs/WORKFLOW.md`).
- Neue Room-Tabellen brauchen eine Migration, bestehende Nutzerdaten dürfen nicht verloren gehen.

## 6. Phase 3 – Statistiken

Design-Interview abgeschlossen am 2026-10-22. Epic #10, Meilenstein „Phase 3 – Statistiken“.

### 6.1 Auswahl und Platzierung
| Nr. | Statistik | Ort | Datenquelle |
|---|---|---|---|
| 1 | Aktive Tage pro Monat (letzte 6 Monate, Balken) | Energie-Seite | Aktiv-Bericht (Phase 2) |
| 6 | Energie-Verlauf: verdient ⚡ / durch Freezes verbraucht ❄️ pro Monat (letzte 6 Monate) | Energie-Seite | Aktiv-Bericht (Phase 2) |
| 2 | Monatsrückblick: Erfüllungsquote dieser Monat vs. Vormonat | Gesamt-Statistik | Häkchen |
| 3 | Serien-Überblick: alle Routinen nach aktueller Serie, mit Bestwert | Gesamt-Statistik | `HabitWithAnalytics` |
| 5 | Häkchen pro Woche über alle Routinen (letzte 12 Wochen) | Gesamt-Statistik | Häkchen |
| 4 | Comeback-Zähler | Routinen-Statistik + Summe in der Gesamt-Statistik | Häkchen |

- **Feste Zeiträume**, kein Umschalter (kann später kommen).
- **Keine Uhrzeit** speichern, keine Änderung an der Datenbank des Originals.
- Auf der Energie-Seite stehen die neuen Karten zwischen „Letzte 30 Tage“ und den Regeln.

### 6.2 Definitionen
- **Aktive Tage pro Monat:** Anzahl Tage mit Status `ACTIVE` im Aktiv-Bericht. Monate vor dem
  Startdatum werden leer dargestellt, nicht als 0.
- **Energie-Verlauf:** verdient = aktive Tage × `ENERGY_PER_ACTIVE_DAY`, verbraucht = gefrorene Tage ×
  `FREEZE_COST`, jeweils pro Monat.
- **Erfüllungsquote:** erledigte / geplante Tage über alle Routinen. Geplant sind nur die Wochentage
  der Routine, ab ihrem ersten Häkchen und höchstens bis heute (wie `calculateConsistency`). Ein
  Häkchen zählt, sobald ein Status existiert. Ohne geplante Tage → kein Wert (Hinweis statt 0 %).
- **Comebacks:** Anzahl Serien einer Routine, die nach mindestens einem verpassten geplanten Tag neu
  begonnen haben (= Anzahl Serien − 1, nie negativ). 0 Comebacks wird neutral angezeigt, nie als Tadel.
- **Häkchen pro Woche:** Anzahl Status pro Woche; Wochenbeginn nach der Einstellung „Erster Wochentag“
  (`HabitState.startingDay`), aktuelle Woche zuletzt.

### 6.3 Architektur
- Berechnungen als reine Funktionen in `com.shub39.grit.core.stats` (`shared:core`), `today` als
  Parameter, Tests in `commonTest` (#41).
- Kein eigenes Repository: Alle Daten liegen schon in `HabitState` (`habitsWithAnalytics` mit allen
  Häkchen, `activity`, `startingDay`). `ForkStats.of(...)` (`core.stats`) berechnet daraus alle Werte;
  `HabitViewModel.observeStats()` rechnet neu, sobald sich eins davon ändert, und legt das Ergebnis in
  `HabitState.stats` (#42).
- UI in eigenen Dateien (z. B. `ForkOverallStats.kt`), in Upstream-Dateien wie `OverallAnalytics.kt`
  und `AnalyticsPage.kt` nur eingebunden. Texte in `stats_strings.xml` (DE + EN).
- `core.motivation` bleibt unberührt.

### 6.4 Issues
| # | Inhalt |
|---|---|
| #41 | Domain: Berechnungen in `core.stats` (test-first) |
| #42 | `ForkStats` + `HabitState.stats` |
| #43 | Energie-Seite: aktive Tage pro Monat, Energie-Verlauf |
| #44 | Gesamt-Statistik: Monatsrückblick, Serien-Überblick, Häkchen pro Woche |
| #45 | Comeback-Zähler |
| #46 | Abnahme auf dem Gerät – übersprungen (Entscheidung 2026-10-22) |

---

## 7. Phase 4 – Avatar (wachsende Pflanze)

Design-Interview abgeschlossen am 2026-10-22. Epic #15, Meilenstein „Phase 4 – Avatar“.

### 7.1 Grundidee
- Ein **Pflänzchen**, das mit der **Gesamtzahl aktiver Tage** (Status `ACTIVE` im Aktiv-Bericht,
  gefrorene Tage zählen nicht) in **12 Stufen** zum großen Baum wächst.
- Energie wird nur im **Shop** ausgegeben; die Stufe hängt nicht an Energie.
- Serie gerissen (Aktiv-Serie 0) → die Pflanze wirkt **müde** (hängende Blätter, blasser).
  **Nie Rückstufung.** Sobald die Serie wieder läuft, ist sie wieder fröhlich.
- Selbst gezeichnet in Compose (`Canvas`, Vektor), keine fremden Assets, keine Lizenzfragen.
  Leichte Animation (Wiegen), aus bei „Animationen entfernen“.
- Anzeige **oben auf der Energie-Seite**, darunter Kontostand, Statistiken und der Shop.
  Pop-up oder Widget eventuell später.

### 7.2 Stufen
| Stufe | Name (DE) | ab aktiven Tagen |
|---|---|---|
| 1 | Samen | 0 |
| 2 | Keim | 3 |
| 3 | Keimling | 7 |
| 4 | Sprössling | 14 |
| 5 | Pflänzchen | 21 |
| 6 | Jungpflanze | 30 |
| 7 | Bäumchen | 45 |
| 8 | Junger Baum | 60 |
| 9 | Baum | 90 |
| 10 | Großer Baum | 150 |
| 11 | Blühender Baum | 240 |
| 12 | Mächtiger Baum | 365 |

Die Schwellen stehen nur in `PlantStage` (`core.avatar`). Bis Stufe 7 steht die Pflanze im Topf,
ab Stufe 8 auf einer Wiese (Topffarbe dann als Farbe der Bank/des Schildes neben dem Baum).

### 7.3 Shop (Vorschlag, Preise in `ShopCatalog`)
| Art | Gegenstand | Preis | ab Stufe |
|---|---|---|---|
| Topffarbe | Terrakotta (Standard) | 0 | 1 |
| Topffarbe | Blau, Gelb, Grün | je 10 ⚡ | 1 |
| Topffarbe | Lila | 15 ⚡ | 3 |
| Topffarbe | Gold | 40 ⚡ | 6 |
| Accessoire | Gießkanne | 25 ⚡ | 1 |
| Accessoire | Marienkäfer | 15 ⚡ | 2 |
| Accessoire | Schmetterling | 20 ⚡ | 4 |
| Accessoire | Blüten | 30 ⚡ | 5 |
| Accessoire | Vogel | 40 ⚡ | 8 |
| Accessoire | Vogelhäuschen | 50 ⚡ | 9 |
| Accessoire | Früchte | 60 ⚡ | 10 |
| Accessoire | Lichterkette | 70 ⚡ | 10 |
| Accessoire | Schaukel | 80 ⚡ | 11 |

- Jeder Gegenstand nur einmal; Topffarbe: genau eine gewählt, Accessoires: beliebig viele.
- Kauf nur bei genug Energie und erreichter Stufe, mit Bestätigung; Hinweis, wenn danach weniger als
  ein Freeze (7 ⚡) übrig ist.

### 7.4 Kontostand und Freezes
- **Kontostand bleibt berechnet.** Gespeichert werden nur die Käufe (`itemId`, Datum).
  `ActivityPlanner` bucht jeden Kauf an seinem Tag ab, **nach** den Freezes der Lücken, die bis dahin
  geschlossen sind. Ohne Käufe ist das Ergebnis exakt wie in Phase 2.
- Ein Kauf macht frühere Freezes nie rückgängig; spätere Lücken werden nur gefroren, wenn danach noch
  genug Energie da ist (Regel aus 4.2).
- Der Kontostand fällt nie unter 0: Bringt ein Backup-Import einen Kauf mit, der hier nicht bezahlbar
  war, bleibt der Gegenstand erhalten und die Differenz verfällt (#68).
- **#30:** Schalter „Freezes automatisch einsetzen“ auf der Energie-Seite, Standard an. Gespeichert mit
  Datum der Änderung; aus → ab diesem Tag keine Freezes, frühere bleiben.

### 7.5 Datenhaltung
- `AvatarStore` (`core.avatar`), Implementierung in `logic/avatar/` über die bestehende
  Preferences-Datei: `avatar_name`, `avatar_purchases`, `avatar_equipped`; dazu `activity_auto_freeze`.
- Backup: optionales Feld `avatar` im `ExportSchema`, Import führt zusammen (Käufe vereinigt, Name aus
  dem Backup, falls lokal leer). Ältere Backups bleiben importierbar.
- Standardname „Grit“, umbenennbar per Tippen auf den Namen.

### 7.6 Issues
| # | Inhalt |
|---|---|
| #48 | Domain: Wachstumsstufen und Stimmung |
| #49 | Domain: Shop, Käufe, Kontostand |
| #50 | Avatar-Daten speichern + Backup |
| #51 | Pflanze zeichnen (12 Stufen) |
| #52 | Energie-Seite: Pflanze, Name, Fortschritt |
| #53 | Shop-UI |
| #30 | Freezes abschaltbar |
| #54 | Abnahme auf dem Gerät – übersprungen (Entscheidung 2026-10-22) |

---

## 8. Phase 5 – Avatar-Reiter, Routinen mit Farbe und Icon

Abgestimmt am 2026-10-26. Epic #76, Label `phase-5`.

### 8.1 Aufgaben entfernen (#77)
- Die Aufgaben-Funktion (Seite, Listen, Unteraufgaben, Erinnerungen, Widget „Alle Aufgaben“,
  Einstellungen „Startseite“ und „Aufgaben sortieren“) wird aus dem Code **gelöscht**, nicht nur
  ausgeblendet. Die App startet immer bei den Routinen.
- Die alte Datenbankdatei `task_database` wird beim App-Start gelöscht (`deleteDatabase`, danach
  wirkungslos).
- Backup: Export schreibt keine Aufgaben mehr. `tasks`, `categories`, `subTasks` und
  `tasksSchemaVersion` bleiben als optionale Felder im `ExportSchema`, damit ältere Backups
  importierbar bleiben; der Import überspringt sie und prüft nur noch `habitsSchemaVersion`.
- Upstream-Syncs: Konflikte an gelöschten Aufgaben-Dateien („modify/delete“) werden mit „bleibt
  gelöscht“ gelöst (siehe `docs/WORKFLOW.md`).

### 8.2 Reiter „Avatar“ (#78)
- Untere Leiste bzw. Seitenleiste: **Routinen | Avatar | Einstellungen**.
- Avatar-Seite: Kontostand oben, darunter Pflanze mit Name und Fortschritt, darunter der Shop. Die
  Energie-Seite zeigt danach nur noch Kontostand und Statistiken.

### 8.3 Farbe und Icon pro Routine (#79, #80)
- 24 Farben und 36 Icons, Standard **Grün + Blitz**. Gespeichert im Preferences-DataStore
  (`routine_styles`, Einträge `habitId|colorKey|iconKey`), keine Änderung am Room-Schema.
  Unbekannte Schlüssel fallen auf den Standard zurück.
- Backup: optionales Feld `routineStyles` im `ExportSchema`.
- Auswahl im Formular zum Anlegen/Bearbeiten einer Routine (Vorschau-Kachel neben dem Namen).

### 8.4 Neue Routinen-Karte (#81)
- Kopfzeile mit Name, Flamme mit Serie und Statistik-Knopf; links eine Kachel mit Icon in der Farbe
  der Routine; rechts 2 × 7 Kästchen für die **letzten 14 Kalendertage** (oben links der älteste,
  unten rechts heute), erfüllte Tage in der Farbe der Routine gefüllt.
- Die Auswahl der Tage und ihr Zustand werden als reine Funktion in `core` berechnet und getestet.

### 8.5 Deko für die Wiese (#82: #101, #103, #104)
Abgestimmt am 2026-10-29 (Entwurf: https://claude.ai/artifact/Mw37mjDy9J1AbhEHHJL8C5).
- Die Pflanzen-Karte zeigt eine **breite Wiese (2 : 1)**, schon ab Stufe 1; die Pflanze bleibt in
  der Mitte, der Topf steht im Gras. Gezeichnet in `MeadowScene` (Compose, Sticker-Stil).
- 8 Deko-Teile mit **festem Platz**, auf der abfallenden Wiesenkante stehend (Preis ⚡ / ab Stufe):
  Pilze 10/1, Gartenzaun 15/2, Schnecke 15/3, Igel 25/5, Hase 30/6, Parkbank 40/8, Teich 50/9,
  Laterne 45/10.
- Kauf im Shop (Kategorie „Deko“, `ShopItemKind.DECORATION`); gekaufte Teile lassen sich aufstellen
  und wegnehmen. Eingeblendete Teile im DataStore unter `avatar_decorations`, im Backup als
  optionales Feld `decorations` im Avatar.
- Bei gerissener Serie schlafen die Tiere mit. Dezente Bewegung (Hase, Schnecke, Igel, Teich,
  Laterne) folgt der Animations-Einstellung.
- Hintergrund (#107): hellblauer Himmel (#90D5FF) mit Wolken statt des Karten-Hintergrunds. Das
  Wetter (`Weather`) hängt an der Serie: ab 7 Tagen große Sonne mit wenigen Wolken (SUNNY), ab
  3 Tagen Sonne mit Wolken (FAIR), 1–2 Tage oder noch nichts getan: Sonne halb hinter einer Wolke
  (HAZY). Nach gerissener Serie keine Sonne: 1–6 inaktive Tage bewölkt, Himmel etwas grauer
  (CLOUDY); ab 7 inaktiven Tagen dicht bewölkt und grau (OVERCAST). Wolken treiben langsam, die
  Sonnenstrahlen drehen sich (folgt der Animations-Einstellung).

### 8.5a Vorschau vor dem Kauf (#121)
- Jedes noch nicht gekaufte Teil hat im Shop einen Vorschau-Knopf (Auge); er öffnet das Kauffenster
  auch bei zu wenig Wasser oder zu niedriger Stufe, Kaufen geht dann nicht (Hinweis warum).
- Das Kauffenster zeigt die Wiese wie auf der Avatar-Karte mit Umschalter **Ohne / Mit** (Standard:
  Mit). Gesperrte Teile werden an der Pflanze auf der benötigten Stufe gezeigt.
- Berechnung als reine Funktion `AvatarState.preview` in `core.avatar` (`ShopPreview`).

### 8.5b Wiese als ganze Seite, eigener Shop, Saison (#138)
Abgestimmt am 2026-11-03 (Vorschau: https://claude.ai/artifact/7ZA5C48eovNtma5vVGcedt, Avatar-Seite B
mit Namensschild in der Mitte, Shop A).
- Avatar-Reiter: `MeadowScene` füllt die ganze Seite (höchstens 600 dp breit, `sceneHeight`, Sonne
  und Wolken per `skyTop` unter der Leiste). Oben eine Leiste: links Wasser, in der Mitte das
  Namensschild (Name ✎ zum Umbenennen, Fortschrittsbalken `PlantState.stageProgress`, Stufe und Tage
  bis zur nächsten), rechts ein runder Shop-Knopf. Darunter der Hinweis „müde“.
- Shop als eigene Seite (`PlantShop`, Zurück führt zum Avatar-Reiter): oben die eigene Wiese,
  darunter Reiter Töpfe | Zubehör | Deko | Saison; Zeilen, Vorschau und Kauf wie bisher.
- Saisonteile: `ShopItem.season` (`SaleWindow`, jährlich, darf über Neujahr gehen). Außerhalb des
  Zeitraums verschwinden nicht gekaufte Saisonteile aus dem Shop (`Shop.offered`) und sind nicht
  kaufbar (`PurchaseCheck.OUT_OF_SEASON`); gekaufte bleiben für immer. Der Reiter „Saison“ erscheint
  nur, wenn es gerade Saisonteile gibt oder welche gekauft wurden. Noch keine Saisonteile im Katalog.

### 8.5c Dein Name (#139)
Abgestimmt am 2026-11-03 (Vorschau: https://claude.ai/artifact/4yCPNBHmtM9TUwaiFTvaj2, Einrichtung A,
Anrede A).
- Beim allerersten Start (`FirstLaunchSetup.isDue`: Einrichtung nicht erledigt, noch nie begrüßt,
  keine Routinen) fragt der Samen vor der Willkommens-Begrüßung auf eigener Bühne „Wie darf ich
  dich nennen?“ (Weiter / Überspringen, Zurück-Taste überspringt). Bestehende Installationen sehen
  den Schritt nie (`user_setup_done` wird gesetzt). Danach folgt die Avatar-Wahl (#140, siehe 8.5d).
- Der Name (`AvatarData.userName`, DataStore `avatar_user_name`, Backup-Feld `userName` im Avatar,
  höchstens 24 Zeichen) steht nur in Überschriften: Tagesbegrüßung („Guten Morgen, Lena“),
  Willkommen („Hallo Lena, ich bin Sprout!“) und Motivations-Pop-up („Starkes Ding, Lena!“). Die
  Sprüche bleiben unverändert. Ohne Namen sieht alles aus wie vorher.
- Ändern unter Einstellungen → „Dein Name“; leer entfernt den Namen.

### 8.5d Zweiter Avatar: Kirschbaum (#140)
Abgestimmt am 2026-11-03 (Vorschau: https://claude.ai/artifact/SDLX9aPNqxjXpvyfYmYwX5, Kandidat C,
Standardname „Kirsche“).
- `AvatarSpecies` (SPROUT, CHERRY) in `AvatarData.species`, DataStore `avatar_species`, Backup-Feld
  `species` im Avatar (fehlt es, gilt Sprout). Alle Arten durchlaufen dieselben 12 Stufen und tragen
  dieselben Shop-Teile.
- Der Kirschbaum nutzt Sprouts Gerüst (Stamm, Ast-Arme, Gesicht, Zubehör-Plätze) mit eigenen
  Farben: dunklerer Stamm mit hellen Rindenstreifen, holziger Zweig mit rosa Knospen in den
  kleinen Stufen, rosa Blütenkrone mit dunkleren Blüten ab dem Bäumchen (mehr Blüten je Stufe),
  fallende Blütenblätter, Kirschpaare am mächtigen Baum.
- Wahl als zweiter Schritt der Einrichtung beim ersten Start („Wer soll mit dir wachsen?“, nach dem
  Namen) und jederzeit unter Einstellungen → „Dein Avatar“ (Bottom Sheet, Tippen wechselt sofort).
  Beim Wechsel bleiben Stufe, Wasser und gekaufte Teile erhalten, nur das Aussehen ändert sich. Ein
  nicht umbenannter Avatar heißt nach der Art („Sprout“ oder „Kirsche“).

### 8.6 Issues
| # | Inhalt |
|---|---|
| #77 | Aufgaben-Funktion vollständig entfernen |
| #78 | Eigener Reiter „Avatar“ |
| #79 | Domain + Speicher: Farbe und Icon pro Routine |
| #80 | Auswahl von Farbe und Icon im Formular |
| #81 | Neue Routinen-Karte |
| #82 | Deko für die Wiese (Teich, Tiere, Bank), aufgeteilt in #101, #103, #104 |
| #101 | Deko: Katalog, Kauf, Ein-/Ausblenden, Speicherung und Backup |
| #103 | Deko: breite Wiese mit gezeichneten Deko-Teilen |
| #104 | Deko: Shop-Kategorie „Deko“ |
| #107 | Wiese: hellblauer Himmel mit Wolken und Sonne je nach Serie |
| #121 | Shop: Vorschau vor dem Kauf für jedes Teil |
| #138 | Wiese als ganze Seite, Shop als eigene Seite, Saison-Mechanik |
| #139 | Einrichtung beim ersten Start: eigener Name (und Avatar-Wahl) |
| #140 | Zweiter Avatar zur Auswahl |

---

## 9. Offene Fragen

- ~~Soll das Pop-up per Einstellung abschaltbar sein?~~ Umgesetzt in #9 (siehe 3.3).
- ~~Pop-up beim Abhaken eines vergangenen Tages ohne laufende Serie?~~ Entschieden: kein Pop-up bei
  Streak 0 (siehe 3.3).
