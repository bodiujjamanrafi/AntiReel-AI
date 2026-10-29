# AntiReel AI 1.0.5 — Android installation and accessibility audit

**Date:** 27 October 2026. **Package:** `com.rafi.antireelai`. **Version code:** 6.

**Device status: NOT VERIFIED.** No physical phone is currently connected. The safe installation preflight reports `no_connected_device`; it did not install, launch, uninstall, clear data or grant a permission. An isolated emulator launch exited before boot (exit 132), so it supplied no installation or runtime evidence. The app cannot honestly be described as installed and working perfectly on the user's phone from this audit.

## Result

The current source, release build, signing configuration, manifest, compiled accessibility metadata, runtime service lifecycle, backup settings and resolved runtime dependency inventory were audited. Concrete service-lifecycle and backup defects were repaired. A signed release APK and signed AAB were rebuilt with the existing release key. Android/Google permission and installation-verifier decisions remain outside the app's authority.

The supplied screenshots show **“App was denied access” in Android Accessibility**. They establish a restricted-permission denial after installation. The separate reported Play Protect installation block has not been independently reproduced with an exact package-manager verification error or a current installation screenshot. These two outcomes must not be conflated.

## Phase 1 — installation, signing and compatibility

| Check | Finding / action |
|---|---|
| Manifest identity | Package is unchanged. Launcher activity is exported intentionally; internal limit activity and break receiver are not exported. |
| Accessibility binding | Exported service is guarded by `android.permission.BIND_ACCESSIBILITY_SERVICE`; unrelated apps cannot bind to it. Metadata points to the existing `reel_accessibility.xml`. |
| Requested permissions | Only vibration, notifications and boot completion, plus AndroidX's signature-protected internal receiver permission. No Internet, SMS, contacts, generic overlay, package-install, broad package-query or secure-settings-write permission. |
| Exported library components | AndroidX ProfileInstallReceiver is protected by the platform DUMP permission; AndroidX startup provider is not exported. |
| Release configuration | Release is non-debug and non-test-only. Dedicated local signing credentials are excluded from source control and from both artifacts. Missing release credentials fail the build. |
| Signing continuity | Existing production release certificate retained; no new package identity, certificate evasion or trust-source disguise. APK verifies with its modern signature; AAB signature and matching signer verify separately. |
| Historical debug conflict | The previously inspected Pixel installation was Android Debug-signed. The production release uses a different certificate. Android cannot ordinarily update that installation with this unrelated signer. An uninstall would erase private history/settings because backup is disabled; none was performed. This remains a migration decision, not a reason to erase data automatically. |
| Test-only APK issue | A prior installed Android Studio Run build carried TEST_ONLY. The normal development update removed it during the earlier connected session. Production packaging rejects any test-only release. This is separate from Android's restricted-settings decision. |
| Downgrade / OS / CPU checks | New safe installer preflight verifies candidate package/signature/version, device SDK and native ABI; it stops on signing conflict, downgrade, unsupported OS/CPU, ambiguous device selection or package-manager rejection. It never uses uninstall, forced downgrade or automatic permission grants. |
| APK integrity | APK signature, ZIP CRC checks, resource/manifest decoding and SHA-256 recording pass. |
| Compatibility | Minimum API 30 (Android 11), target/compile API 37. Four native ABIs are included: ARMv7, ARM64, x86 and x86-64. ZIP and ELF64 LOAD alignment pass the 16 KB checks. These are packaging checks, not certification of all native runtime/RELRO behavior or every device. Android 10 and earlier are unsupported. |
| Data preservation | No user data or settings were deleted. An explicit same-signer `adb install -r` is the only automated install route in the preflight tool; it refuses unrelated-signature replacement. |
| Backup hardening | Confirmed empty template backup-rule files were replaced with explicit exclusions for all supported app-data domains in legacy backup, cloud backup and device transfer. This strengthens the existing `allowBackup=false` setting without deleting local data. |

### Potential Play Protect concerns

The capability combination—accessibility screen inspection, floating overlays, media-pause requests and deterministic Home navigation at a selected daily limit—is sensitive even when a user intends it. It must be prominently disclosed and accurately declared for Google review. These are **possible review/detection factors**, not a proven explanation for Google's classification of the blocked APK.

No malicious code pattern was identified in the reviewed application source searches for networking, external code loading, shell execution, hidden installation, permission mutation or component hiding. This negative source review is not a malware-free certification. No APK was uploaded to a third-party antivirus service, no Play Protect scan/approval was obtained, and no Google appeal/publication was submitted.

## Phase 2 — accessibility service

| Finding | Repair / evidence |
|---|---|
| Repeated connection callback | The previous connection path could enqueue another recurring inspection loop and replace its speech engine without closing the old engine. Reconnection now removes the scheduled inspection callbacks, cancels delayed warnings, resets observations and closes the previous speech engine before restarting. This is a confirmed code path; the audit does not prove it caused the earlier phone denial. |
| Unbind readiness gap | No explicit unbind cleanup existed. Unbinding now immediately clears process-local readiness, invalidates scan tickets, removes recurring/event/break/warning callbacks, flushes recorded duration, hides overlays and closes speech resources. |
| Stale scan after interruption/configuration change | New thread-safe ServiceScanGate tickets reject results from an older connection or configuration. Interruption, unbinding, reconnection, consent/platform/break changes and history resets invalidate old work. Already queued completion callbacks are allowed to finish so the worker's busy state cannot become permanently stuck. |
| Processing after cancellation | Bounded node traversal now checks cancellation and thread interruption. Cancelled scans return no further evidence; raw screen labels remain ephemeral and are not persisted. Existing password/editable/hidden-node exclusions remain. |
| Stale result after screen lock or break | The result callback rechecks running state, consent, selection, break time, screen interactivity and keyguard before counting or showing the overlay. |
| Delayed limit warning after a changed decision | Delayed limit-screen work is now explicitly tracked/cancellable and ticket-checked. A newly started break, deselected platform, withdrawn consent, interruption or unbind cancels it. |
| Foreground action safety | Before media pause/Home enforcement, the current foreground package is checked against the selected platform. An old frame cannot send the user Home after they have already left that platform. |
| Native window metadata lifetime | Fallback AccessibilityWindowInfo objects are recycled after obtaining the selected root. Root/child node recycling remains in place. |
| Scope and capabilities | Android event delivery is restricted to supported packages, narrowed at runtime to selected ones; empty selection uses the app's own package rather than Android's all-packages empty-list behavior. Gesture, key filtering and touch-exploration capabilities are not requested. Text-change/selection accessibility events are not requested. |
| Activation reporting | Active still requires Android access, the actual service callback, consent, a start request, selected platforms and no break. Bound state is process-local, not restored from preferences. Exact component checks and public state callbacks remain. |
| User disclosure | The complete purpose, data handling, deterministic limit behavior, decline option and revocation path appear before optional setup media. Android approval remains a separate user action. `isAccessibilityTool=false` is retained accurately. |

The application uses TYPE_ACCESSIBILITY_OVERLAY after its service is connected. Adding SYSTEM_ALERT_WINDOW would not enable a denied accessibility service and is not a remedy for this issue.

## Validation

- **83 Kotlin/JUnit tests passed**, including four new tests for unconnected, invalidated, disconnected and reconnected scan-ticket behavior.
- **Eight Python audit/safety tests passed**, covering malformed native libraries, 4 KB versus 16 KB LOAD alignment, complete data-domain exclusions separation of cloud/device-transfer rules, signer-conflict refusal, downgrade refusal, no-device reporting and a simulated compatible update without permission grants. Installer tests use mocks and do not establish a physical installation.
- **22 packaged APK checks passed**, plus bundle signature/structure/version and signing-continuity checks.
- **Lint: zero errors, 63 warnings.** Remaining warnings are recorded in the generated lint report; they are not represented as resolved or as antivirus results.
- The Android instrumentation APK compiles, including a new cancelled-snapshot privacy test. It was not executed for 1.0.5 because no working device/emulator was connected. Older-version device tests are historical, not evidence for this release.
- **108 public resolved Maven coordinates** were queried against OSV; the completed lookup returned **zero matching known vulnerability records**. No APK or user data was sent. Database coverage is incomplete; this is neither an independent native-code audit nor a malware scan.
- Physical install, Android permission grant, live speech, every social-player implementation and real cross-platform end-to-end counting remain **unverified** in this audit.

## Artifacts

- `AntiReel-AI-1.0.5.apk` — 3,932,529 bytes; SHA-256 `575edbdbcdbc7c6f9bd9a706b93b3fe79b95f759bbd4b6a33fedb4748c67b097`.
- `AntiReel-AI-1.0.5.aab` — 4,282,768 bytes; SHA-256 `4f2d34fb42e94f5d76ea20bf83e424983e4471d20a37ea93d748d5fb93dc978d`.

Release certificate SHA-256: `0f507ded36b9d99db671ac730ad1199d9f0c782cd8f06b2a06b03d9b9f246d7c`.

The APK is for direct installation. The AAB is for Play Console, not the phone's package installer. Detailed evidence is in `docs/release-apk-audit.json`, `docs/release-dependencies.json`, `docs/dependency-vulnerability-audit.json` and the versioned checksum/verification files in `dist`.

## Reproduce and complete device verification

```sh
# Normal connected environment / configured signing credentials:
./gradlew :app:prepareDistribution :app:assembleDebugAndroidTest
python3 -m unittest discover -s tools/tests -v

# Read-only install preflight. Select --serial if several devices are connected.
python3 tools/install_checked_apk.py

# Explicitly install only if the preflight proves a compatible, data-preserving path:
python3 tools/install_checked_apk.py --install --launch

# Read-only Android access status; this does not grant access:
python3 tools/check_device_activation.py

# Refresh public dependency metadata and the limited OSV lookup:
./gradlew :app:exportAuditDependencies -I tools/gradle/dependency-inventory.init.gradle --no-configuration-cache
python3 tools/check_dependency_vulnerabilities.py
```

The installation tool distinguishes package-manager failure from an unrelated signing certificate. It does not treat launching the Activity or a remembered Start preference as proof of live reel tracking.

After a device is connected and any signature migration is safely decided, verify installation, launch, in-app consent, Android service approval, the actual service connection, selected platforms, forward/reverse/ad counting, pause/break behavior, exact-limit intervention and daily reset on the real release. If Android blocks restricted access for a trusted build, its supported per-app approval occurs in Android App info, followed by the service switch in Accessibility. An absent/disabled approval menu needs a current App info screenshot and device-policy diagnosis. No hidden API or global protection toggle is an acceptable substitute.

If Google has incorrectly classified the APK, the supported route is its app-verification appeal or the ordinary Google Play review workflow with an accurate accessibility declaration and a real demonstration from this release. Source changes, proper signing and local test results cannot guarantee that Google will remove a classification or approve all future versions.

## Official references

- Android signing and update identity: https://developer.android.com/studio/publish/app-signing
- Accessibility service lifecycle: https://developer.android.com/reference/android/accessibilityservice/AccessibilityService
- Restricted settings: https://support.google.com/android/answer/12623953?hl=en
- Accessibility disclosure/declaration: https://support.google.com/googleplay/android-developer/answer/10964491?hl=en
- Google app-verification appeals: https://support.google.com/googleplay/android-developer/answer/2992033?hl=en
- OSV batch lookup: https://google.github.io/osv.dev/post-v1-querybatch/
