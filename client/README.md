# Client Kotlin Multiplatform

Compose Multiplatform app (Android, WebAssembly, JavaScript) for the recruiter chatbot.
See the [main README](../README.md) for the architecture, the tech stack and the build commands.

- `shared/` — shared code: Ktor client, SSE parser, `ChatViewModel`, Compose UI, FR/EN resources, tests
- `androidApp/` — Android entry point (`MainActivity`)
- `webApp/` — web entry point (`main.kt`), built with `composeCompatibilityBrowserDistribution`
