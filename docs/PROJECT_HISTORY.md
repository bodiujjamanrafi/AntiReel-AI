## Latest update: 1.0.21

Dedicated YouTube Shorts state machine, generation-safe asynchronous snapshots, identity-based deduplication and opt-in numeric diagnostics. 154 unit tests pass. The physical 30-Short check is pending because no phone is connected. [Changes, limitations and device test plan](YOUTUBE_SHORTS_1.0.21.md). UI and other platform counting rules are unchanged.

## Latest update: 1.0.20

Removed the Settings transition kick and stabilized its visible content area. Restored Miso’s original hands. [Changes and verification](UI_UPDATE_1.0.20.md).

## Latest update: 1.0.19

Grouped Settings subpages, a frameless 2×2 Miso selector with live controls, immediate preference switches, stronger dark dividers, refined storage, one Settings theme selector, and lightweight navigation motion. Existing core tracking and reminder source is unchanged. [Changes and verification](UI_UPDATE_1.0.19.md).

## Latest update: 1.0.17

Shortened onboarding to four essential prompts, outlined answer-summary cards, grouped Settings with every existing destination preserved, simplified Quick Access cards, and removed only native Miso hand geometry. [Changes and validation](UI_UPDATE_1.0.17.md). Navigation, tracking logic and saved preferences remain intact.

## Latest update: 1.0.16

Performance update: shared/prewarmed Miso body textures, frame-coalesced visible UI refreshes, screen-specific history loading, cached navigation/chart drawing and on-demand guide playback for connected users. [Changes, verification and device-testing limits](PERFORMANCE_UPDATE_1.0.16.md). No phone performance measurements or installation were performed at the user's preference.

## Latest update: 1.0.15

Removed after-answer spoken reactions and their waits. Added native Shorts/Reels fixes for Android's unset scroll values, short forward flings and unstable playback metadata. Current unit/native checks and the pending live verification are documented in [update notes](UI_UPDATE_1.0.15.md). The user's 20-reel limit is retained.

## Latest update: 1.0.14

Removed the first-use platform/access guide, added a shared spring navigation animation, and fixed a missed-counting case where stale captions consumed new page positions. Added explicit current-page index support. See [changes, validation and pending phone checks](UI_UPDATE_1.0.14.md).

## Latest update: 1.0.13

Redesigned plan completion, answer editing that returns to Settings, Home on a new Activity, a concise first-use platform/access guide, and shared Shorts/Reels fixes for recycled page indices and delayed scroll evidence. See [update and verification notes](UI_UPDATE_1.0.13.md).

## Latest update: 1.0.12

Theme selection now follows language, Continue has a stable bottom footer, question controls provide optional haptics, and Language & personal preferences has an organized frameless layout. Miso's agreement is selected by default while Android access still requires explicit action. See [update notes](UI_UPDATE_1.0.12.md).

## Latest update: 1.0.11

Completed the English/Bengali onboarding checkpoint, custom duration wheel, editable personal plan, persistence-gated finish and Miso celebration. Added shared UI language support and aligned daily time/reel allowance warnings. See [update notes](UI_UPDATE_1.0.11.md) and [onboarding status](ONBOARDING_STATUS.md).

## Latest update: 1.0.10

13.5-second setup video using the latest three supplied screens, one tap demonstration per step, separate Terms/Services pages and explicit agreement. Miso’s mouth follows speech directly, including when idle animation is disabled. See [update notes](UI_UPDATE_1.0.10.md).

## Latest update: 1.0.9

Centered setup video with pinned accessibility actions on the Miso page and full-screen player; outlined platform chooser and reordered platform summaries. See [update notes](UI_UPDATE_1.0.9.md).

## Latest update: 1.0.8

Connect Miso now autoplays a larger narrated video with finger guidance and full-screen playback. Miso is centered in navigation; platform totals and settings details are frameless. Accessibility help and App info have dedicated settings locations. Header and launcher artwork use transparent native Miso PNGs. See [update notes](UI_UPDATE_1.0.8.md).

## Latest update: 1.0.7

Miso now has a dedicated navigation destination and a step-by-step connection page using the supplied Android screenshots. Miso & display has frameless live previews; home metrics and platform open actions are frameless. The header and launcher use the exact native Miso character. See [update notes](UI_UPDATE_1.0.7.md).

# AntiReel AI — Android

Native Kotlin + Jetpack Compose app, Android 11+, in this `island` project.

## Run

Open this folder in Android Studio. Let Gradle sync finish, select a physical Android phone, then Run `app`.

1. Open **Platforms** and select Instagram, YouTube, TikTok and/or Facebook.
2. Tap **Start companion**, read the disclosure, and choose **Agree & open Settings**.
3. Enable **AntiReel AI** under Android's installed accessibility services.
4. Return to AntiReel and tap **Start companion** again.
5. Open a selected platform. Miso appears at the top center, with that platform's daily count beneath it. The overlay does not intercept touches.
6. Change daily goal and companion opacity in **Settings**. Pause from **Today**, or disable the accessibility service to revoke access.

Closing the activity does not stop the accessibility service. Force-stopping the app, revoking permission, or manufacturer background restrictions can stop it. Android may restrict accessibility for sideloaded apps; if applicable, use the app-info menu's Allow restricted settings option before enabling it.

## What is implemented

- Deep petrol, seafoam, warm ivory and copper Compose UI; editorial serif headlines, monospaced metrics, custom navigation glyphs and system inset handling.
- Custom animated, gradient-shaded mascot with breathing motion and blinking. This is a 2D procedural drawing with a dimensional appearance, not a 3D model.
- Multi-platform opt-in, explicit accessibility disclosure, persistent pause/resume.
- Accessibility overlay independent from the main activity. No separate draw-over-other-apps permission needed.
- Separate local daily totals, seven-day chart, 30-day retention, daily goal nudge and activity deletion.
- No network permission, account, backend, screen-text storage, autonomous gestures or remote AI calls.
- Background window inspection, bounded node traversal, 120 ms stability window and repeated-event deduplication.

## Counting accuracy — development status

This is a functional implementation that still needs real-device validation and adapter tuning against installed social app versions. It does **not** promise exact counts.

A view is defined as a recognized short-video player with stable author/caption metadata visible for at least 120 milliseconds. Rapidly skipped videos do not count. Normal feed swipes do not count. Returning to a video after leaving can count as another view. Videos with identical metadata can be undercounted. Changing metadata can overcount. Localized/obfuscated accessibility trees and app updates can prevent detection, especially Facebook and TikTok. When metadata or player recognition is missing, the overlay holds the last count; it does not invent a count from arbitrary scroll events. Total counts are explicitly labeled estimates in the app.

To qualify each platform, manually compare at least 20 reel transitions (including rapid skips, paused playback, comments, normal feeds, app switches, and revisits). Verify activity dismissal, lock screen, permission revocation, day rollover and goal nudges. Do not claim production precision until this is done on representative app versions and devices. App-store distribution also requires applicable accessibility declarations and review.

## Source map

- `MainActivity.kt`: dashboard, platforms, activity, preferences and permission flow.
- `CompanionView.kt`: mascot rendering shared by app and overlay.
- `TrackerStore.kt`: local preferences and daily counters.
- `tracking/ReelAccessibilityService.kt`: recognition, background inspection and overlay lifecycle.
- `tracking/ReelCounter.kt`: pure dwell/deduplication state machine.
- `ReelCounterTest.kt`: dwell, duplicate events, fast skips, platform transitions and reset tests.

## Validation

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

Use Android Studio's bundled JDK if the shell defaults to an older Java runtime.

### Checks completed in this session

Kotlin/Compose sources compiled with the cached Kotlin 2.2.10 compiler and Compose plugin against locally cached Android dependencies. All eight `ReelCounterTest` tests passed. Android resources compiled with AAPT2. Full Gradle build, lint, APK installation and visual/device validation remain unverified: this session denies the local sockets needed by Gradle and ADB. The independent compilation is not a substitute for the complete Android Studio build.

## UI and responsiveness update

- Replaced the purple mascot with a pearl-and-seafoam floating creature, warm antenna, blinking eyes and gentle tilt. This remains a gradient-shaded 2D drawing, not a true 3D asset.
- Updated launcher icon and top-left brand mark; dark launch background.
- Asymmetric companion card, redesigned navigation dock, directional fade/slide section transitions, platform selection summary, daily comparison, and expanded settings.
- System-respecting touch haptics for buttons, navigation, platform switches and slider release; optional in Settings.
- Authentic installed-app icons are loaded through Android PackageManager. Uninstalled apps use labeled symbol fallbacks; icons are not fetched remotely.
- Dashboard preference updates are event-driven rather than polling every 800 ms. Mascot shaders are cached rather than allocated each frame.
- Overlay count is now large, frameless, shadowed text with separate opacity and a short scale animation. It changes to copper at the daily goal.
- Accessibility scroll/content events trigger debounced inspection (60 ms), with a 120 ms identity stability window and polling fallback. Brief metadata gaps are tolerated to avoid duplicate counts. This counts recognized appearances, not completed watches.
- Expanded Instagram player identifiers and Facebook/TikTok text fallback. These are heuristic adapters for Instagram, YouTube, TikTok and Facebook only; compatibility with every app/version/language is not guaranteed.

### Design references consulted

- https://dribbble.com/search/digital-wellbeing-app
- https://dribbble.com/tags/digital-wellbeing
- https://in.pinterest.com/pin/wellbeing-mobile-app-ui-kit-an-ui-kit-template-by-betush-in-2025--112730796961537284/
- https://motionsites.ai/
- https://developer.android.com/develop/ui/compose/performance/phases
- https://developer.android.com/reference/android/view/accessibility/AccessibilityEvent

### Redesign validation

Independent Kotlin/Compose compilation and eight counter unit tests pass. AAPT2 resource compilation passes. The cached Gradle installation cannot start because local socket creation is denied (`FileLockContentionHandler`). No APK, screenshot, device accuracy, or frame-time result is claimed for this update.

On-device checks: compare 20 reel changes on each selected platform; test rapid swipes, two clips by the same author, comments, normal feeds, returning from another app, pause, lock/unlock, and accessibility revocation. Check large font sizes, haptics disabled, system animations disabled, and opacity on bright and dark video backgrounds.

## Performance update

- Cache the 3D companion body in a hardware-composited RenderNode. Ordinary frames reuse that body and animate small facial paths; geometry is rebuilt only after resize, reattachment or an ear-pose change.
- Use display-synchronized animation callbacks and explicitly hardware-accelerate the service overlay.
- Run accessibility snapshot parsing and recognition on the worker, coalesce scans at a 90 ms minimum interval, and reduce fallback polling while idle. Recognition/settling rules are unchanged.
- Compile recognition patterns once, avoid unnecessary fallback parsing, and prune old counters once per day instead of after every reel.
- Keep slider drafts local until release, load platform icons off the UI thread, and avoid rewriting unchanged overlay labels.

Independent Kotlin/Compose compilation and 28 logic tests pass. Device frame-time measurements and the full Gradle/APK build remain unverified in this restricted environment. These changes remove identified work from animation frames; they are not a measured FPS guarantee.

## Pinterest home and navigation update

References inspected directly:
- Navigation: https://www.pinterest.com/pin/1084241679076852450/ (https://pin.it/5qmj3af9k)
- General styling: https://www.pinterest.com/pin/993817842795391998/ (https://pin.it/2IOuVm3lW)

The app now uses pearl/lilac surfaces and burgundy accents, compact daily statistics, a companion control card, quick access and grouped platform rows. The password-manager content from the reference is not included. Existing AntiReel actions and tracking behavior are retained.

Navigation retains Today, Platforms, Activity and Settings. Its animated notch and raised active icon use draw/graphics-layer animation rather than measuring layout every frame. Shared vector icons and style tokens live in ReferenceNavigation.kt. Each destination keeps its own lazy-list scroll position; reduced motion disables navigation-indicator motion.

Validation: independent Kotlin/Compose compilation and 28 logic tests pass. Full Gradle building and device visual/frame-time validation remain blocked/unverified in this environment; no measured smoothness guarantee is made.

## Today screen refinement

- Daily statistics share the same measured row height and equal widths.
- Companion actions use a larger two-line button, with permission/platform checks preserved before resuming.
- Quick access uses larger indigo, sage and warm sand cards; the header has a status badge and no brand subtitle.
- Social icons have no added tinted tile. TikTok uses its vector brand silhouette from Simple Icons v16 rather than a generic note.
- Break controls offer 2, 5 or 10 minutes, a localized UI countdown updated inside the break card, and early ending. Taking a break preserves the companion's on/off setting; only an already-running companion resumes automatically.
- Navigation has an orchid surface and burgundy raised indicator. Selection retains accessibility semantics but removes the rectangular ripple. Page transitions use a short fade/lift with subtle outgoing scale, respecting Reduced motion.

Validation: Kotlin/Compose compilation and the existing 28 logic tests pass. Device rendering and frame-time testing remain unverified.

## Rabbit and Platforms update

Reference inspected: https://www.pinterest.com/pin/1128855462875065172/ (https://pin.it/4htZOfOgK).

The procedural 3D companion now takes cues from the reference's cream rabbit, pink inner ears, rounded cheeks, cheek-side paws and bucket hat. This is an original mesh adaptation, not the reference image pasted over the counter. The body remains cached in a hardware layer; blinking, reel-change winks and gentle motion update separately. Standby is relaxed, active expressions follow the goal, and breaks use a resting expression. Reduced motion and expression controls still apply.

Kwai, Moj and Josh bring the catalog to ten platforms. Each has package visibility entries and a recognition adapter. Platforms now provides search, All/Selected/Installed filters, select-installed/clear actions and Open buttons. Installed state refreshes when the app resumes. Selecting an app does not bypass Android consent: accessibility must still be enabled manually, after disclosure. No broad package-query or internet permission is added.

Original app icons are loaded from installed apps, with bundled Google Play listing icons as offline fallbacks. The download sources are recorded in assets/platform-icons/SOURCES.json. Brand names and logos identify their respective services; no affiliation is implied.

Validation: independent Kotlin/Compose compilation, resources and 30 logic tests pass. Adapter fixtures are synthetic. Real-app recognition accuracy, Android visual QA and device frame-time measurements remain unverified.

## Pink Miso and companion-session control

The companion action now uses a session-style control with a prominent circular play/pause affordance. The button and raised navigation indicator share a raspberry-plum color; navigation uses lighter translucent shades and a thin highlight instead of a costly background blur. Quick-access labels are centered, with restrained glass-like highlights and borders.

A dismissible first-launch speech card introduces Miso and explains its help. A local welcomeSeen preference prevents repeat onboarding after acknowledgement; it does not start tracking or grant permissions. Miso's mesh has brighter blush-pink material, fuller pear-shaped cheeks, asymmetric soft ears and a lilac-pink hat. Existing expressions, reduced motion and cached body rendering are retained.

## Session, voice and live Activity update

- A translucent square start/pause control contains a display-synchronized arc. Its phase freezes on pause; Reduced motion disables rotation. The companion card has a clearer two-part layout and Hear Miso action.
- Miso greets once using Android TextToSpeech with an installed offline English voice. Speech can be replayed from Today/Settings, disabled in Settings, and stops when the activity leaves the foreground. Mouth animation follows speech callbacks. If no offline English voice exists, Settings explains what to install; no synthesized audio file or network voice is bundled.
- Navigation is translucent, with saturated icons and preserved accessibility labels. It hides after downward user scrolling and returns on upward scrolling. Its reserved inset stays stable to prevent scroll-layout jumps. Quick-access cards use brighter colors.
- Platforms places collapsed Accessibility controls below the app list. Connect accessibility opens the disclosure; Manage accessibility opens Android Settings. Details are visible only after Show controls.
- Activity records estimated foreground time in recognized short-video players while tracking is on. It uses monotonic intervals, drops observation gaps longer than two seconds, batches writes approximately every three seconds, flushes on pause/app switch, splits midnight in the device timezone, and retains 30 local days. Breaks, locked screens and unrecognized screens end attribution. Time is not whole-app usage, proof of playback, or proof of attention. Existing historic reel counts are not converted into guessed time.
- The seven-day chart selects individual dates; streaks reflect consecutive days with recorded reels/time, not days meeting a wellbeing goal. A Time/Reels donut and platform legend show each selected day's distribution and allow highlighting an app. Per-app details show both reels and time. Deleting activity clears both counts and duration.

Validation includes Kotlin/Compose compilation, resource compilation, existing recognition/counter tests and new monotonic-timing, app-switch, pause, gap, midnight, DST and streak tests. Phone audio, real-app detection accuracy, frame rates and complete APK/device testing remain unverified.

Latest checks: independent Kotlin/Compose compilation, AAPT2 resource compilation and all 40 unit tests pass. The full Gradle assemble/test/lint attempt stops during settings resolution because the offline cache cannot resolve org.gradle.toolchains.foojay-resolver-convention:1.0.0; an APK and device results are not claimed.

## Floating navigation, Miso control and Activity comparison refinement

Activity reference inspected: https://www.pinterest.com/pin/1083326885401707066/ (https://pin.it/68GKCb57Z). Its light panels, fine chart grids, compact statistics, mint and violet series inform the Activity design. No reference data is used.

- Navigation now floats over the content instead of occupying a reserved Scaffold bottom bar. When hidden, list content extends through the former navigation region; only normal system/reading insets remain. The larger navigation and Standby badge share the Reels Today burgundy gradient.
- Tap the framed, transparent Miso square to start/pause/resume via the existing platform and accessibility checks. No microphone or voice-command permission is added. Its animation follows the real session state.
- Selected/Installed quantity cards are larger and filter the platform list when tapped. Accessibility text is larger; Connect/Review and Manage remain actionable without expanding explanations.
- Activity now receives explicit immutable day snapshots instead of reading mutable preferences inside otherwise-skippable chart components. Fresh preference notifications therefore update chart inputs. Pending usage is flushed when the app resumes, and stale in-flight screen inspections are invalidated.
- Two selectable platform series show actual recorded time over seven days; the daily bar chart still selects dates, and the donut shows time/reel distribution. A separate per-app comparison uses the selected date versus its previous date and labels today's partial-day comparison explicitly. Missing baselines and missing historic durations are not invented.

Checks: independent Kotlin/Compose compilation, AAPT2 resources and 45 logic tests pass, including new snapshot isolation/refresh and comparison-baseline tests. Actual phone rendering, frame rate, speech and social-app recognition accuracy still require device validation. The earlier full Gradle build remains blocked by its unavailable Foojay plugin dependency.

## Today page organization

Today now follows the requested order: bold attention headline with Miso on the right, equal-width/equal-height reel and allowance cards, a compact Miso session panel, selected app icons, direct quick actions, and a daily-focus summary. The existing pearl/rose/burgundy styling and floating navigation are preserved.

The reel card's background sparkline uses the last seven recorded daily reel totals; the flame and allowance clock are drawn in code. No sample trend is substituted. The selected-app strip uses cached original app icons and opens installed apps, falling back to platform management when unavailable.

Quick actions now open a 2/5/10-minute break picker (or end a current break), edit the daily goal, and toggle Miso's sound. Daily Focus shows recorded video-screen time, goal remaining and the platform with the most recorded time, with a link to today's detailed Activity. The session panel retains the Miso start/pause/resume control, voice replay and accessibility disclosure.

Reference access note: the attached image was resolved and its text read, but full image materialization was unavailable in this runtime. The layout follows the explicit written arrangement; exact screenshot matching and on-device visual/smoothness validation are not claimed.

## Compact dashboard, appearance and forward reel counting

- Today uses transparent, responsive selected-app tiles sized to fit four across the available width. Miso now has a compact horizontal session card with larger typography, contextual goal messages, voice and access actions. Session animation stops for pauses and reduced motion. Quick actions use distinct teal, violet and vermilion gradients. Break and daily-goal dialogs share a reusable rounded, theme-aware layout. Today's Focus includes actual video-screen duration, goal progress and the leading recorded platform.
- Platforms has a smaller companion summary with inline Selected/Installed counters that filter the list. Platform rows and accessibility controls use subtle dark outlines. Accessibility explanations and secondary controls expand on demand; connecting still uses the existing disclosure and Android settings screen.
- Activity's weekly line chart includes every platform with recorded duration, with individual legend toggles. Daily-history bars show recorded duration and select that day's totals and per-app distribution. The larger distribution ring shows time or reel percentages, authentic icons and a tappable per-app breakdown. Duplicate comparison bars and the separate repeated app-details list are removed. Totals come only from saved observations; duration remains foreground recognized short-video-screen time, not total device/app usage or proof of playback. The service batches live duration writes about every three seconds and flushes on leaving the app, pausing or returning to AntiReel.
- Settings includes Device / Light / Dark appearance, persisted locally, and organized display, expression/motion, pace, sound, tracking/access and data sections. All primary surfaces, content, cards and dialogs use the selected palette. The Pinterest Settings short link could not be retrieved in this environment; an exact visual match is not claimed.
- `NovelReelGate` requires forward page evidence for subsequent reels, tracks the furthest observed page in a continuous player session, and rejects recently seen metadata hashes. It retains up to 2,048 hashes in memory, scoped to the local day and platform, without storing captions. Reverse and revisited page positions do not increment. Standalone sponsored/ad disclosure labels and known ad-badge IDs exclude ads before counting and time attribution.
- Limits are daily totals across selected platforms. At 80%, Miso gives a firm reminder; beyond the goal, an angry expression and head shake accompany a soft red glow that spreads outward from the companion. The glow settles to a static translucent tint, does not intercept touches, disappears outside recognized selected players and honors reduced motion. Voice, reminder vibration and limit glow have Settings controls; speech requires an installed offline English voice. Reminder keys prevent repeated speech for the same daily goal/stage.

Counting remains dependent on each installed app's accessibility output. A hidden ad label cannot reliably be detected, and a platform that supplies neither usable direction/page evidence nor identifiable metadata cannot reliably distinguish new reels from repeats. This policy favors excluding ambiguous changes over falsely counting backtracking. Duplicate-memory state is intentionally temporary, so service restarts and evicted identities can be counted again. Captions are imperfect identities: two distinct videos with identical exposed metadata may be conservatively treated as repeats. Real-platform compatibility is not established by synthetic unit tests.

Validation: full `assembleDebug`, `testDebugUnitTest` and `lintDebug` succeeded using the installed Gradle distribution, a writable temporary Gradle home, the shared read-only dependency cache, and an init-script mapping of the cached Foojay implementation plugin. No project build configuration was changed for that workaround. All 56 Gradle unit tests pass; lint reports 0 errors and 40 warnings, mainly existing unused resources, API attribute compatibility and KTX/style suggestions. The debug APK installs successfully on the connected Android phone. Its lock screen prevented visual interaction testing; an isolated emulator could not start in this environment. No on-device frame-rate, theme screenshot, TTS playback, red-glow or real-social-platform accuracy result is claimed.


## Settings drill-down, break reminders and clearer limits

Settings now starts with compact, icon-labeled groups: Appearance, Miso & display, Daily goal, Breaks & reminders, Accessibility, Activity & storage, Understanding tracking and Privacy. Tap a row for that category's controls; Android Back returns to the overview. Accessibility controls were removed from Platforms and live only in Settings. The Home Access action opens that category. Typography uses the shared system sans family throughout Settings and platform headings.

Custom goals accept any positive Int whole number rather than rounding to preset slider values. The editor validates input before saving and offers optional quick presets. Main content now explicitly supplies the theme's foreground color; custom dialogs use a Material Surface with explicit content color, fixing inherited black text in dark mode. Dark surfaces and secondary text are brighter, chart series have a separate high-contrast dark palette, and all navigation icons now display their destination names.

Breaks show a live timer on Today and in Breaks & reminders. Android's quiet ongoing notification uses a native countdown, expires at the deadline and opens the app. An inexact idle-capable alarm delivers the break-complete notification with the original 1.4-second Miso chime in res/raw/miso_chime.wav. Its source is tools/generate_miso_chime.py (PCM synthesis using Python's standard library). Completion also runs from the accessibility service and app resume when available, and pending breaks restore after boot. Obsolete alarms cannot finish a replacement break. Android notification permission is requested when starting a break; system channel/Do Not Disturb/volume and battery scheduling policies still govern delivery and audibility. No exact-alarm permission, continuously running timer service or account is required.

Near (80%), reached (100%) and exceeded (>100%) goals have distinct spoken messages, with daily per-goal/stage deduplication. Speech is recorded as delivered after the utterance completes; interruption leaves the warning eligible to retry. Over-limit warnings can preempt near-limit speech. Spoken warnings have a separate control from the one-time welcome greeting. A subtle red glow starts near the limit, strengthens at the boundary, and becomes stronger beyond it; it remains transparent, nonblocking, limited to recognized selected players, and respects reduced motion. An offline English Android TTS voice and audible speech volume remain necessary; visible warning text and the glow also communicate the state. No pre-recorded speech or online speech service is bundled.

Activity replaces its long scope explanation with a short insight derived from the selected day's actual counts, duration, leading platform and available previous-day baseline, plus a relevant break suggestion. The scope explanation opens from Understanding tracking in Settings. Weekly charts retain all recorded platforms and allow selecting a day through the plot or weekday buttons; legends show that day's per-app duration. No usage was fabricated or inferred for missing history.

Miso is a brighter pink sculpted bunny with asymmetric ears, fuller cheeks and a small 3D flower clip. The launcher icon matches. The header Miso is larger, the session control is completely frameless, and body meshes remain cached in GPU display lists. Idle/active expressions, blinking, talking and the over-limit head shake continue to honor session and motion settings. The Pinterest short link was attempted again but remained unavailable; an exact match to that reference is not claimed.

Latest validation: full Android build, all 65 Gradle unit tests and lint pass (no lint errors; remaining warnings are reported in app/build/reports). New tests cover custom goal validation, distinct warning messages, stale/early break alarms, actual-data insights and at least 4.5:1 text contrast on primary/card backgrounds in both themes. The light Settings detail screen was visually inspected on the connected phone. Full dark-mode interaction, notification playback, spoken audio and real-social-platform behavior have not been verified end to end. Updating the APK does not clear the user's records or choices.


## Happy/angry threshold and stronger reel interruption

Miso now smiles below 80% of the daily goal and switches directly to the angry expression from 80%, including standby previews. A break keeps the resting smile. The old intermediate curious/concerned moods are removed.

With Red pause curtain enabled, the selected recognized reel screen gains a stronger red veil from 80%, increasing toward the goal. From 95%, an opaque red curtain fully covers the video and displays a break prompt. A slow four-second decorative halo adds a deliberate interruption without flashing; Reduced motion keeps it static. The overlay never takes touch focus, so Android Back, Home and system controls remain usable. Pausing Miso, taking a break or leaving a recognized selected player removes the curtain. Counting and video-screen duration stop while the video is fully covered. This is visual obscuration, not an OS-level app suspension or pause of another app's audio.

The effect caches its gradient and text layout, redraws the decorative halo at 20 fps, and creates no layout work each frame. The existing cached 3D body still drives Miso's expression and animation. Break completion now has one scheduled service callback as well as the Android alarm and app-resume fallback; stale deadline checks still prevent an old alarm ending a newer break. The break-complete notification continues to use the bundled original Miso chime. Android notification permission is granted on the connected phone; channel sound, system volume, Do Not Disturb and idle scheduling still apply.

Validation for this refinement: assembleDebug, all 68 unit tests and lintDebug pass (0 lint errors, 43 warnings). Tests cover the happy/angry boundary and monotonic, bounded veil opacity, including full coverage at 95%. On-device notification audio and frame-rate behavior are not claimed as tested.


## October 26 reference layout implementation

Home, Platforms and the Settings overview now use shared native Compose components arranged from the three supplied reference images. Light and dark use one centralized AppPalette with identical layout structure. The existing animated Miso is retained; no reference screenshots or sample usage numbers are embedded as UI.

Home has a large right-hand Miso hero, circular live reel/allowance metrics, a frameless companion row with separate Pause/Start, Hear Miso and Access actions, four selected-app cards across the viewport, four functional quick actions and a compact focus banner based on recorded duration and goal remaining. Focus mode uses the existing companion toggle and permission checks. Break time, Activity, Settings, custom goals, first-run introduction and voice replay remain accessible.

Platforms has the right-hand Miso hero, Selected/Installed summary cards, searchable filters and grouped Selected/Available app lists. Rows use actual installed state, supported platform logos, recorded counts, working switches and Open/Install actions. All, Selected, Installed, Social and Video filters operate on the existing supported catalog. New PlatformKind is static catalog metadata, not usage data. The four filter tests cover actual selection/installation intersections and query/category handling.

Settings follows the reference groups Personalize, Your boundaries, Apps & permissions, Data & privacy and Support. Icons are unframed, with no row divider lines. Existing controls open through their own categories. Manage platforms navigates to Platforms; Reduced motion controls the existing preference; Help & guidance opens local troubleshooting and working notification/accessibility actions. All existing privacy and deletion controls remain.

Navigation was explicitly restored to the previous burgundy notched bar, with its raised selected icon, existing labels, sizes, colors, no-ripple interaction, scroll visibility behavior and animations. A before/after hash check verified that its restored composable remained unchanged during the page implementation; its two gradient colors were also checked.

Validation: assembleDebug, all 72 unit tests and lintDebug pass, with 0 lint errors and 45 warnings. Both theme contrast checks still pass. Existing tracking, break, warning, activity and custom-goal tests remain green. No physical device is connected, and an isolated emulator could not start in this environment (exit 132), so final visual matching, on-device rendering performance and APK installation have not been verified for this update. The previous installed app is not claimed to contain these new layouts until the APK is installed.


## October 26 transparent controls and rendering refinement

This refinement supersedes the prior reference-layout grouping where it differs. The burgundy notched navigation component, labels, colors, geometry and selection animation are unchanged. Destination changes now compose only the current page and use a 100 ms draw-phase opacity transition instead of retaining two screens and animating layout/scale. Each tab retains its own scroll state. Android Back closes a Settings detail first, then pops the bounded, saved tab trail; only the root with no remaining history exits. Dialogs retain their own dismiss behavior.

Home now uses smaller statistic rings with larger labels and bold numbers, a fitted outlined status indicator, and a transparent rounded session-control frame. The duplicate small Miso is removed. Start/Pause, Hear Miso and Access use separate 50 dp circular touch targets with no rectangular ripple. Selected platforms are unframed, four per row, wrapping additional apps onto subsequent rows. Quick access is a transparent outlined two-by-two grid with centered icons and names. Today's focus has original local vector landscape art with a sky that follows the device's current hour; it is not live weather, a remote image or fabricated usage. The hero's colored ambient backdrop is removed.

Platforms removes its extra kicker/description and summary subtitles. Selected/Installed remain working filter actions in transparent outlined cards. Platform rows are unboxed, have no separator lines, and retain authentic icons, real installation/selection state, working neutral switches, and outlined Open/Install controls. Activity panels are transparent with shared theme-aware neutral outlines; the heading subtitle and perspective panel are removed. Graphs still use recorded per-platform data and distinguishable series colors. Settings is one compact, ungrouped list of bare icons and names, without row subtitles or category headings. Appearance uses sun, moon and phone vector symbols. Detail cards are transparent and outlined, with neutral Settings controls; all existing preferences and data safeguards remain accessible.

Miso body display lists now share a bounded eight-entry cache containing no Activity or Context references. View construction reuses cached geometry on page revisits; continuous frames do not allocate body keys or rebuild meshes. Eye/cheek rendering uses analytic antialiased ovals instead of constructing trigonometric polygons every frame, with refined highlights and a neutral shadow. Scrolling suspends the in-app mascot's idle animation and restores it when scrolling stops. Activity history snapshots are prepared off the main thread, and service-only status/acknowledgement writes no longer trigger screen-wide preference refreshes.

The social-player counter is larger, bold, tabular, frameless white text with a stronger contrast shadow and the existing brief count-change animation. Tracking, deduplication and warning thresholds are unchanged. Two additional original synthesized notification sounds (tools/generate_goal_sounds.py) distinguish approaching the goal and reaching it; the original break-complete chime remains. Settings offers previews and separate goal-notification controls. Android permission is also requested on starting Miso where needed. Milestone notifications use persisted daily per-goal stage deduplication; the exceeded-stage update is silent. Notification channels, system volume and Do Not Disturb still govern audibility, and spoken warnings retain their own preference.

Validation: assembleDebug, all 72 unit tests and lintDebug succeeded (0 lint errors, 47 warnings). No CompanionView DrawAllocation warning remains. The three generated PCM sounds were checked for duration and clipping. Latest APK: app/build/outputs/apk/debug/app-debug.apk. No physical device is connected, so this version is not claimed installed or verified for visual appearance, navigation gestures, notification playback, real social-player operation or frame rate. Source-level work reductions do not establish zero lag or a measured performance improvement on a phone.

## Theme visibility, setup guidance, history paging and exact-goal interruption

Miso and the header character now rasterize their 3D body once into an 8 MiB bounded bitmap cache, rather than nesting shared RenderNodes inside Compose's AndroidView rendering. Both hardware and software drawing use the same cached image, with native animated eyes and expressions drawn above it. Bitmaps use explicit pixel density; new instances and both small/large sizes are covered by a device rendering regression test. The brand text has an explicit theme foreground. Navigation keeps its destination names and notched geometry, with slightly different burgundy tones in light and dark mode.

Quick access now contains Focus mode, Break time, Daily goal and Counter style. The latter two open the actual goal editor and Miso display controls. Platforms places its narrower Selected/Installed filter cards directly below the Miso heading, right aligned without reducing their text sizes.

Connect Miso pins Agree & open Settings and Not now below a separately scrolling body. It includes three labelled native setup previews (find AntiReel, enable Use AntiReel, confirm Android permission), optional offline spoken guidance, and the real Android Accessibility settings action. These previews are illustrations rather than screenshots or working system toggles; labels vary between device manufacturers. The app never enables its own accessibility access. Broad-access disclosure and local-data behavior remain visible, with further details available on demand.

Activity now distinguishes three questions: Time across your week compares per-platform duration during the latest seven days; Daily history shows combined day totals, pages through seven-day windows within the retained 30 days, and switches between actual recorded duration and reel count; Where your attention goes shows platform percentages for the selected day. Older partial windows do not invent dates beyond retention. Platform legend cards show authentic logos, the selected day's actual duration, series color and a working show/hide action. Trend geometry is cached between data/size changes to reduce draw work while scrolling. All data still comes from recognized selected video screens, not total device usage, playback or guessed historic duration.

The daily boundary is now enforced at the exact goal, independently of the near-warning tint preference and overlay visibility. Both the recognition service and TrackerStore reject further increments at/above the goal. At a recognized selected player the service flushes duration, resets pending page state, requests media pause, leaves the player via Android Home, and opens a dedicated full red warning Activity with angry/talking Miso and Leave the app/Open AntiReel actions. This foreground screen receives touches instead of allowing scroll gestures through to a feed. Reopening a recognized selected player at the same daily boundary repeats the interruption, with launch debouncing. The warning screen expires at local midnight. The near-limit veil remains a warning from 80%; it no longer freezes the allowance at 95% before the goal can be reached.

The foreground warning requests permanent Android audio focus and sends a pause command; it does not change global media volume, suspend another app or grant privileged media-session control. Android 15+ only permits the focus request from a top app or eligible foreground service, hence the request from the visible warning Activity. External players and OS versions may differ in their response; universal audio suppression is not claimed. The red screen and counter gate do not depend on a player honoring pause. Spoken warnings require an installed offline English TTS voice and audible speech settings.

Validation: assembleDebug, assembleDebugAndroidTest, all 77 unit tests and lintDebug succeeded (0 lint errors, 48 warnings). New tests cover all 30 retained dates across non-overlapping seven-day windows, valid selection boundaries and exact-goal count gating. The latest debug APK was successfully installed on the connected Pixel 6 Pro. MisoRenderingTest passed on the phone, checking repeated small/large character instances against both light and dark backgrounds. Full cross-platform playback/audio interruption, hardware theme screenshots and sustained scrolling frame-rate measurements are not yet established by those checks.

### Activation, happy Miso, and one daily extension
- Fixed enabled-accessibility/paused-internal-state mismatch. Consent-backed activation starts automatically when the service connects or the app resumes; explicit in-app pause persists across reconnects. Setup approval requests activation without changing Android permission itself.
- Offline 20-second illustrated Android setup video (61 KB), narrated four-step walkthrough, optional AntiReel AI shortcut explanation, and pinned consent/dismiss actions. Android labels vary; the video is an illustration, not a device recording. Generator: tools/generate_setup_video.py.
- Miso remains happy below the exact goal and becomes angry at the limit. Breaks retain the resting expression. Platform summary centered.
- Settings > Restart allowance permits one additional 50-reel block per local day, only after reaching the goal. Keeps history, locks that day's extended ceiling even if the regular goal changes, and restores regular goal on the next day. No extra alarm rescheduling when no break is active; no redundant start preference write when already running.
- Validation: debug APK assembled and installed on Pixel 6 Pro; 77 unit tests pass; device activation/allowance and Miso-rendering tests both pass using isolated test preferences. Lint completes. Observed running=true after launch with accessibility enabled. Live cross-platform recognition, audible narration, and sustained scroll frame times still require end-to-end device checks; no zero-lag claim.

### Miso branding and release security (1.0.1)
New generated pink-bunny launcher and header assets, adaptive mask-safe foreground and themed monochrome. Accessibility event scope follows selected packages, password/editable subtrees are excluded, Android's disclosure accurately describes boundary interruption, and cleartext networking is disabled. Dedicated private release signing produces a non-debug APK; do not substitute a debug build for distribution or replace an existing differently signed installation without a data migration plan. Review, evidence, build instructions, signing-key handling and Play Protect limitations: docs/SECURITY_REVIEW.md and docs/release-apk-audit.json. 77 unit tests, four device tests and all 11 local packaged-security checks pass. Lint: zero errors, 60 warnings. This review does not certify malware-free status or Google approval. No app data was deleted and no device protection was disabled.

### Restricted-access recovery (1.0.2, 27 October 2026)
The supplied screenshot says “App was denied access” in Android Accessibility. Added trust-aware restricted-settings help, a public-intent shortcut to AntiReel’s own App info page, manufacturer settings-page fallback, and a persisted setup-return check that offers troubleshooting when the service remains off. User approval stays in Android Settings; no hidden APIs, security bypasses, Play Protect changes or automatic permission grants. Guide: Settings > Apps > AntiReel AI > More > Allow restricted settings (if offered), then enable Use AntiReel in Accessibility. An optional shortcut is not the service toggle. Android 11+ is required; device policies can still prevent access. Debug/release/test APKs build, 77 unit tests and all 11 local release-audit checks pass. New settings-intent device test compiled but cannot run while the phone is disconnected; the four prior 1.0.1 device tests are not represented as new 1.0.2 device validation. See docs/SECURITY_REVIEW.md.

### Activation investigation and live connection status (1.0.3)
Connected Pixel investigation confirmed Android ACCESS_RESTRICTED_SETTINGS=ignore and no enabled accessibility service. Installed 1.0.2 was an Android Studio debug/test-only build; a normal same-key debug update to 1.0.3 removed TEST_ONLY without clearing app data, but did not change the OS restriction. These are separate findings; the exact historical event losing the previous grant is not proven. Fixed readiness reporting to require actual onServiceConnected, consent, enabled access, a start request, selected platforms and no break; added public accessibility-state callbacks plus late-bind observation for Android 11/12 and explicit connection diagnostics. Signed release audit now rejects test-only artifacts. Added read-only tools/check_device_activation.py and docs/ACTIVATION_DIAGNOSIS.md. Final validation: 79 unit tests, five Pixel device tests, 12 packaged release checks; lint zero errors / 63 warnings. Updated development APK installed preserving data. Android approval and live reel recognition are not claimed while access remains denied. No app-op permission change, Play Protect modification, hidden API, installer spoof, uninstall or permission grant was performed.

### Production packaging and disclosure (1.0.4)
Moved the complete accessibility purpose, local-data explanation, exact-limit Home/media behavior and revocation options ahead of optional setup media. Release signing now explicitly enables modern signing and retains the existing release key. New `:app:prepareDistribution` builds signed APK/AAB, runs Kotlin tests/lint and produces checksums plus verification evidence; it rejects debug/test-only artifacts, signing-key drift, incomplete/unsigned bundles and missing native ABI/alignment coverage. The ELF parser has two regression tests. Final validation: 79 unit tests, two Python release-audit tests, 17 packaged APK checks; lint zero errors / 63 warnings. Four native ABIs, ZIP and ELF64 LOAD alignment verified; minimum Android remains 11. This release was not device-tested (phone disconnected), Google-reviewed, malware-certified or approved by Play Protect. See docs/PLAY_REVIEW_NOTES.md for the review hand-off and remaining work. No package renaming, key rotation, permission bypass or device-protection change was made.

### Full installation/accessibility audit (1.0.5)
Fixed repeated-connection polling/TTS cleanup, immediate unbind readiness, thread-safe cancellation of old inspections and pending limit screens, current consent/selection/lock/break rechecks and foreground verification before Home/media actions. Added cancellation-aware node traversal and explicit cloud/device-transfer backup exclusions. Added a safe signer/version/OS/ABI installer preflight (`tools/install_checked_apk.py`) that refuses destructive/conflicting updates and reports installation separately from permission/tracking. Validation: 83 Kotlin tests, eight Python tests, 22 release checks, lint zero errors / 63 warnings. OSV returned zero matching known records for 108 resolved public Maven coordinates; coverage is incomplete. New Android privacy instrumentation test compiles but no device is connected; an isolated emulator could not boot. No claim of installed/activated/live tracking or Play Protect approval is made. Full findings: docs/FULL_ANDROID_SECURITY_AUDIT.md. No user data was cleared and no permission was granted by tools.
