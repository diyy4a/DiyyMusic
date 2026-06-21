# Discord OAuth setup for DiyyMusic

The build uses this fixed callback by default:

```text
http://127.0.0.1:6463/callback
```

1. Open the Discord Developer Portal.
2. Select application ID `1518124516893528125`.
3. Open **OAuth2**.
4. Under **Redirects**, add the exact URI above and save changes.
5. Rebuild/install DiyyMusic, then open **Profile → Discord Rich Presence → Connect Discord**.

Do not add a trailing slash and do not change `127.0.0.1` to `localhost` unless the build uses the same changed value.

To use another redirect, add this to `local.properties` before building:

```properties
DISCORD_REDIRECT_URI=http://127.0.0.1:6463/callback
```

The APK value and Developer Portal value must match character-for-character.
