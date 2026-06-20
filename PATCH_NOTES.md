# DiyyMusic v0.7.1 Codemagic Build Fix

Extract this ZIP into the root of an existing DiyyMusic v0.7.0 project and overwrite the matching files.

The fix makes keystore creation idempotent: CI reuses a valid `app/persistent-debug.keystore` and only generates a new one when the file is missing or invalid.
