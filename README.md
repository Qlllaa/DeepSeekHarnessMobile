# DeepSeek Harness Mobile

Android app that runs DeepSeek Harness inside a built-in Ubuntu environment using PRoot.

## Architecture

- Android 16 + ARM64
- Jetpack Compose + Material 3 UI
- Built-in Ubuntu ARM64 rootfs (downloaded on first launch)
- PRoot for Linux environment isolation
- Node.js + pnpm for Harness runtime
- Native Agent UI (not WebView)

## Project Structure

```
DeepSeekHarnessMobile/
├── app/
│   └── src/main/
│       ├── java/com/deepseek/harnessmobile/
│       │   ├── MainActivity.kt
│       │   ├── LinuxRuntimeService.kt
│       │   ├── RuntimeManager.kt
│       │   └── ProcessRunner.kt
│       ├── res/
│       └── AndroidManifest.xml
├── runtime/
│   ├── proot/arm64-v8a/
│   └── scripts/
│       ├── bootstrap.sh
│       ├── start-ubuntu.sh
│       └── start-harness.sh
├── harness/
│   └── deepseek-harness/
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## v0.1 Goals

1. APK builds and installs
2. Ubuntu rootfs downloaded and extracted
3. PRoot starts Ubuntu environment
4. Node.js + pnpm available
5. Harness installed and running on localhost:3080
6. Android UI shows runtime status

## Build

```bash
export GRADLE_USER_HOME="$HOME/.gradle_local"
./gradlew assembleDebug
```

## TODO

- [ ] Download Ubuntu ARM64 rootfs on first launch
- [ ] Integrate PRoot binary
- [ ] Implement Agent UI
- [ ] Add Terminal tab
- [ ] File manager
- [ ] Settings page
