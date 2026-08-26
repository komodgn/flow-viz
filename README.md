# Flow Viz

An interactive playground that visualizes Kotlin Coroutines `Flow` as **animated marble diagrams**.
Built with Compose Multiplatform (wasmJs) — it runs real `flow{}` / `StateFlow` / `SharedFlow`
in the browser and renders their emissions live, so you *see* how each one actually behaves.

> A web version of the [`flow-study`](../flow-study) console examples (01–07),
> built to learn Flow and Compose at the same time.

## Scenes

| Scene | What you can feel by clicking |
|-------|-------------------------------|
| **Cold Flow** | Nothing runs until you `collect`; each collector re-runs the block from the start |
| **Hot / StateFlow** | Conflation (same value isn't re-emitted) + a late subscriber gets the latest value instantly |
| **The Bug** | A real hang reproduced: `drop(1).first()` waits for an emit that never comes |
| **SharedFlow** | `replay=0` late subscribers miss past events; no conflation, so duplicates all arrive |
| **map / combine** | `map` transforms each value; `combine` recomputes when *any* input changes |
| **stateIn** | Cold→Hot conversion — one shared upstream, late subscriber starts from the current value |

## Run (dev server)

```bash
./gradlew wasmJsBrowserDevelopmentRun
```
Starts webpack-dev-server and opens the browser with hot reload.

> After changing `build.gradle.kts`, fully restart the dev server (Ctrl+C then re-run) and
> hard-reload the page (`Cmd+Shift+R`) — stale JS/wasm can otherwise cause a `LinkError`.

## Static build (for hosting)

```bash
./gradlew wasmJsBrowserDistribution
```
Outputs `index.html` + `.wasm` + `flowviz.js` to `build/dist/wasmJs/productionExecutable/`.
Serve that folder from any static host (Vercel, Cloudflare Pages, Netlify, GitHub Pages).

## Deployment

CI builds the wasm bundle and Vercel only serves it (Vercel has no JDK/Gradle, so it can't
build the project itself). See [`.github/workflows/deploy.yml`](.github/workflows/deploy.yml):
GitHub Actions runs `wasmJsBrowserDistribution`, then uploads the static output via the Vercel CLI.

Required GitHub Actions secrets: `VERCEL_TOKEN`, `VERCEL_ORG_ID`, `VERCEL_PROJECT_ID`.

## Requirements
- JDK 17+ (to run Gradle)
- A WasmGC-capable browser: Chrome 119+, Firefox 120+, Safari 18.2+

## Roadmap
- [x] **v0 (MVP)** — Cold Flow scene: Collect A/B buttons + marble pop-in animation + run log
- [x] **v1** — left sidebar navigation + Hot (StateFlow) / The Bug scenes
- [x] **v2** — SharedFlow (one-off events), map/combine, stateIn scenes
- [ ] **v3** — repeatOnLifecycle scene (lifecycle-scoped collection) + a "view source" panel per scene

## Project layout
```
src/wasmJsMain/
├── kotlin/
│   ├── Main.kt              # wasmJs entry point (CanvasBasedWindow)
│   ├── App.kt              # font setup, sidebar nav, scene switching
│   ├── Theme.kt            # color palette
│   ├── Components.kt       # shared UI (MarbleLane, LogPanel, SceneScaffold, ...)
│   ├── ColdFlowScene.kt
│   ├── HotFlowScene.kt
│   ├── BugScene.kt
│   ├── SharedFlowScene.kt
│   ├── MapCombineScene.kt
│   └── StateInScene.kt
├── composeResources/
│   └── font/               # bundled Pretendard (web can't use system fonts for Korean glyphs)
└── resources/
    └── index.html
```

## Stack
Kotlin 2.1.0 · Compose Multiplatform 1.7.3 · kotlinx-coroutines 1.9.0 · Gradle 8.13
(versions live in `gradle/libs.versions.toml`)

## Contributing
This is a personal learning project, but ideas, issues, and PRs are welcome —
especially new Flow scenes or clearer visualizations. Feel free to open an issue to discuss.
