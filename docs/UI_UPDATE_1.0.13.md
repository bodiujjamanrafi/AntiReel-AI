# AntiReel AI 1.0.13 — completion, navigation and reel tracking

## Changes

- Replaced the default completion alert with a custom theme-aware dialog: native celebrating Miso, clear plan heading, distinct daily time/reel metrics, full-width primary action and quiet review action. Content scrolls on compact displays; saving remains explicit and failure-aware.
- Answer editing opens the existing review inside Settings in a full-screen dialog. It no longer clears onboarding completion or replaces the app with a fresh onboarding journey. Back from a question returns to review; Back from review returns to the same personal-settings page. The actual Android Back dispatcher is covered by an isolated device test.
- Navigation destination, navigation visibility, settings detail and navigation back stack are session state. A recreated/cold Activity begins at Today instead of restoring a different navigation destination. Returning from Android permission settings in the same Activity retains its setup context.
- A new completed profile gets a short, persistent first-use guide on Today and Platforms: choose platforms, then connect Miso in Accessibility. The action follows real platform selection. The guide clears only when platforms, app consent and Android accessibility approval are present. Existing completed users are not forced through it again. English and Bengali copy are supplied.
- Extracted a bounded page adapter shared by all supported platforms. Unchanged/recycled single-item indices no longer suppress pixel scroll evidence; nonzero scroll deltas work even when absolute scrollY is fixed at zero. Multi-item RecyclerView events use pixel movement, while actual page-index changes remain supported. Small partial motions, reverse movement and stale partial-scroll accumulation are handled explicitly.
- Removed the invented permanent page-zero position from count eligibility. The first metadata-only reel no longer freezes later observed forward positions.
- A settled metadata candidate remains eligible when forward-scroll evidence arrives later. Pending transitions must settle before being counted, preventing a previously rejected candidate from prematurely counting a subsequent page.
- Video titles/captions take precedence over creator metadata; a channel name alone no longer becomes the supposed unique identity of every Short from that creator. Known replay fingerprints, backwards scrolling, ads and unsupported screens retain their guards.
- Strict counting with no usable identity no longer schedules rapid pending scans forever. Event-driven inspection retains a short settling interval rather than incrementing blindly on every scroll callback. Accessibility event scope and capability declarations are unchanged.

## Verification

- 111 Kotlin/JUnit tests pass. Added cases exercise recycled Shorts indices, constant scroll offsets, RecyclerView ranges, partial movement, forward/back/revisit behavior, metadata arriving before the pager on all ten supported platforms, hidden metadata, ads, and creator/title identity.
- Eight Python installation/packaging safety tests pass.
- Ten isolated phone UI tests pass on the connected Pixel 6 Pro: previous onboarding/preferences checks plus answer-editor system Back and first-use guide routing. The full English and Bengali journeys exercise the redesigned completion dialog. Test data is stored in disposable preference files, not the user's saved history.
- A separate MainRoutingTest was compiled and attempted against the existing profile. Another application came to the foreground and Compose lost its test hierarchy, so this integrated check is not reported as passing. It remains available for a quiet, completed-profile device run. No target-UID AndroidRuntime crash appeared in the inspected log.
- Release APK and AAB build successfully. Lint: zero errors, 83 warnings. All 22 release APK checks pass; APK and bundle signatures, release-signer continuity and archive integrity pass.
- The matching development-signed version 1.0.13 (code 14) installed and launched over the existing development build without uninstalling or clearing data. Android accessibility was initially off and later reported enabled; no permission-grant commands or security-setting changes were used.

## Practical limits

No live end-to-end Shorts/Reels count was verified against each installed third-party application in this run. The platform fixtures exercise actual shared counting logic, not a compatibility certification for every app version. Accessibility evidence arrives asynchronously and may be incomplete; a short stabilization interval is intentional. Do not describe the counter as instant, mistake-free or universally supported. No zero-lag or Play Protect guarantee is implied.

The archive contains source, release artifacts and verification notes, excluding local signing credentials, keystores, build caches and user data. The release signer differs from the installed development signer: do not uninstall the installed app to force a certificate switch and lose its data.
