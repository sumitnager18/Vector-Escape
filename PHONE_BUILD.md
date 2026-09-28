# Vector Escape — Phone Build

The repository is configured so the Android APK can be built in GitHub Actions without a local Android Studio installation.

## Build the APK

1. Open the repository on GitHub.
2. Open **Actions**.
3. Select **Vector Escape CI**.
4. Choose **Run workflow** (or push a commit to main).
5. Wait for the **Android debug APK** job to finish.
6. Open the completed workflow run.
7. Under **Artifacts**, download **vector-escape-debug-apk**.
8. Extract the ZIP and install **app-debug.apk** on the Android phone.

## Important

- This is a real Gradle/Android build, not a TypeScript-only validation.
- The workflow uses Java 17 and Gradle 8.11.1.
- If the Android build fails, the failure log is the source of truth.
