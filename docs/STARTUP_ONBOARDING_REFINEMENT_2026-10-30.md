# Startup, onboarding and Facebook Lite removal — 30 October 2026

## Changes

- Facebook Lite is archived and has no active detector. It is absent from built-in selection, installed-app custom selection and onboarding choices. Its package is removed from static Accessibility subscriptions and explicit visibility declarations. The service's existing selected-detector filter excludes it. Saved activity keys, historical labels, raw legacy selections and analytics ordering remain intact. Attempts to add it programmatically are rejected.
- Android 12+ native splash resources explicitly suppress the inherited app icon/branding, with a neutral theme-aware startup surface. Android 11 keeps a compatible base theme. The existing animated Miso preloader is now its own centered, mascot-only layout; no question controls or app wordmark are drawn in it.
- The activity waits for initial Android window insets before allowing drawing. System-bar configuration runs on theme changes rather than every composition. The language footer retains its existing weighted, safe-area-aware layout and fixed minimum reserved space.
- The final plan dialog uses a compact header with one 80dp Miso, 22sp title, grouped daily metrics and full-width primary/secondary actions. All existing localized text, data, callbacks, busy state and save-failure behavior are retained. Content scrolls on constrained displays.
- Bundled and hosted-source support documentation now explains Facebook Lite removal. The hosted website was not deployed in this task.

## Verification

- Debug APK and instrumentation APK compile.
- 169 JVM unit tests pass, including archival/history and disabled Lite-detector checks.
- Lint completes with zero errors and 90 warnings; existing warnings are not claimed resolved.
- 17 Python tooling tests pass.
- Same-signature, data-preserving debug update installed on the connected Pixel 6 Pro. No uninstall, preference reset, permission grant or signing-identity change.
- 10 physical-device OnboardingFlowTest tests pass: English/Bengali journeys, summary actions, real allowance persistence in isolated test preferences, save failure, missing answers, back navigation, large-font/small-layout footer reachability, haptics, mascot-only preloader/footer stability and archived Lite history with tracking disabled.
- Tests use isolated preference files, preserving the owner's actual onboarding, allowance and tracking data.

## Limits

Facebook Lite tracking has been removed, not repaired or verified. No live Lite counting claim is made. Compose tests cover the preloader and footer, but exhaustive cold-start frame capture across Android versions, launcher implementations and navigation modes was not performed. Light/dark colors use the existing theme tokens; exhaustive visual approval on all screen sizes is not claimed. The bottom navigation and other platform detectors were not changed.
