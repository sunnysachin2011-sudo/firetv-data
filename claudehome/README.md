# Claude Home — Fire TV Stick launcher app

Android TV app for the Fire TV Stick 4K Max (Fire OS 8). No Gradle, no libraries: aapt2 + javac + d8 + apksigner.

Features (v0.5): Apple TV-style home — tabs (For you, Sport, Apps), "Tonight" poster row from ../posters.json, sport row (football + cricket) from ../football-today.json, glass cards on a warm dark gradient, dock of favourite + recent apps, usage-sorted app grid, Claude Lounge tile, LIVE badge, kick-off banner, auto-start after boot.

Build on Windows (JDK 17 in C:\Android\jdk, SDK in C:\Android\sdk): `powershell -File build.ps1` → `ClaudeHome.apk`.
Install: `adb connect <stick-ip>` then `adb install -r ClaudeHome.apk`.
One-time after first install: `adb shell appops set ai.claude.home SYSTEM_ALERT_WINDOW allow` (needed for the boot start on Fire OS).
