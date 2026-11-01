# Claude Home — Fire TV Stick launcher app

Android TV app for the Fire TV Stick 4K Max (Fire OS 8). No Gradle, no libraries: aapt2 + javac + d8 + apksigner.

Features (v0.5): Apple TV-style home — tabs (For you, Sport, Apps), "Tonight" poster row from ../posters.json, sport row (football + cricket) from ../football-today.json, glass cards on a warm dark gradient, dock of favourite + recent apps, usage-sorted app grid, Claude Lounge tile, LIVE badge, kick-off banner, auto-start after boot.

Picture setup (v1.4): Settings → Picture setup → **Start setup** (7-step wizard: picture mode, Brightness, Contrast, Sharpness, Color, gray scale, picture size) or **Test patterns** (12 full-screen patterns). Patterns are drawn in code (`Patterns.java`), 1 pattern pixel = 1 screen pixel. Keys: OK / ▶ next, ◀ back, ▲ hide text, Back exit. The app cannot change TV settings; the user changes them with the TV remote. Start from ADB: `adb shell am start -n ai.claude.home/.PictureActivity` (add `--es mode patterns` for the viewer).

Build on Windows (JDK 17 in C:\Android\jdk, SDK in C:\Android\sdk): `powershell -File build.ps1` → `ClaudeHome.apk`.
Install: `adb connect <stick-ip>` then `adb install -r ClaudeHome.apk`.
One-time after first install: `adb shell appops set ai.claude.home SYSTEM_ALERT_WINDOW allow` (needed for the boot start on Fire OS).
