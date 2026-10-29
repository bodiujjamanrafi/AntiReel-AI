# AntiReel AI 1.0.14 — counting recovery and responsive navigation

## Changes

- Removed the first-use platform/Miso guidance from Today and Platforms. Completing the questionnaire no longer creates a guidance flag; saving a profile removes an existing flag. The completion dialog now simply confirms that daily boundaries are ready.
- The navigation notch and selected-icon lift share a short, damped spring animation. The destination still changes on tap; navigation never waits for the animation. Reduced motion remains immediate. Animation values remain confined to drawing/graphics layers rather than per-frame layout work.
- Fixed a confirmed shared counting failure: a forward page with the previous reel's caption could be rejected as a duplicate while still consuming that page position. When the new caption finally arrived, the same page was permanently ineligible. Position is now committed only with an accepted count, leaving the candidate eligible for late metadata. The regression covers all ten supported platform adapters.
- The page adapter now accepts Android's explicit currentItemIndex when present. This supports pagers whose from/to ranges or pixel offsets are unavailable. Existing indexed/pixel fallback and backward-direction/replay guards are retained.
- The service also receives player selection events and applies the existing full-size/source checks before using their index evidence. Small tab selections, comments and unrelated scroll containers cannot directly increment the counter. No text-input events, touch interception, gesture capability or additional permissions were added.
- Removed temporary development diagnostic logging added before the interrupted session. Recognition remains bounded and asynchronous, and screen text/identifiers are not persisted by tracking.

## Automatic activation

The existing explicit Agree & open Settings action requests activation. Once Android connects the accessibility service, onServiceConnected calls startAfterConnection and immediately schedules inspection. This path does not require returning to the app first. It requires the app's disclosure consent and selected platforms, and respects a deliberate in-app Pause or timed break. Reopening or reconnecting must not silently undo a manual Pause. Android accessibility approval itself remains a user action.

## Verification

- 113 Kotlin/JUnit tests pass, including new delayed-caption/page-position and explicit-current-index regressions. Existing tests cover ads, reverse scrolling, revisits, hidden metadata and metadata/page event order.
- Eight Python packaging/installation safety tests pass.
- Debug APK and instrumentation APK compile successfully. Current device UI and live YouTube Shorts checks could not run: ADB and mDNS both report no connected phone after the workspace reconnected. Historical 1.0.13 device results are not counted as current-version tests.
- Release APK/AAB verification is recorded in dist/AntiReel-AI-1.0.14-verification.json. Signing identity, data-preserving installation safeguards and permission scope remain intact.

## Remaining device validation

The code-level counting failure is fixed and tested, but live Shorts behavior on the user's installed YouTube version remains unverified. Reconnect the existing phone, install the matching development-signed build with tools/install_checked_apk.py, and check forward Shorts, backtracking, ads, platform switching and activation after manual Android approval. Do not clear history or uninstall to change signing certificates. The release APK has a different signer from the previously connected development installation.

No mistake-free counting, zero-lag, universal platform compatibility or Play Protect certification is implied by these checks. No installation on the disconnected phone is claimed for this completed build.
