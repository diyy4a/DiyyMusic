# Discord setup for DiyyMusic

## Standard account linking

DiyyMusic 0.9.2 uses the standard Discord OAuth `identify` scope by default. Register this exact callback in **Discord Developer Portal → OAuth2 → Redirects**:

```text
http://127.0.0.1:6463/callback
```

1. Open the Discord Developer Portal.
2. Select application ID `1518124516893528125`.
3. Open **OAuth2**.
4. Enable **Public Client** so the Android app can exchange its PKCE authorization code without embedding a client secret.
5. Add the exact redirect URI above and save it.
6. Rebuild/install DiyyMusic.
7. Open **Profile → Discord Rich Presence → Connect Discord**.

Do not add a trailing slash, and do not replace `127.0.0.1` with `localhost` unless the APK is built with that exact value.

To override it in `local.properties`:

```properties
DISCORD_REDIRECT_URI=http://127.0.0.1:6463/callback
```

## Mobile Rich Presence

Account linking and Rich Presence are separate. Standard OAuth can identify the user, but publishing Rich Presence from an Android app requires Discord Social SDK access for the Discord application.

Only after Discord has approved/enabled Social SDK access for the application, build with:

```properties
DISCORD_SOCIAL_SDK_ENABLED=true
```

Without that flag, DiyyMusic intentionally requests only `identify`. This prevents the `invalid_scope` page caused by requesting restricted Social SDK permissions on an application that does not have access.
