# Flow Viz

Kotlin Coroutines `Flow`를 **마블 다이어그램 애니메이션**으로 보여주는 인터랙티브 학습 사이트.
Compose Multiplatform (wasmJs)로 만들었고, 실제 `flow{}` / `StateFlow` / `SharedFlow`를 브라우저에서 진짜로 돌려서 시각화한다.

> [`flow-study`](../flow-study) 콘솔 예제(01~07)의 웹 버전. 학습 겸 Compose+Flow 복습용.

## 실행 (개발 서버)

```bash
./gradlew wasmJsBrowserDevelopmentRun
```
→ webpack-dev-server가 뜨고 브라우저가 자동으로 열림 (핫 리로드 지원).

## 정적 빌드 (배포용)

```bash
./gradlew wasmJsBrowserDistribution
```
→ `build/dist/wasmJs/productionExecutable/` 에 `index.html` + `.wasm` + `flowviz.js` 생성.
   이 폴더를 그대로 GitHub Pages / Netlify 등 정적 호스팅에 올리면 됨.

## 요구사항
- JDK 17+ (Gradle 실행용)
- WasmGC 지원 브라우저: Chrome 119+, Firefox 120+, Safari 18.2+

## 로드맵
- [x] **v0 (MVP)** — Cold Flow 씬: Collect A/B 버튼 + 구슬 pop-in 애니메이션 + 실행 로그
- [ ] v1 — Hot(StateFlow) / The Bug 씬 추가
- [ ] v2 — map/combine 연산자, stateIn 변환 씬
- [ ] v3 — 사이드바 네비게이션 + 씬별 "코드 보기" 패널

## 구조
```
src/wasmJsMain/
├── kotlin/
│   ├── Main.kt   # CanvasBasedWindow 진입점
│   └── App.kt    # Cold Flow 씬 (마블 다이어그램 + 컨트롤)
└── resources/
    └── index.html
```

## 스택
Kotlin 2.1.0 · Compose Multiplatform 1.7.3 · kotlinx-coroutines 1.9.0 · Gradle 8.13 (버전은 `gradle/libs.versions.toml`)
