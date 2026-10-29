# AntiReel accessibility denial — investigation, 27 October 2026

## Confirmed on the connected Pixel 6 Pro

Before the latest changes:

- Installed package: com.rafi.antireelai, version 1.0.2 (3).
- Certificate: Android Debug. The dedicated signed release certificate is different.
- Installed flags included DEBUGGABLE and TEST_ONLY. Installation metadata reported com.android.shell as the initiating package, not an installed signed release from Google Play.
- Android app-op: ACCESS_RESTRICTED_SETTINGS = ignore, with a recent rejected attempt.
- Enabled accessibility services setting: null. AntiReel was not enabled or activated.
- Device installation metadata showed firstInstallTime 26 October 2026 23:27:57 and a later update. The historical transition that lost the previous grant was not available, so a specific update cannot be proven to have reset it.

After a normal, same-certificate debug update to 1.0.3 (4):

- TEST_ONLY is absent. The existing data was retained; no uninstall/reset was performed.
- Android's restricted-settings block still reads ignore and the accessibility service remains off.
- Therefore the test-only packaging issue is separate from the restricted-settings decision. Removing testOnly does not grant accessibility or certify Play Protect acceptance.

## Research findings

Google's official restricted-settings guidance says Android can restrict settings for installed apps, and trusted-app approval is performed on that app's App info page. Public app code cannot grant its own accessibility access. The screenshot matches a restricted-access denial, not an overlay-rendering error.

Google's build documentation says Android Studio Run generates test-only APKs intended for ADB deployment. For sharing, build a normal APK or signed release. This is an additional packaging issue found in the installed build, not proof of the reason for the permission denial.

A changed install route or a reinstall is a plausible explanation for access differing from a previous installation. This is an inference; there is no complete historical permission log proving the exact trigger. A signed APK also cannot automatically preserve a grant across a different signing identity. Do not uninstall a development build merely to switch certificates if its local history is needed.

Sources:

- https://support.google.com/android/answer/12623953?hl=en
- https://developer.android.com/build/build-for-release
- https://developer.android.com/reference/android/view/accessibility/AccessibilityManager
- https://developer.android.com/studio/publish/app-signing

## App-side fixes

- Require Android's enabled-service state, the actual process-local onServiceConnected callback, disclosure consent, a start request, selected platforms and no active break before showing Active.
- Observe accessibility service-state changes through public Android APIs (API 33+) and observe the service's own binding callback for Android 11/12 as well. React when binding finishes after Activity.onResume; avoid stale permission/UI state without polling.
- Clear process-local binding state when the service is destroyed. Do not persist a connected=true claim across process death.
- Display Android access and actual service connection separately in Settings > Accessibility.
- Check the exact accessibility component rather than any service sharing the package name.
- Keep restricted-settings help and user-controlled Android App info navigation from 1.0.2.
- Extend the signed-release audit to reject a test-only APK. The release still uses the same private release certificate; development updates retain the existing debug certificate for data continuity.
- Add a read-only diagnostic: python3 tools/check_device_activation.py. It reports the permission/app-op/build state without modifying it.

## Validation and remaining blocker

Final checks: 79 unit tests and five device instrumentation tests pass. Signed release audit: 12/12. Android lint: zero errors / 63 warnings. The final development update was installed with app data preserved. See the current build/test reports and docs/release-apk-audit.json for evidence. Actual cross-platform counting cannot be validated while Android blocks the service. No tool was used to enable accessibility, allow restricted app-ops, disguise the installer, alter Play Protect, mark the app as a disability tool, or change global device protection.

To complete activation for a trusted build, the device owner must use Android's App info > More > Allow restricted settings confirmation, if offered, and then enable Use AntiReel in Accessibility. If Android does not offer that option, a screenshot of the app's own App info page is required to distinguish a missing-menu/OEM restriction from an administrator/Advanced Protection restriction. These policies cannot be overridden by the app. No promise of automatic approval or support for every Android device is made.
