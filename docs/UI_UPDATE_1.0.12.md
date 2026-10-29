# AntiReel AI 1.0.12 — theme, preferences and stable onboarding

## Changes

- Language now leads to a dedicated theme page, followed by welcome and the existing twelve questions. Device, Light and Dark use shared theme previews. A selection immediately updates the existing persisted app theme and remains editable in Settings. Existing saved question indices retain their meaning.
- Back navigation explicitly follows question → welcome → theme → language, rather than relying on arithmetic across special stage identifiers.
- Continue has a reserved bottom footer outside the scrolling question body. The preloader reserves the same footer space and does not expose a premature button. Language changes, theme selection, welcome and question content do not move its bottom anchor. Keyboard insets still keep controls above the keyboard.
- Question options have clear radio/multiple-choice indicators and restrained selected colors. Content scroll returns to the top on a new question or review. Miso and the existing typography/palette are retained.
- Question-page buttons, choices, review actions, theme/language selection and duration-wheel changes use light haptic feedback. The existing Touch feedback preference and Android haptic settings remain respected.
- Removed the artificial answer-transition delay when speech is muted. Spoken replies remain deliberate, with a visible responding label; draw-phase transitions do not delay page selection.
- Language & personal preferences now groups profile, language, appearance, daily plan, Miso voice and answer editing in a frameless layout. Voice controls save their related flags in one preference edit. Language/theme/profile changes retain history.
- Explicit settings revision inputs ensure preference and Miso-display controls refresh after writes even with Compose skipping unchanged object parameters.
- The Miso terms/services checkbox starts selected by default. Users may uncheck it. Nothing is persisted as accepted merely by opening the page; the existing Agree & open Settings action records acceptance only when pressed. Android accessibility approval remains a separate manual system action.

## Validation

- 100 Kotlin/JUnit tests pass; the added route tests cover theme ordering and backward-compatible question navigation.
- Eight Python installer/packaging safety tests pass.
- Eight isolated device UI checks pass on Pixel 6 Pro: full English journey with saved Dark theme; fixed footer bounds through language/theme/welcome/question; haptic callbacks and disabled-feedback preference; Bengali custom-goal restoration; failed save; incomplete-summary recovery; compact/large-font layout; immediate profile controls; and default agreement/manual-action behavior. The footer bounds and journey/haptic behavior share one test, so there are eight test methods in total.
- All test preference changes use disposable files. No real profile, reel history, permission, protection setting or device lock setting is rewritten. Test-window keep-awake is scoped to each fixture and removed afterward.
- Release build succeeds; lint has zero errors and 83 warnings. All 22 APK packaging checks pass, with APK/AAB signature continuity and integrity verified.
- The matching debug APK 1.0.12 (code 13) installed and launched as a data-preserving update on the connected Pixel. The production release retains its existing signer and is not an in-place replacement for that debug-signed installation.

These checks establish the listed behaviors on the connected device, not universal frame-rate or Play Protect approval. No live social-player tracking was exercised while Android accessibility remained off. Signing credentials and user data are excluded from the archive.
