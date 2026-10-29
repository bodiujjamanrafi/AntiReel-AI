# AntiReel AI — security review and release installation

Review date: 27 October 2026. Version 1.0.5 (6), package `com.rafi.antireelai`.

## Outcome and limits

A source/configuration review, Android lint, APK signature verification, packaged manifest audit and device regression tests have been completed. No standalone antivirus engine or Google Play Protect verdict was obtained. This is **not a malware-free certification**, a complete dependency vulnerability audit, or a promise of universal installation. The supplied screenshot now confirms the message “App was denied access” with a restricted-permission warning in Android Accessibility. This is a separate access restriction after installation; it does not by itself establish a malware verdict. The exact earlier APK installer/Play Protect classification is still unknown.

Google documents that enhanced fraud protection may block Internet-sideloaded apps that request accessibility access. AntiReel needs this access for selected-platform reel detection. It cannot honestly remove the permission while retaining the existing detector, or claim to bypass Google’s safety classification. Signing correctly does not grant a trusted/approved verdict.

## Changes made

- New pink Miso bunny launcher icon, adaptive artwork with mask-safe padding, themed monochrome silhouette, five density fallbacks and a matching app-header logo.
- Accessibility XML limits events to supported social packages; runtime service configuration narrows this to the user’s selected platforms. No selection uses the app’s own package, rather than an empty array (which Android interprets as all packages).
- Existing package checks before node traversal remain in place. Password, editable and hidden nodes/subtrees are now excluded before reading labels. Processing is bounded to 650 nodes / 35 levels and 1,500 characters per field. Raw labels remain transient and are not written to history or uploaded.
- Android’s accessibility service description now accurately discloses media-pause requests, Home navigation and the daily-limit warning. The app remains `isAccessibilityTool=false`; it does not claim to be a disability support tool.
- Cleartext traffic explicitly disabled; backups remain disabled. The APK has no Internet permission, ad/analytics SDK, SMS/contact permissions, package-install permission, or arbitrary external-code loader in reviewed application sources.
- Release signing configured using a dedicated RSA-3072 private key. Release is not debuggable; no debug certificate fallback. A release build fails clearly if local release signing configuration is missing.

## Verified build

- `assembleDebug`, `assembleRelease`, `assembleDebugAndroidTest`, `testDebugUnitTest`, `lintRelease` and release dependency-tree generation completed.
- 77 unit tests passed.
- Four Pixel 6 Pro instrumentation tests passed on the prior 1.0.1 build: password/editable/hidden-node exclusions, system-guarded accessibility and absent sensitive network/contact/SMS/install permissions, Miso rendering on both theme backgrounds, and activation/pause/one-use daily extension behavior. Test settings were isolated from the user’s real data.
- Lint: 0 errors, 63 warnings. Warnings include unused legacy resources, API-level attributes and existing style/formatting suggestions. They are not all removed, and lint is not an antivirus scanner.
- The prior 1.0.1 debug build was installed without erasing data. The phone is disconnected during the 1.0.2 update, so the new build was not installed and new device tests were not executed. DeviceSettingsTest compiles a regression check of the public App info/accessibility intents; it is awaiting a connected device. An ADB installation is **not evidence that the normal package installer or Play Protect will approve the APK**.
- Dedicated signed release artifact prepared and audited, not installed over the differently signed debug app.

## Release artifact

`app/build/outputs/apk/release/app-release.apk`

- Size: 3,932,529 bytes.
- SHA-256: `575edbdbcdbc7c6f9bd9a706b93b3fe79b95f759bbd4b6a33fedb4748c67b097`
- Minimum Android version: Android 11 / API 30. Device policies, Advanced Protection and manufacturer restrictions can prevent installation or accessibility access. It does not support every Android device.

Packaged checks: all 17 pass (including non-test release, native ABI coverage, ZIP alignment and ELF64 LOAD alignment). Detailed evidence and public certificate fingerprints: `docs/release-apk-audit.json`.

## Signing and existing installations

The phone currently has the debug-signed development app. The new release uses its own certificate. Android does not accept an in-place update across those unrelated signatures. **Do not uninstall the existing app merely to test this release without first deciding how to preserve its local data**; backups are disabled and uninstalling removes history/settings. No app was uninstalled during this work. Future release updates must retain the same release key and increasing versionCode.

Private signing material is local in `.signing/antireel-release.p12` and `antireel-signing.properties`, with restrictive filesystem permissions and source-control exclusions. Do not share these files or commit them. Keep a secure private backup before publishing; losing the key can prevent future updates. They are not packaged in the APK. Reuse them for future release builds; never regenerate them to evade a warning.

Build a signed release using Android Studio’s release variant or `./gradlew :app:assembleRelease`. This workspace’s cached Gradle workaround is documented in the working environment; the ordinary command works when Gradle dependencies can resolve normally. Signing properties must contain storeFile, storePassword, keyAlias and keyPassword. Building a normal Debug APK does not produce the production release artifact.

Re-run the local packaged audit with:

```sh
python3 tools/audit_release_apk.py app/build/outputs/apk/release/app-release.apk
```

## If Android says “App was denied access”

This screenshot is from Android Accessibility and explicitly mentions a restricted permission. Google documents a per-app user approval flow on Android 13+ for trusted sideloaded apps:

1. Dismiss the message. Open **Settings → Apps → See all apps → AntiReel AI**.
2. On its **App info** page, tap **⋮ → Allow restricted settings**, if present. Follow Android’s on-screen confirmation (device credentials may be requested).
3. Return to **Settings → Accessibility → Downloaded apps / Installed services → AntiReel AI**, then enable **Use AntiReel** and confirm the permission notice if you trust the installation.
4. Return to AntiReel and select the platforms you want. Miso starts after consent and connection; an intentional in-app pause remains respected. The optional accessibility shortcut is not the main service toggle.

The new Connect Miso screen includes this troubleshooting branch and opens AntiReel’s own App info through the public Android Settings intent. Returning without an enabled service shows connection help. Settings destinations fall back to general Settings if a manufacturer omits the standard page. The app cannot determine the exact restriction reason through a public API and does not claim to detect a policy it cannot inspect.

If the option is missing or policy blocks it, do not disable Play Protect or use hidden APIs, root, changed installer identity, or automatic permission approval. Work/school device management, supervision and Advanced Protection can restrict access. There is no universal app-side fix for every device.

Official instructions: [Learn about restricted settings](https://support.google.com/android/answer/12623953?hl=en).

## If Play Protect still blocks installation

1. Record the exact warning text and APK SHA-256, because an unknown-app scan prompt, enhanced fraud-protection block and harmful-app classification require different diagnosis.
2. Keep Play Protect enabled. No security feature was disabled and no package/certificate was changed to conceal the app from detection.
3. For a potentially harmful application classification you believe is incorrect, use Google’s official App verification appeal. An appeal/review is a Google decision; none was submitted automatically, and the APK/source was not uploaded to third-party scanning services.
4. For distribution, prepare a Play Console test/release through the standard review process, including accessibility declaration, prominent disclosure/consent, privacy policy and accurate store/data-safety information. This requires your developer account and does not guarantee approval.

Official references:

- [Google Play Protect’s enhanced fraud protection](https://security.googleblog.com/2024/02/piloting-new-ways-to-protect-Android-users-from%20financial-fraud.html)
- [Google App verification and appeals](https://support.google.com/android/answer/2992033?hl=en)
- [Android app signing and key continuity](https://developer.android.com/studio/publish/app-signing)
- [Google Play accessibility API policy](https://support.google.com/googleplay/android-developer/answer/16558241)


## Latest device investigation: version 1.0.3

See docs/ACTIVATION_DIAGNOSIS.md. The phone reconnected, and a normal, non-test-only development APK was installed with the existing debug certificate, retaining app data. This supersedes the prior disconnected-device note. Android still reports restricted access ignored and the accessibility service off. App-side activation now requires a real service connection and observes late connection/state updates. 79 unit tests and five device tests pass; release packaged audit: 12/12. Permission approval and live reel tracking remain unverified while Android blocks access. No security permission was granted using tools.


## Latest production release: 1.0.4

The complete disclosure is shown before optional setup media. Signed APK and AAB share the existing release certificate; bundle signature, structure and version and APK signing continuity are verified by :app:prepareDistribution. Seventeen packaged APK checks, 79 Kotlin unit tests and two Python audit tests pass. Lint: zero errors / 63 warnings. Four native ABIs are included; the checked ZIP/ELF LOAD alignment supports 16 KB packaging requirements, but does not replace device/RELRO/runtime verification. No phone is connected for 1.0.4 testing or installation. Previous device tests are historical only. Google approval, malware-free status, future absence of Play Protect blocks and support for every Android device are not established. See docs/PLAY_REVIEW_NOTES.md for exact submission requirements.


## Full audit: 1.0.5

See docs/FULL_ANDROID_SECURITY_AUDIT.md for current findings. Reconnection, unbind, cancellation and delayed-action defects repaired; all backup domains explicitly excluded. 83 Kotlin tests, eight Python tests, 22 packaged checks pass; lint zero errors / 63 warnings. OSV lookup of 108 public Maven coordinates completed with zero matching known records; this is not malware-free certification. Physical device not connected; isolated emulator exited before boot. Installation, user-granted permission, actual service activation and end-to-end reel counting are not confirmed for this release. No uninstall, user-data reset, security bypass or permission grant was performed.
