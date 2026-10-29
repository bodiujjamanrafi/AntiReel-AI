# AntiReel AI 1.0.10 — concise setup and speech-driven Miso

## Setup video

The guide now uses the three references re-uploaded on October 28: image(20261028-045201).png, image(20261028-045210).png and image(20261028-045219).png. The source resources retain their original image bytes; their hashes are in setup-video-1.0.10.json.

The offline narrated video is 13.5 seconds instead of approximately 39.9 seconds. It shows each screen once, with one finger approach/tap per screen. All three fit a consistent 720 × 1280 portrait canvas without stretching or cropping permission text. It has 24 frames per second and is approximately 330 KB. There is no automatic looping; Replay is explicit.

Removed the added animated mascot/speech pulse inside the movie and concealed the floating shortcut mascot in the third screen’s video composition. Native Android app icons identifying AntiReel remain part of the reference UI. The live Miso at the top of the page follows narration playback, pause and completion. The original reference images themselves are unchanged.

Playback position and intent are retained while opening information pages and switching full screen, avoiding an automatic restart of the guide after reading terms. The player releases on navigation and pauses in the background.

## Agreement and information pages

Small transparent Terms and Services icon actions sit below the player. Each opens a separate, scrollable in-app information page. Android Back and the visible Back to Miso action return to the guide. The concise accessibility-purpose/local-processing disclosure stays visible near the agreement controls.

Agree & open Settings is disabled until the user checks the agreement box. Acceptance version 1 is saved only when that action is actually used. Reading pages, playing the video or toggling the checkbox does not grant Android access. Already connected users retain their pause/start and manage-access controls.

## Speaking mouth

Accessibility overlays now receive speech-start/finish/stop/error updates immediately through MisoSpeaker callbacks, rather than waiting for a periodic scanner update. Compose screens continue observing the same speech state.

CompanionView treats speaking movement independently of decorative idle movement: the mouth moves while speaking even if reduced motion/idle animation is off. A speech-only view refreshes at approximately 30 fps and retains its cached body bitmap. Speaking stops and the mouth returns to its static expression when speech ends or is stopped. The warning Activity, overlay, home, preview and setup-page character share this renderer.

## Validation and limits

- 86 Kotlin tests and eight Python packaging/installer safety tests pass.
- On Pixel 6 Pro, HomeNavigationTest passed agreement disabled/enabled behavior, both information pages, Android Back, native video playback, full screen and display-settings navigation.
- MisoSpeechCallbackTest passed: an actual offline spoken warning starts/stops the native mouth without scanning or changing recorded usage.
- MisoSpeechRenderingTest passed: the mouth changes over time with idle animation off and returns to the original static face after speech stops.
- Offline short narration export passed. Video duration/geometry were checked with ffprobe, and actual on-device playback was inspected via PixelCopy of the app’s own video surface.
- Final release packaging passes all 22 existing APK checks and verifies APK/AAB signer continuity and integrity. Lint reports zero errors and 74 warnings.
- The matching debug update installed and launched on the Pixel without uninstalling or resetting data. Accessibility was off, so this task did not verify a live social-platform limit handoff. No system permissions, installer identity or device-protection settings were changed.

The packaged release certificate differs from the installed debug certificate. The production APK is not an in-place replacement for that debug installation. No universal frame-rate, player-recognition or Play Protect approval claim follows from these checks.
