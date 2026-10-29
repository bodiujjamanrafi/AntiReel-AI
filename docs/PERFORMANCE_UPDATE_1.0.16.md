# AntiReel AI 1.0.16 — less work during scrolling and navigation

## Implemented

- Miso uses two shared 640×640 body textures, one per resting state, instead of rebuilding body geometry and bitmaps for each view size. Startup and service connection prewarm them on a background thread. The shared rasterizer releases its mesh after both variants are ready. Facial expressions, speech mouth motion and gestures remain native drawing. Resizing reuses the textures with an aligned destination rectangle.
- The two body pixel buffers total approximately 3.13 MiB, excluding graphics overhead. This replaces the old 8 MiB size-keyed body cache and geometry retained by individual views. First access can still wait if prewarming has not finished; no claim of zero cold-start cost is made.
- Preference transactions coalesce into one visible UI refresh per display frame. Service writes do not refresh the hidden Activity; resume refreshes current state once. Status/warning bookkeeping remains excluded. Count/time/history invalidations retain their required domains.
- Home and Platforms request seven days of local history, Activity requests thirty, and Settings/Miso do not rebuild history. History parsing remains off the UI thread. The old artificial history-update delay is removed.
- Installed-platform queries run initially and on actual supported-package addition/removal, rather than on every Activity resume. A background change is applied when the app resumes. No new permissions or exported manifest components were added.
- Navigation reuses its path, gradients and stroke objects. Icon color animation is isolated inside each tab; notch and lift animation retain draw/layer-phase reads. The existing layout, spring response, labels and reduced-motion preference are retained.
- The usage donut caches label lanes, ellipsized names, percentages, paints and geometry until data, size or styling changes instead of allocating them during each draw.
- Connected users get an asynchronously decoded, sampled guide preview. The video player/decoder is created when Play, Replay or playback is requested. First-time access guidance still autoplays. Legal documents, access actions and full-screen playback remain available.
- Frequent recognition scans reuse classifier term collections and normalize IDs once per snapshot. Label/content collection avoids the previous per-node temporary two-element lists. Classification, replay/ad filtering, counter semantics and daily-limit rules are unchanged.

## Verification

- 127 Kotlin/JUnit tests pass. Five new tests cover batched preference domains, usage/history reset propagation, bookkeeping exclusion and screen-specific history windows. Existing counting and graph tests remain passing.
- Eight Python installer/packaging safety tests pass.
- Debug APK and instrumentation APK compile. Production APK and AAB build successfully; all 22 release APK checks and bundle/signature/integrity checks pass. Lint has zero errors and 82 warnings.
- The user explicitly selected Continue without phone when offered unlocked-device performance measurements. No device timing benchmark, native rendering execution, UI test execution or APK installation was performed for this version. The previously installed version is not claimed to be this update.

## Practical limits

These are implemented reductions in rendering, allocation and refresh work, not measured frame-rate or speedup percentages. Actual scrolling, cache warmup, resampling quality, package-broadcast refresh and video interactions should be checked on an unlocked device before claiming complete elimination of lag.

The existing Miso rendering, navigation and playback instrumentation checks compile; they were not run in this session. Do not count old device results as current-version performance validation.

The user's 20-reel daily goal, saved history, permissions and preferences were not changed. No reel-watching tests or bonus restart were used. Full live Shorts/Reels counting precision remains subject to the 1.0.15 verification limits.

## Included builds

The development APK uses the known development certificate from the previously connected phone. The production APK/AAB retain the separate production certificate. Do not uninstall or clear data to force a signer change. Signing credentials, user data, caches and phone captures are excluded from the archive.

Android reference for phase-aware rendering: https://developer.android.com/develop/ui/compose/performance/phases
