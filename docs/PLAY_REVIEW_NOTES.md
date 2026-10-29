# AntiReel AI — production release and Google review notes

Version 1.0.4 (5), 27 October 2026. This is a review hand-off, not a claim of Google approval or malware-free certification.

## What the release does

AntiReel is a personal digital-wellbeing app. A user selects supported social platforms, consents to accessibility access and sets a daily reel allowance. It estimates foreground short-video page changes and time, displays Miso, and follows a deterministic boundary: at the chosen limit, request media pause, navigate Home and display the app's warning. The user can pause AntiReel or revoke its service. Android Home/Back and app uninstallation remain available.

It is not a disability accessibility tool and retains `isAccessibilityTool=false`. It cannot grant its own accessibility permission. Its prominent in-app notice appears before optional setup media and describes both observation and interruption behavior; “Not now” stays available.

The APK has no Internet permission, ad/analytics SDK, SMS/contact permission, generic overlay permission, installer permission or broad installed-package query. Accessibility delivery is scoped to selected supported packages. Password/editable nodes are excluded. Raw screen labels are transient; only local counts, measured durations, connection status and settings persist. There is no account or server. Audit/diagnostic metadata is not transmitted by the app.

## Artifacts and checks

- `dist/AntiReel-AI-1.0.4.apk`: non-debug, non-test direct-install release.
- `dist/AntiReel-AI-1.0.4.aab`: signed Play Console upload bundle, not a directly installable APK.
- Adjacent checksum and verification files identify the exact artifacts.
- The APK and bundle use the same existing release key. Credentials are not included.
- `:app:prepareDistribution` builds release APK/AAB, runs unit tests/lint, audits the packaged APK, validates bundle signature/structure/version, and checks signing continuity before copying outputs.
- 79 Kotlin unit tests and two release-audit Python tests pass. Seventeen APK configuration/security checks pass. Lint has zero errors / 63 warnings.
- The APK includes ARMv7, ARM64, x86 and x86-64 native libraries. ZIP alignment and ELF64 LOAD alignment meet the checked 16 KB criteria. This is not a full native runtime/RELRO review or a hardware validation matrix.
- Minimum OS remains Android 11/API 30. Earlier Android, Wear OS/TV, OEM restrictions, managed devices and every hardware variant are not certified. This version has not been device-tested because no phone was connected. Prior-version device tests do not prove 1.0.4 runtime behavior.

## Google submission work still required

Use the signed AAB in your own Play Console testing/review workflow. Complete an accurate accessibility declaration and store/data-safety/privacy information for the actual build. Explain the explicit user-defined limit and the exact Home/media behavior; do not claim the app is an accessibility tool for disabilities.

Provide a real recording of this exact release showing disclosure, consent and decline paths, Android approval and the main feature. The bundled illustrated setup animation is user guidance, not evidence of a successful Google review or a real device permission grant. A successful recording cannot be claimed while Android blocks the service.

For an incorrectly classified harmful-app warning, Google's official app-verification appeal is the supported route. The classification decision remains Google's. No appeal, upload to Google or third-party malware service, public publication, permission grant, installer spoof or disabling of Play Protect was performed here. Do not change package names, identities or signing keys to evade an existing classification.

Sources:

- https://support.google.com/googleplay/android-developer/answer/10964491?hl=en
- https://support.google.com/googleplay/android-developer/answer/2992033?hl=en
- https://developer.android.com/guide/practices/page-sizes

## Existing debug installation

The dedicated release key differs from the Android Studio debug key. An unrelated certificate cannot replace the installed debug app in place. Do not uninstall the existing app without a separate data-preservation decision; this work has not removed app data or changed device security.
