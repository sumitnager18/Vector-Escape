# VECTOR ESCAPE

Original directional-vector puzzle game project.

## Project
- Web prototype: React + TypeScript + Vite
- Android project: Kotlin + Jetpack Compose
- Core puzzle: directional vectors can exit only when their path to the board edge is clear.
- Campaign, procedural generation, solver, Daily Vector, Practice mode, persistence, audio and haptics are part of the project design.

## Source status
The repository is being initialized from the current Vector Escape project snapshot. The full source snapshot is kept separately so that no generated or machine-specific files are accidentally committed.

## Important
- Do not commit Android `local.properties`.
- Do not commit Gradle caches, build outputs, or secrets.
- The Android build should be verified in GitHub Actions before treating an APK as release-ready.
