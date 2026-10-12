# DevStudio

A landscape Android coding workspace prototype written in Java. It includes a three-pane IDE-inspired interface, a project file list, editor, AI agent panel, encrypted local API-key storage, and a GitHub Actions APK build.

## Build

The GitHub Actions workflow builds a debug-signed APK on pushes to `main` or manually through Actions. Open **Actions → Build DevStudio APK → Artifacts** to download it.

## AI setup

On first launch, enter an OpenAI API key. API usage may cost money. The key is stored locally using AndroidX Security Crypto where available and is never committed to the repository.

## Current prototype limitations

- The editor workspace is currently local to the app; GitHub repository sync is not implemented yet.
- BUILD opens the repository Actions page; it does not yet upload the local editor workspace to GitHub.
- The generated artifact is Android's debug-signed APK, not a production release signed with your own private signing key.
- AI file actions currently support simple filenames in the workspace root.

## Stack

- Java
- Android Gradle Plugin 8.7.3
- Gradle 8.9
- minSdk 26 / compileSdk 35
