# Miso, legal routing and reminder refinement — 30 October 2026

## Implemented

- Cold launches show a centered, packaged Miso logo for 650ms before the existing onboarding/dashboard route. This removes dependence on asynchronous 3D rendering and ensures restored language drafts still show the preloader. First-time onboarding skips a duplicate internal preloader. Android 12+ system splash uses the existing Miso foreground instead of a transparent icon. No corner branding image is introduced.
- Settings → Privacy & security opens the public privacy URL directly via Android browser intent. Its public navigation already includes Privacy, Terms, Security and Support. The local Private by design, Your data and Your controls groups move intact to Help & guidance, retaining permission and data-deletion callbacks.
- Help & Support opens the public support URL directly, without the former subtitle. Email support remains functional. Native Miso consent/disclosure and Terms/Services navigation are preserved.
- Breaks & reminders no longer includes the Hear Miso action. Voice/greeting preferences remain unchanged. Existing near-limit, reached-limit and break-complete notifications retain their activity triggers, sounds, permission gates, channels and identities; they carry Antireels AI subtext. No test notifications or fabricated milestones were posted.
- New restart allowances add 30 reels once per day. Existing saved absolute restart limits remain intact for today; time boundaries and historical data are preserved. Relevant English/Bengali copy and legal source wording are updated.
- Miso page uses an aligned mascot/status header, bounded portrait video, equal-width playback controls and consistently spaced access/consent/Terms/Services actions. Content can scroll on compact displays. Playback, full-screen, accessibility flow and state callbacks remain connected. New video heading is localized.
- Page changes use a short 100ms fade beginning at 85% opacity without page scaling. Settings uses 120ms. Inputs are never delayed by the animation. Bottom-navigation design, structure, labels and selection rendering are unchanged.
- Generated legal pages show Antireels AI in the brand header, titles and footer. Public-site navigation and support email remain unchanged.

## Verification

- Final debug APK and instrumentation APK build successfully.
- 169 JVM unit tests pass; 17 Python tooling tests pass.
- Lint: zero errors, 90 warnings. Existing warnings are not represented as resolved.
- Added instrumentation coverage for packaged-logo visibility, external legal intents, retained local privacy controls, 30-reel/legacy restart behavior and real setup-video playback/control alignment. Updated old routing, allowance and consent expectations. These tests compile but were NOT run this turn.
- No physical device is connected. An isolated emulator launch failed before boot. Fresh live visual, playback, notification and navigation validation remains pending; prior-turn device results are not evidence for this update.
- Four generated legal pages pass local branding/navigation/email checks. The support page includes the new 30-reel rule.
- All four existing HTTPS public URLs return HTTP 200. Their bodies still show the OLD brand; the source update is NOT live.

## Publishing blocker

The existing domain is served by Vercel. The Cloudflare connection can list accounts but lacks Pages read scope; no Cloudflare resource or DNS record was changed. Vercel CLI account lookup and a production publishing attempt failed with `fetch failed`. Publishing was attempted from a staging directory containing only static legal files and Vercel configuration, never Android source or credentials.

Ready static bundle: `dist/AntiReels-AI-legal-pages-update.zip`.
Ready debug APK: `app/build/outputs/apk/debug/app-debug.apk`.
The user's signed release APK, signing configuration and package identity remain unchanged. This update was not installed on a phone during this turn.


## Follow-up: single splash and faster navigation

- Android 12+ now keeps the first native Miso splash and skips the second in-app preloader, eliminating its additional 650ms delay. Android 11 retains a single legacy fallback because it has no native icon splash.
- The selection spring stiffness changes from 700 to 1400 with critical damping; icon/label color transitions shorten to 90ms, page fades to 80ms and Settings fades to 100ms. The bar's geometry, colors, labels, order and icons remain unchanged.
- Installed the update on the connected Pixel 6 Pro and passed 20 device tests: repeated actual navigation, no second Compose preloader, Miso video playback, public-browser routing, help/data controls, 30-reel restart, onboarding and consent flows. The tests preserve actual goals, platforms and activity history.
- Inspected a clean cold-launch recording: one continuous native Miso splash followed directly by the app, without the extra progress-indicator screen. Android 11 fallback was not tested on a physical Android 11 device.
- Current public branding remains undeployed. The existing Vercel authorization returns HTTP 403 with `Not authorized` for the linked project. Attempting a new CLI login fails before generating an approval flow (`fetch failed`). A fresh owner-approved Vercel login is required. No deployment, DNS or domain change is claimed.
