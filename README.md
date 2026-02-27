# RadiantSMS Android (Compose + Sanctum Bearer Token)

## What this project does
- Logs in via `POST /api/auth/login`
- Stores Sanctum token in DataStore
- Sends `Authorization: Bearer <token>` for protected requests
- Routes to Admin or Member dashboard based on `user.role`

## Configure base URL
Edit:
`app/src/main/java/com/radiant/sms/App.kt`

Example:
- Production: https://radiant.infinityfreeapp.com/
- Emulator to local PC server: http://10.0.2.2:8000/

## Build
Open in Android Studio and run.

## Common hosting gotcha
Some free hosts strip `Authorization` headers. If you get 401/419 unexpectedly,
check server logs and confirm the Authorization header is received.
