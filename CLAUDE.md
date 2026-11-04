# CLAUDE.md – grit-motivation

Private fork of shub39/Grit. Read `docs/SPEC.md` (requirements, file locations, architecture
decisions) and `docs/WORKFLOW.md` (branching, commits, upstream sync) before changing code.

- Build only the `foss` flavor: `./gradlew :androidApp:assembleFossDebug`. Never touch the `play` flavor.
- Run `./gradlew spotlessApply` before committing (ktfmt + GPL license header).
- New fork-specific logic goes into new files (e.g. `shared/core/.../core/motivation/`) to keep
  upstream merges conflict-free; edit upstream files only where wiring is unavoidable.
- `shared:ui` depends on `shared:core` only, not on `shared:logic`.
- UDF: UI sends `HabitsAction`s, `HabitViewModel` owns `HabitState`. No business logic in composables.
- Pure Kotlin in `core` gets unit tests in `commonTest` (run with `:shared:core:jvmTest`).
- The upstream `AGENTS.md` rule about issues/PRs refers to the upstream repository. In this private
  repo, work is tracked with GitHub issues and PRs as described in `docs/WORKFLOW.md`.
