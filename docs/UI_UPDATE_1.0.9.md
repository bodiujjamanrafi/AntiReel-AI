# AntiReel AI 1.0.9

- Choose your platforms now has a clear icon, larger touch area and a restrained rounded square outline.
- Selected/Installed summary actions appear after All, Selected, Installed, Social and Video filters.
- Miso is a bounded page rather than one tall lazy-list item. The video occupies the remaining space and fits its full portrait image in the middle. Settings actions sit in a separate footer above navigation, so neither scrolling nor watching the full video is required to reach them.
- The full-screen player also includes the access action. The access purpose and local-data disclosure remain visible alongside the agreement action. Opening Settings never grants accessibility automatically.
- The guide uses the three supplied Android screenshots already present in the project (find AntiReel, review Allow, optional shortcut), synchronized with existing spoken directions. The entire reference screen is centered; permission text is preserved. No unrelated signing screenshots are included.
- Video preprocessing resizes each source only once, rather than on every rendered frame. The runtime maintains one decoder, releases it on navigation and pauses when backgrounded. No new tracking timers or per-frame preference writes were added.
- The connection label requires consent, Android service enablement and the actual service connection callback.

Validation: 86 Kotlin tests and eight Python safety tests pass. The signed release APK passes all 22 existing package checks; AAB signature and structure are verified. Release lint has 0 errors and 73 warnings. The extended HomeNavigationTest passed on Pixel 6 Pro, including native video playback, full screen, visibility of Open Settings in both modes without scrolling, and navigation to display settings.

The matching debug update installed successfully with data retained, and the phone reported Accessibility enabled at that time. A later read-only check found the main package absent while the test package remained. The retained package-specific logs confirm installation but provide no removal reason. No uninstall was performed by this task; no automatic reinstall or protection-setting change followed. Live reel counting remains unverified.
 These UI checks do not certify universal frame rates, live reel recognition or Play Protect approval.
