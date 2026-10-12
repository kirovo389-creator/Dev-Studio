# DevStudio

An Android Java coding-workspace prototype with a dark three-pane editor, GitHub-backed projects, and a configurable OpenAI-compatible AI provider.

## Create and open projects

- **CREATE PROJECT** creates a private GitHub repository and writes a starter Android Gradle project into it.
- **OPEN PROJECT** loads text files from an accessible GitHub repository.
- **SAVE TO GITHUB** commits the selected file.
- **BUILD** dispatches the GitHub Actions workflow, which builds a debug APK artifact.

### GitHub token permissions

The token must be able to create repositories and write repository contents. For fine-grained personal access tokens, grant repository **Administration: Read and write**, **Contents: Read and write**, and **Actions: Read and write**. For classic tokens, use an appropriate repo scope. Keep the token private; DevStudio stores it using AndroidX Security Crypto.

## AI provider settings

AI setup is optional. Open **AI Agent → SETTINGS** whenever you want to enter or change:
- **BASE URL**, e.g. `https://api.openai.com/v1`
- **API KEY**
- **MODEL ID**, e.g. `gpt-4o-mini`

The endpoint must be compatible with the OpenAI Chat Completions API. API usage may cost money.

## Build APK

GitHub Actions builds a debug-signed APK on pushes to `main` and on manual workflow dispatch. Open **Actions → Build DevStudio APK → Artifacts** to download it. It is debug-signed, not a production release signed with your own private signing key.

## Limitations

DevStudio is a prototype, not a 1:1 replacement for Android Studio. It does not currently include an Android emulator, full Java/Kotlin language server, visual layout designer, debugger, or full Gradle sync UI. The file browser and editor support text files; large/binary assets are not editable in the app.

## Stack

- Java
- Android Gradle Plugin 8.7.3
- Gradle 8.9
- minSdk 26 / compileSdk 35
