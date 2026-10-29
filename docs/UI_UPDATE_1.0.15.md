# AntiReel AI 1.0.15 — quiet onboarding and native-player counting

## Changes

- Removed the extra spoken reaction/suggestion after every questionnaire answer, including its speech-completion wait. Continue saves and advances directly. Optional narration of each question remains available. The transient footer label is Saving rather than Miso is responding. Daily-limit warnings are unaffected.
- Fixed Android's unset accessibility-scroll sentinel: AccessibilityRecord initializes unset scroll deltas to -1. The old horizontal filter could reject an event with dx=-1 and dy=0, and an unset vertical delta could be interpreted as backward movement. Both axes are now normalized before determining movement. Explicit negative movement is still respected.
- Added a bounded metadata fallback for recognized native player scroll notifications whose indices and distances are missing. Observed on the connected YouTube version: full-size reel_recycler events with fromIndex=-1, toIndex=-1, currentItemIndex=-1, scrollY=0 and scrollDeltaY=0. A fresh event plus a settled, previously unseen identity is required; an event alone never increments a count.
- Removed the full-height drag requirement when native forward motion is independently paired with a new identity. A short gesture can fling a whole video page. Indexed and accumulated-distance evidence remain available when metadata is hidden.
- Kept the checks for ads, known backward movement, duplicate identities, stale event evidence, unrelated screens and non-player scrolls. Seek bars, time bars, sliders and comment containers do not authorize a reel transition.
- Playback progress is excluded from snapshots. YouTube's lower-left caption/creator rows are preferred over changing player and engagement controls when resource IDs are absent. The engagement rail, bottom navigation and accessibility playback controls are excluded from identity selection. Caption selection uses normalized viewport geometry rather than device-specific pixel coordinates.
- All temporary diagnostic logging has been removed. Tracking still processes bounded snapshots asynchronously, keeps screen labels in memory only, and scopes events to selected packages. Existing navigation animations and reduced-motion support remain intact.

Android reference for unset deltas: https://android.googlesource.com/platform/frameworks/base/+/refs/heads/main/core/java/android/view/accessibility/AccessibilityRecord.java

## Validation

- 122 Kotlin/JUnit tests pass. Regressions cover unset axes, zero-metric native scrolls, short forward flings, stale events, prior videos, ads and changing control labels. Shared platform fixtures cover all ten supported adapters; these are not live compatibility certifications.
- Ten onboarding/preference device checks pass across the session, including voice-enabled advancement without the reaction wait. One interrupted editor check passed on rerun. Five native accessibility snapshot/privacy tests pass on the final identity implementation, including caption-region and playback-progress exclusion.
- Eight Python installer/packaging safety tests pass.
- Release verification, signer identity and artifact hashes are recorded in dist/AntiReel-AI-1.0.15-verification.json. No Android permission or device-protection bypass was used.
- The matching development-signed APK was installed with the checked updater. Its signer matched the existing installation. The updater issued no uninstall, data-clear, downgrade or permission-grant commands.

## Live findings and remaining verification

Earlier live attempts were not successful: zero-metric events, short movement, volatile identity and locked-device/foreground checks exposed additional problems. The latest native sequence reached the configured daily limit and reported limit_reached; counting stopped and YouTube left the foreground. This is not a complete successful forward/back/revisit test of the final fixes.

The user explicitly chose to KEEP the 20-reel goal when offered a higher goal for further testing. No goal increase, bonus restart, history reset or additional live reel test was performed after that decision. Preserve that decision. Full live precision checks on YouTube and the other selected platform versions remain pending; do not describe this build as verified for every reel or every platform.

When scroll direction and a stable identity are both unavailable, accessibility cannot reliably identify a new video. The metadata fallback rejects previously seen identities, but cannot guarantee rejection of backward navigation to an unseen video without directional evidence. No zero-lag, mistake-free counting or Play Protect certification is claimed.

## Build selection

- AntiReel-AI-1.0.15-development.apk matches the development signer on the connected phone. Its integrity/signature details are supplied separately. This is a development build, not the production release.
- AntiReel-AI-1.0.15.apk and .aab retain the production signer. They are not same-certificate replacements for the development installation. Do not uninstall to force a certificate switch.
- The archive excludes keystores, signing properties, user preferences, raw accessibility labels, phone UI dumps and build caches.
