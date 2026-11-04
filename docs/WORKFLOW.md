# Arbeitsweise & Version Control

## Remotes

| Remote | URL | Zweck |
|---|---|---|
| `origin` | `github.com/CMkc34/grit-motivation` (privat) | Unser Repo – hierhin wird gepusht |
| `upstream` | `github.com/shub39/Grit` | Original – nur lesen (Push-URL ist auf `DISABLED` gesetzt) |

## Branches (GitHub Flow)

- `master` ist immer baubar. Nie direkt darauf entwickeln.
- Pro Issue ein Branch von `master`:
  `feat/<issue-nr>-kurzbeschreibung`, `fix/...`, `chore/...`, `test/...`, `docs/...`
  Beispiel: `feat/3-quote-engine`
- Pull Request nach `master`, CI muss grün sein, dann **Squash-Merge**
  (Branch wird automatisch gelöscht).
- PR-Beschreibung verlinkt das Issue mit `Closes #<nr>`.

## Commits – Conventional Commits

```
<typ>(<bereich>): <kurz, Imperativ, englisch oder deutsch – aber konsistent>

Optionaler Body: warum, nicht was.
```

Typen: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `ci`, `style`.
Bereiche: `core`, `ui`, `logic`, `app`, `ci`.

Beispiel: `feat(core): add GritQuoteEngine with streak-based quote selection`

Kleine Commits, die jeweils für sich kompilieren.

## Vor jedem Push

```bash
./gradlew spotlessApply
./gradlew :androidApp:assembleFossDebug
./gradlew :shared:core:jvmTest
```

## Upstream-Updates einspielen

Upstream entwickelt auf `dev` und veröffentlicht Releases auf `master`. Wir folgen `upstream/master`.

```bash
git fetch upstream
git switch master
git pull
git switch -c chore/sync-upstream-<version>
git merge upstream/master
# Konflikte lösen, bauen, testen
git push -u origin HEAD
```

Dann PR öffnen. Hinweis: Für diesen PR muss in den Repo-Einstellungen kurz „Allow merge commits“
aktiviert werden (ein Squash würde die Upstream-Historie plattdrücken und spätere Syncs erschweren) –
alternativ lokal `git switch master && git merge --ff-only chore/sync-upstream-<version> && git push`.

**Gelöschte Aufgaben-Funktion (Phase 5, #77):** Ändert Upstream Dateien, die wir gelöscht haben
(`core/tasks`, `logic/tasks`, `ui/task`, `TasksViewModel`, Aufgaben-Widget, Task-Schema in
`shared/logic/schemas`), meldet Git einen „modify/delete“-Konflikt. Auflösung: Datei bleibt gelöscht
(`git rm <datei>`). Neue Upstream-Verweise auf Aufgaben in anderen Dateien ebenfalls entfernen.

Damit Merges konfliktarm bleiben: Upstream-Dateien nur minimal ändern, neue Logik in **neue Dateien**
legen (z. B. `core/motivation/`).

## GitHub Actions

- `ci.yml` (unser Workflow): Spotless, Unit-Tests, `assembleFossDebug` bei jedem PR und Push auf `master`.
- Die Upstream-Workflows `beta.yml`, `foss-release.yml`, `play-release.yml` sind im Repo
  **deaktiviert** (sie brauchen Signier-Keys des Original-Autors). Dateien bleiben unverändert,
  damit Upstream-Merges nicht kollidieren.
