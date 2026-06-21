# DiyyMusic v0.7.2 Patch

This patch fixes the Codemagic Kotlin compilation error:

```text
No parameter with name 'onOpenRadio' found.
```

The invalid argument was removed only from the `LibraryScreen` call. The valid Radio callback on `ListenNowScreen` remains intact. Extract the patch into the project root and overwrite existing files.
