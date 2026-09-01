# Artify Workforce

Production-ready Android workforce management app for construction and field teams — selfie-verified clock-in/out, trusted server timestamps, geofenced attendance, leave workflows, and supervisor approval, backed by an offline-first local database with cloud sync.

## Features

- **Selfie-verified attendance** — CameraX capture at shift start/end, biometrically stamped against a trusted server timestamp so clock-ins can't be backdated or spoofed.
- **Geofencing** — clock-in is blocked outside a project site's configured radius; mock/spoofed GPS is detected and rejected.
- **Supervisor approvals** — inspect selfie evidence and geofence data, then approve or reject shifts and leave requests with a mandatory audit reason.
- **Leave management** — sick, casual, annual, and transit leave types with quotas, date-range presets, and a Room-backed request/approval history.
- **Offline-first sync** — attendance and leave records are written to a local Room database first and queued for Firestore sync when the device is offline, with a visible sync/queue status and manual "Sync Now" control.
- **Push notifications** — Firebase Cloud Messaging for approval/rejection alerts.
- **Audit trail & ERP outbox** — every approval/rejection is logged, with an idempotent outbox pattern for downstream ERP integration.
- **Light & dark themes** — a "Sophisticated Dark" theme by default plus a high-daylight light theme for outdoor visibility, switchable in-app.

## Tech stack

- **UI:** Jetpack Compose + Material 3
- **Architecture:** MVVM (`ViewModel` + `StateFlow`) per role (Auth, Worker, Supervisor)
- **Local storage:** Room (DAO/entity layer under `data/`)
- **Cloud:** Firebase Firestore (offline persistence & sync), Firebase Cloud Messaging, Firebase AI
- **Location:** Google Play Services Location + a geofence/mock-location detection engine
- **Camera:** CameraX for selfie capture
- **Networking:** Retrofit, OkHttp, Moshi
- **Other:** Kotlin Coroutines/Flow, Navigation Compose, DataStore Preferences, Coil, Accompanist Permissions

## Project structure

```
app/src/main/java/com/example/
├── data/            Room database, DAOs, entities, repository
├── location/        Geofence & GPS/mock-location detection
├── model/           Shared enums (roles, attendance/leave state, etc.)
├── notifications/   Firebase Cloud Messaging service & notification manager
├── server/          Trusted NTP time service & server-authority engine
├── sync/            Firestore offline sync manager
└── ui/
    ├── components/  Shared composables (camera capture, theming, etc.)
    ├── screens/     AuthScreen, WorkerDashboardScreen, SupervisorDashboardScreen,
    │                DailyAttendanceLogsScreen, RequestLeaveScreen
    ├── theme/       Color, typography, and light/dark theme definitions
    └── viewmodel/   AuthViewModel, WorkerViewModel, SupervisorViewModel
```

## Getting started

**Requirements:** Android Studio (latest stable), JDK 11, Android SDK 36.

1. Open the project root in Android Studio and let it sync (Android Studio generates the Gradle wrapper automatically if it isn't present).
2. Copy `.env.example` to `.env` and fill in any keys you need (e.g. `GEMINI_API_KEY`) — secrets are injected at build time via the Secrets Gradle Plugin and are never packaged unless explicitly uncommented.
3. To use Firebase (Firestore sync, Cloud Messaging), add your own `google-services.json` to `app/`. Without it, the build still succeeds (`missingGoogleServicesStrategy = WARN`).
4. Run the `app` configuration on an emulator or device (`minSdk 24`, `targetSdk 36`).

Demo credentials for Worker, Staff, Supervisor, and Admin roles are available as one-tap quick-login cards on the sign-in screen.

## Permissions

`INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`, `CAMERA`, `POST_NOTIFICATIONS`.
