# AntiReel AI

Native Android digital-wellbeing app with Miso, configurable short-video boundaries, local per-platform activity, reminders and English/Bengali UI. Kotlin + Jetpack Compose; no application server, account, Internet permission, ads or analytics SDK.

Current version: **1.0.22 (23)**. Minimum Android **11 / API 30**; compile/target SDK **37**. Counting estimates depend on the accessibility signals exposed by each platform. Exact counting, universal blocking, Play Protect approval and Google Play approval are not guaranteed.

## Project layout

- `app/src/main/` — native UI, Miso, local preferences, accessibility tracking and reminders.
- `app/src/test/`, `app/src/androidTest/` — unit and physical/emulator instrumentation tests.
- `legal-site/content/` — reviewed legal/support wording, shared with bundled native documents.
- `legal-site/build.py` — dependency-free static legal-site generator.
- `tools/` — release audits, checked installer, legal verification and secret scanning.
- `docs/PLAY_COMPLIANCE_1.0.22.md` — audit, Data Safety worksheet, remaining publication work.
- `docs/GITHUB_SETUP.md`, `legal-site/DEPLOYMENT.md` — account/deployment handoff.
- `docs/PROJECT_HISTORY.md` — prior release notes and validation history.

## Build locally

Install Android Studio with its bundled JDK, Android SDK/platform/build-tools 37, and the Gradle wrapper dependencies. Open this directory in Android Studio. Configure your local SDK path in `local.properties` (not tracked).

```sh
./gradlew :app:testDebugUnitTest :app:assembleDebug
# Requires a connected, user-approved device/emulator; never changes real settings to pass tests.
./gradlew :app:assembleDebugAndroidTest
# Optional instrumentation execution should use isolated test data and a reviewed test scope.
python3 legal-site/build.py
python3 -m unittest discover -s tools/tests -q
```

`app-debug.apk` is for development. Keep the installed app’s signing identity; a development key is not interchangeable with the production key. Do not uninstall/reset app data merely to switch certificates.

## Production signing and bundle

Copy `antireel-signing.properties.example` to `antireel-signing.properties` locally. Fill it using the **existing** private release key stored outside this repository. Never commit filled credentials or keystores. A missing signing configuration fails the release build rather than falling back to a shared debug key.

```sh
./gradlew :app:prepareDistribution
```

This builds the release APK/AAB, runs unit tests/lint, checks signing continuity, packaged manifest/security settings, archive integrity, supported ABIs and 16 KB native alignment, then writes verified artifacts to ignored `dist/`. AAB is for Play Console, not direct installation. Play App Signing enrollment/upload-key decisions remain with the developer; preserve existing package and key continuity.

## Privacy and access

Tracking requires the current versioned in-app accessibility disclosure/affirmative consent and separate Android approval. Old consent flags alone no longer authorize the revised disclosure; existing users must accept it once without losing history/preferences. Decline/dismiss does not grant access. You can pause or revoke accessibility at any time.

Local activity can be deleted with confirmation in Settings → Activity & storage. All profile/draft/preferences can be removed using Android App info → Storage & cache → Clear storage. No AntiReel account or remote activity store exists. The privacy policy explains speech-engine, external browser, voluntary support email and diagnostic exceptions.

## Legal pages and private backup

The fixed legal destinations are `https://legal.imrafi.bond/privacy`, `/terms`, `/security` and `/support`. **They are configured but not deployed/verified yet.** Settings includes the same complete offline documents and safe browser/email actions. Build the static files with `python3 legal-site/build.py`; review `legal-site/DEPLOYMENT.md` before any Cloudflare/DNS change.

The intended GitHub repository is `bodiujjamanrafi/AntiReel-AI`, private. **Remote creation/push is pending authenticated access.** Follow `docs/GITHUB_SETUP.md`; do not upload signing credentials. No unrelated DNS, VPS or website changes are part of this project.

## Verification boundaries

Unit/build/static tests are not a physical-device compatibility certification. This release has no connected-phone validation; accessibility approval, denial paths, live Miso/counting/statistics, notification behavior and the earlier 30-consecutive-Short check still require a real device. Check the compliance report before submitting to Google Play.

Support: **hello@imrafi.com**. Security reports use the same address; do not attach credentials, private keys or unrelated private screen content.
