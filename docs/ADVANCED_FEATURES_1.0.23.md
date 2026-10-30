# AntiReels AI 1.0.23 — implementation and verification

30 October 2026. Version code 24. Existing application ID `com.rafi.antireelai`, minimum/target SDKs, signing configuration and bottom navigation are preserved. The working tree also retains the preceding Today/Activity UI refinement. The pre-existing `.vercelignore` change is untouched.

## Implemented

- Removed the two intersecting under-eye overlay ellipsoids, rather than removing the hands or arms. The head's existing cheek shaping, facial blush, eyes, expressions, ears, flower, materials and cached animation renderer remain intact. Happy and angry renders were visually reviewed; the existing pose set was exercised on the phone.
- Added an independent `facebook_lite` platform for `com.facebook.lite`. Facebook now maps only to its main package. New Lite activity has its own counts/time and appears in all existing analytics. Older Facebook records may include historical Lite usage; they are preserved and cannot accurately be split retrospectively. Installed icons come from PackageManager; the actual installed Facebook Lite icon was also exported as the offline fallback.
- Added Platforms → Manage → Add custom platform, a searchable installed video-sharing-app picker, confirmation, duplicate protection, persistent records and removal. The picker queries launchable handlers of `ACTION_SEND` / `video/*`, plus the known YouTube package; it does not enumerate every installed app. Some apps without that interoperability entry point will not appear. Unselected query results are not saved.
- Unknown custom packages are explicitly unsupported. They can be kept in the platform list, but have no detector, cannot be enabled through the selection controls, receive no service package subscription and generate no counts or durations. A new supported package needs a tested detector adapter; adding an app is not a claim of compatibility.
- YouTube is absent from new built-in/onboarding choices. Its stable historical ID/records and dormant saved selection are retained. The custom picker requires the specified YouTube limitation dialog and affirmative Add anyway action before activating the existing experimental detector. Cancel does not add or enable it.
- Removed custom entries retain inactive metadata for historical labels. The usual activity deletion removes activity, not platform/profile preferences; clearing all app storage removes the remaining preferences. No history migration invents attribution or video time.
- Improved both goal editors with a higher-contrast outline, pink focus state, numeric keyboard, custom-limit hint, preset selection and synchronized Save validation. Positive whole-number goals through `Int.MAX_VALUE` are accepted, including Bengali digits. Invalid, nonnumeric, zero, negative and overflowing input is rejected; count-total accumulation is protected against integer overflow.
- Updated the header, Android app label and About branding to AntiReels AI. The longer header uses its existing weighted space and ellipsis to avoid overlapping controls.
- Kept adaptive launcher XML with a fully opaque plum background, corrected Miso foreground texture, vector monochrome layer and opaque density-specific legacy fallbacks. Native device checks exercised circular and rounded masks and the themed layer. The foreground texture is a layer within an adaptive icon, not a standalone transparent launcher implementation.
- Updated the bundled and hosted-source privacy policy with YouTube/custom tracking limitations, scoped app discovery, local metadata retention and actual processing practices. Security/support sources were updated consistently. Hosted deployment was not performed or verified.

## Detection and performance engineering

`PackageDetectors` dispatches by the catalog's exact package/detector mapping. Facebook Lite reuses the existing Facebook recognition and established repeat/session gate, with additional full-player/selected-Reels evidence and comment-sheet rejection. Ordinary embedded previews, ads, repeated callbacks and unknown packages have synthetic regression coverage. Existing Instagram, TikTok, Facebook and other recognition/counting algorithms are unchanged; the existing YouTube algorithm is retained only behind experimental opt-in.

The service caches selected package-to-detector mappings instead of repeatedly searching the catalog and copying selections during events. Each overlay update computes one allowance snapshot. Disabled/paused/unconsented/unsupported-only tracking no longer schedules a four-second idle heartbeat; preference changes and break completion restart the existing active heartbeat without duplicate posts. Active-player scan intervals, event coalescing, bounded worker snapshots, cancellation gates, foreground/lock checks and repeat detection remain in use.

Activity snapshots carry platform IDs, so catalog additions cannot misattribute a briefly stale chart array. Custom/archived platforms remain available to analytics, with deterministic theme-aware colors and no fake records.

A baseline Pixel 6 Pro / API 37 scroll workload recorded 393 rendered frames, 2 janky frames (0.51%), 5 ms median and 10 ms 95th percentile. Raw baseline graphics/memory measurements are in `performance-baseline-1.0.23.json`. The after-change benchmark was attempted after reconnection but stopped because another application became foreground. No valid after-change measurement or measured performance improvement, CPU reduction or low-end-device result is claimed.

## Verification status

- Debug app, instrumentation APK, unit tests and debug lint build successfully using existing cached dependencies.
- 173 Kotlin unit tests pass, including independent Lite/history identities, dormant YouTube handling, custom-record encoding, input validation, conservative Lite recognition, a synthetic 30-transition burst/repeat sequence and heartbeat conditions.
- 17 Python tooling tests pass, including generated legal-source consistency.
- Final physical run: all 17 checks passed, including the corrected searchable installed-app picker, YouTube warning/confirmation, persistent add/remove, unsupported filtering, goal input, Miso poses, launcher masks, analytics, accessibility snapshot safeguards and legal readers.
- The picker test now waits for the clickable candidate rather than confusing it with the editable search text.
- The legal reader's former fixed 29 October test date was updated to verify each bundled document's actual updated date and the new privacy section.
- Lint has zero errors. Warnings remain; consult the current lint report for exact totals and locations.
- A same-certificate development update was installed on the Pixel without an uninstall, data reset, permission grant, signing change, goal change or consumption of the restart allowance. The existing manually signed `dist/app-release.apk` was not replaced.

## Detector confidence

| Platform | Status |
| --- | --- |
| Instagram, TikTok, Facebook and other existing built-ins | Existing detector logic preserved; regression tests pass. Not a new live compatibility certification. |
| Facebook Lite | Separate built-in adapter with synthetic recognition/dedup tests. Continuous real-device counting remains unverified. Lite was installed but not selected; no real selection or allowance was changed for testing. A screen probe did not confirm Lite as the foreground package, so it is not treated as valid Lite evidence. |
| YouTube | Experimental, explicit custom opt-in only; known accessibility limitations remain. |
| Unknown custom applications | Unsupported and not monitored. No fabricated activity. |

## Remaining device/release work

The corrected picker check is complete. Repeat the recorded scroll/memory workload while AntiReels remains foreground to obtain a valid after-change measurement. Continuous Facebook Lite validation requires the owner's selected Lite feed with sufficient daily allowance; confirm new/rapid/slow transitions, ads, backward navigation and repeats against actual accessibility signals. Do not increase goals, consume the restart allowance, reset history or bypass Android permission controls to make the test pass.

Build a production APK/AAB with the owner's existing production key after review. The build verified here is a development build; it does not certify Play Protect, all Android devices or Google Play approval. Review the updated installed-app-discovery disclosure and Play Console declaration as part of publication. No DNS, hosting, GitHub publication or unrelated external resources were changed in this task.

## Principal modified files

- Platform catalog/persistence: `PlatformCatalog.kt` (new), `TrackerStore.kt`, `PreferenceUiUpdates.kt`.
- Platform UI: `CustomPlatformPicker.kt` (new), `SettingsPlatforms.kt`, `PlatformLayouts.kt`, `MainActivity.kt`, `MisoPage.kt`, `SettingsPages.kt`, `OnboardingModel.kt`, `OnboardingFlow.kt`, `UiCopyDetails.kt`.
- Tracking: `tracking/PackageDetectors.kt` (new), `tracking/ReelAccessibilityService.kt`.
- Miso, goal and analytics integration: `CompanionBodyCache.kt`, `PreferenceComponents.kt`, `SettingsGoalDialog.kt`, `ActivityCharts.kt`, `TodayComponents.kt`, `SettingsSections.kt`.
- Android configuration/resources: `app/build.gradle.kts`, `AndroidManifest.xml`, `values/strings.xml`, both adaptive launcher XML files, `ic_launcher_background.xml`, Miso launcher foreground and density-specific launcher PNGs.
- Legal: `legal-site/content/{privacy,security,support}.json`, matching bundled Android assets, `legal-site/build.py`.
- Tests: `PlatformCatalogTest.kt`, `FacebookLiteDetectionTest.kt`, `AdvancedFeaturesTest.kt` (new); `PlatformFilterTest.kt` and `LegalReaderTest.kt` updated.

## Official policy references reviewed

- [Android scoped package visibility](https://developer.android.com/training/package-visibility/declaring)
- [Google Play AccessibilityService API](https://support.google.com/googleplay/android-developer/answer/10964491)
- [Google Play sensitive permissions and package visibility](https://support.google.com/googleplay/android-developer/answer/16558241)

These sources require scope-appropriate visibility and accurate disclosure/consent. They are not approval of this app or a guarantee of store acceptance.
