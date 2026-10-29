# AntiReel AI 1.0.11 — completed onboarding checkpoint

## Experience

The interrupted 1.0.11 implementation is retained and completed in the existing native app. No dependency, package identity or release-signing key was changed.

The journey includes a short native Miso introduction, English/Bengali language selection, welcome, twelve questions, single/multiple selection, custom answers, a scrolling hours/minutes wheel plus accessible text entry, custom reel targets, answer-dependent dialogue and poses, editable review, and a separate confirmation with a finite Miso celebration. Motion uses the existing cached native body; text transitions affect draw opacity and respect reduced motion. Idle Miso stays happy; limit warnings retain their angry reaction.

Names accept Bengali combining marks. Invalid, incomplete or out-of-range answers cannot finish the journey. Review displays actual custom answers instead of just the words “Custom duration”. A restored incomplete summary returns to its missing question. Final completion occurs only after the validated profile saves successfully; a failed save stays in onboarding with a retry message. Draft state and the selected question are restored from local preferences. No activity history is cleared.

## Language and speech

The selected language now drives shared dashboard, platform, navigation, settings and dialog text through one typography wrapper and local catalog. Dates use the selected locale. Platform brands, version data and user names remain unchanged. Privacy/accessibility text includes the locally stored profile and onboarding choices.

Miso initializes from any installed offline voice, preferring the selected language; initialization no longer depends on English being installed. Welcome and answer reactions support both languages. Global voice mute is respected. Missing Bengali offline speech has a visible text fallback. The existing example Android setup video remains the supplied system-screen footage; Bengali users get synchronized Bengali directions and optional offline Bengali narration instead of competing English audio. The original Android screenshots themselves retain the device language in which they were supplied.

## Real allowance behavior

Time and reel targets are independent; reaching either ends the daily allowance. The home allowance, Miso mood, warning milestones, notifications and limit screen now describe the same rule. Buffered usage flushes when it reaches the time threshold, rather than waiting for the ordinary three-second batch. No historical time is guessed from reel counts. The extra 50-reel restart remains a reel allowance; it does not erase time already recorded or waive an enabled time boundary.

## Fix found on the phone

The Bengali draft-restoration test exposed a reentrant Android/Compose layout callback caused by an unnecessary immediate wheel scroll during initial layout. The wheel now requests deferred scrolling only when its target index actually changes. Restoring a custom duration subsequently passes on the Pixel.

## Verification

- Release lint reports zero errors and 83 warnings.
- 98 Kotlin/JUnit tests pass, including dual allowance boundaries, day-long durations, incomplete/custom answers, review values, pose choices and translation identity.
- Eight Python installation/packaging safety tests pass.
- Six isolated preference/device UI tests pass on Pixel 6 Pro: the full English journey; Bengali custom limits and draft recreation; failed final storage; incomplete-summary recovery; 320 dp/1.8× font layout; Bengali dashboard/settings/navigation in light/dark palettes.
- Native Miso rendering and speech-only mouth rendering tests both pass on the device.
- The first grouped screen checks timed out while the phone was locked. After it was unlocked, the fixtures kept only their own test window awake and restored that window flag afterward. No device timeout, lock, protection or permissions setting was changed.
- The matching debug update preserves the installed certificate and real app history. Test questions and profile changes use disposable, isolated preference files.
- Release packaging checks APK/AAB signing continuity and integrity and the existing 22 APK audit checks. Release lint/build results are recorded in the generated distribution verification and build output.

## Practical limits

Draft recreation was tested by disposing and reconstructing the UI from committed preferences; this is not proof against every OS process-kill scenario. Layout checks cover one simulated compact/large-font viewport and the connected Pixel, not every manufacturer. Native pose/speech rendering and allowance logic pass; actual accessibility service activation and live social-platform limit handoff remain unverified while Android accessibility is off. No claim of universal frame rates, perfect player recognition or Play Protect approval is made.

The release APK cannot overwrite the debug-signed copy on the Pixel in place. Do not uninstall to resolve certificate differences without a data-preservation plan. Signing credentials and user data are not included in the project archive.
