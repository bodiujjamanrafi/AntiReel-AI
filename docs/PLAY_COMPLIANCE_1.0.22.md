# AntiReel AI — Google Play readiness and security report

**Build:** 1.0.22 (23), package `com.rafi.antireelai`. **Reviewed:** 29 October 2026. **Support:** hello@imrafi.com.

**Status:** Code/documentation/build preparation completed; publication is NOT complete. Public legal deployment, GitHub authentication/push, owner legal decisions, physical testing and Play Console submission remain pending. No Google approval, universal compatibility, malware certification or perfect counting is claimed.

## Phase outcomes

| Phase | Result | Remaining dependency |
| --- | --- | --- |
| Android audit | Manifest, service, selected-app scope, snapshots, storage, alarms, speech, intents, dependencies and release settings inspected | Physical-device validation / OEM matrix |
| Legal documents | Privacy, Terms, Security & Permissions, Help & Support authored from implementation; native/public wording shared | Owner review of identity, audience, legal/processor details |
| Android integration | Settings legal reader, browser/email actions, data controls, independent versioned disclosure/consent, safe notification Settings opening | Broader physical/OEM scenarios and renewed real tracking consent |
| Static legal site | Responsive, system light/dark, selectable, dependency-free HTML/CSS with same Miso brand and security headers | Visual browser QA and approved hosting |
| Cloudflare | Deployment ZIP, exact scoped plan and post-deploy verifier prepared | Account access and approval; DNS/hosting unchanged |
| Public URLs | Four fixed HTTPS destinations integrated; offline copies remain readable | All live checks failed from this environment; certificates unverified |
| Play readiness | Signed release APK/AAB, policy worksheet and declaration/video instructions prepared | Play Console/account/testing/listing/review |
| Private GitHub | Clean Git snapshot and source bundle with staged/history secret checks prepared | `gh` login and private remote creation/push/privacy verification |

## 1. Technical and data audit

### Manifest / permissions

The only requested permissions are `VIBRATE`, `POST_NOTIFICATIONS`, and `RECEIVE_BOOT_COMPLETED`. `BIND_ACCESSIBILITY_SERVICE` guards the exported service so Android controls binding; it is not a runtime permission the app can grant itself. `isAccessibilityTool=false` is retained because general digital wellbeing is not disability support. Accessibility is scoped dynamically to selected supported packages; when none are selected, the app's own package is used rather than an empty array that would mean all apps.

Miso uses `TYPE_ACCESSIBILITY_OVERLAY`, not `SYSTEM_ALERT_WINDOW`. No Internet, microphone, camera, contacts, SMS, location, shared storage, installer, broad app query or exact-alarm permission is requested. Package queries identify only supported social apps and TTS engines. Launcher activity is exported; limit activity and break receiver are not. PendingIntents are explicit/immutable. Cleartext traffic and Android backup/transfer are disabled. There is no general foreground service or permission bypass.

### Accessed / stored / transmitted data

| Information | Access / purpose | Persistence / transmission |
| --- | --- | --- |
| Name/nickname, language, onboarding drafts/old answers, goals, selected apps, preferences | Local personalization/settings | Private SharedPreferences; retained until changed/clear storage/uninstall |
| Visible accessibility labels/content, player metadata, scroll/page events | Find short-video screens and estimate transitions | Bounded, transient selected-app snapshots; other visible selected-app text may be encountered; no content upload/persistence |
| Foreground window/app information | Locate selected foreground player | Root/package information checked; no full-device activity history saved |
| Daily per-platform reel counts and recognized foreground durations | Activity charts, limits and warnings | Private preferences; older than 30 calendar days pruned on later tracking writes, not a guaranteed wall-clock deletion job |
| Connection/status codes, break deadlines, reminder state, restart eligibility, consent version | Operation and user controls | Local preferences; no remote store |
| Video fingerprints / repeat state | Deduplicate accessible content | Bounded in-memory state only; process/day/eviction limitations apply |
| Optional numeric debug diagnostics | Developer-enabled troubleshooting | Android log buffer / manual export only; no labels, creators, URLs or identity hashes; no automatic upload |
| Speech text, possibly an entered name | Selected installed Android TTS engine | IPC to another Android service; installed offline-capable voice chosen; provider's separate practices must be considered |
| Fixed legal URL | External browser when user taps | No profile/query parameters attached; browser/hosting receive normal web request metadata |
| Voluntary support correspondence | User chooses whether to send email | External email app/provider and support inbox; no automatic diagnostic attachment |

The app has no account, backend, analytics/advertising/crash upload SDK or networking code. The legal site has no forms, scripts, analytics, external fonts, authentication or application cookies. This does NOT mean the browser, Cloudflare or email provider processes no request information. Their final configuration/retention cannot be asserted without account access.

Raw accessibility labels are read transiently in selected apps before recognition; it would be inaccurate to claim only video text can ever be encountered or that messages can never be present in a selected app's visible tree. Editable/password/hidden nodes are excluded; documentation now explains the actual boundary.

### Security changes / findings

- Added a standalone, localized accessibility disclosure and explicit Agree/Not now actions before Android setup. It is separate from terms/service information. Dismissal and Back do not accept.
- Persisted disclosure version 2. A legacy `consent=true` without the current disclosure does not authorize tracking; existing users must accept once again. Profile/history/settings remain intact. Android approval is still independently required.
- Added full bundled native legal documents, fixed allowlisted external destinations and guarded browser/email launch with a visible no-handler fallback. No Internet permission or WebView added.
- Notification Settings now uses the existing guarded Android Settings opener, avoiding an unhandled missing/OEM destination.
- Kept confirmed activity deletion and added direct privacy-page access to deletion, Android app settings and accessibility controls. Full local data removal remains Android Clear storage/uninstall; no account deletion system is invented.
- Expanded .gitignore to reject keystores/credentials/env/cache/build outputs and supplied an empty local signing template plus staged/history pattern scan.
- Existing SHA-256 YouTube identity/state-machine fix, all other platform rules, usage calculations, Miso body/expressions, notifications, goals, restart allowance, navigation/header and data files are preserved.

Runtime storage is Android app-private, not separately encrypted by AntiReel. Offline voice selection is based on the engine's reported capabilities, not an independent audit of that engine. Accessibility and media/Home behavior remains subject to platform policy review. Release keys are preserved and excluded from Git/deliverables.

## 2. Current Play policy checks

Official sources were checked on 29 October 2026; future submissions must recheck current Play Console requirements.

- **Target SDK:** current new/update phone-app requirement is API 36 or higher (effective 31 August 2026). This app targets API 37, so its declared target exceeds that floor. Minimum API 30 is unchanged. Console acceptance and runtime compatibility still require testing. Source: https://support.google.com/googleplay/android-developer/answer/11926878?hl=en
- **Accessibility:** general wellbeing keeps `isAccessibilityTool=false`, requires a declaration and a separate prominent in-app data-use disclosure with affirmative consent. User-defined deterministic limit responses must be fully explained; they must not disable Android controls/uninstall. A declaration video must show launch, full disclosure, acceptance/system permission, decline/reentry and the actual core feature. The bundled setup animation is not submission evidence. Source: https://support.google.com/googleplay/android-developer/answer/10964491?hl=en
- **Privacy:** the app and listing need the app-specific privacy URL, publicly readable without login, on normal HTML with valid HTTPS. The prepared URL is not yet live-verified. Source: https://support.google.com/googleplay/android-developer/answer/9859455?hl=en
- **Data Safety:** only locally processed data not sent off device is outside collection scope, but SDKs, TTS transfers, user-initiated external actions and support/site operations must be reviewed accurately. Do not infer a final blanket answer from the absence of Internet permission alone. Source: https://support.google.com/googleplay/android-developer/answer/10787469?hl=en
- **Account deletion:** AntiReel has no app-account creation. Do not claim an account-deletion feature/badge that does not exist. Answer the Console questions accurately; local deletion controls and voluntary support requests are documented. Source: https://support.google.com/googleplay/android-developer/answer/13327111?hl=en
- **New personal account testing:** if the developer account was created after 13 November 2023, current production-access requirements include at least 12 opted-in closed testers continuously for 14 days and a production-access application. Account type/date are unverified. Source: https://support.google.com/googleplay/android-developer/answer/14151465?hl=en

## 3. Data Safety worksheet — owner must validate before submission

| Console topic | Code-grounded preparation |
| --- | --- |
| Automatic off-device collection by AntiReel | None found; local nickname/preferences/app activity stay on device. On-device-only data is not declared as off-device collection solely because it is processed locally. |
| Automatic advertising/analytics sharing | None found; no ad ID, ads or analytics SDK. |
| Installed apps/app activity | Supported installation checks, selected-app counts/durations and transient content are local; no inventory/activity uploaded. Do not claim full-device Digital Wellbeing tracking. |
| Speech engine | Offline-capable Android engine receives speech text via IPC. Verify the actual engine/provider and applicable system/service-provider exceptions before final sharing answers. Do not claim no on-device transfer occurs. |
| User-initiated browser/email | Fixed URL and email draft only; no attached user state. Account for any owner-controlled site/email collection and current Console guidance for user-initiated actions. Optional support includes email address and whatever text/attachments the user sends. If classified as collection in the selected distribution/support setup, declare relevant optional personal info/messages/files for support/developer communications, not advertising. |
| Encryption in transit | App has no app-owned network transport. Do not claim proprietary end-to-end encryption. Legal pages require normal HTTPS after deployment; email/TTS provider behavior is not certified. |
| Data deletion | Local confirmed activity deletion; full storage removal via Android. No AntiReel remote account/database to delete. Correspondence requests: hello@imrafi.com. |
| Privacy URL | https://legal.imrafi.bond/privacy — NOT YET VERIFIED LIVE. |

This is a decision worksheet, not a completed or submitted Data Safety form. The owner must settle the TTS/service-provider treatment, hosting/log configuration and voluntary-email scope before choosing final Collection/Sharing answers. Adding future SDKs, uploads, accounts or telemetry requires a new audit and policy update.

## 4. Accessibility declaration draft

**Primary purpose:** App functionality — a user-controlled short-video wellbeing limit, not advertising, general surveillance, autonomous planning or a disability accessibility tool.

**Suggested explanation:** “AntiReel AI uses AccessibilityService, after explicit disclosure/consent and Android approval, to inspect visible accessibility content and scroll/page events in selected supported social apps. It identifies short-video players, estimates reels/time, rejects observable repeats, and displays Miso/counters through an accessibility overlay. When the user-defined reel/time limit is reached, it requests media pause, performs Home and shows AntiReel’s warning. These actions follow the user's deterministic settings. The user can pause tracking, revoke permission, use Home/Back or uninstall. Raw screen content is not stored or uploaded; only local counts/durations/settings/status are persisted. No disability-tool flag, gesture injection or security bypass is requested.”

The exact English/Bengali user disclosure is maintained in `AccessibilityConsent.kt` and shown before accepting/launching Android setup. Reviewers should use the app normally: complete onboarding, choose a platform, tap Start/Miso, read disclosure, consent and enable the service manually. No reviewer login is needed. Do not claim all users/devices can grant access if Android blocks it.

**Video still required:** record this exact release on a real unlocked Android device. Show both decline and later acceptance, complete disclosure text (scroll slowly if needed), the real Android permission screen, Miso counting in a selected platform, the configured limit response, pause and permission revocation. Use no unrelated private content. The video must be accessible to Google reviewers; no video has been recorded/uploaded here.

## 5. Legal review items before publication

1. Confirm the developer legal identity/contact/location that must match the verified Play account and any applicable public-address requirements. No company identity was invented.
2. Confirm intended age groups/countries and whether any child-directed distribution is intended. No age verification, family account or parental-control system exists. Do not select an audience category merely to skip policy requirements.
3. Confirm governing law/competent jurisdiction with appropriate counsel if an exclusive clause is desired. Current terms preserve mandatory rights and do not invent one.
4. Confirm support mailbox access, provider, retention/deletion process and any applicable legal obligations. No promised response SLA or fixed retention window was invented.
5. Review Cloudflare/security logs, privacy settings and any processor obligations after account configuration. Keep analytics/scripts disabled or update disclosures accordingly.
6. Confirm social-logo/artwork rights and attribution/licensing for distribution. App source contains platform identification marks; private Git or a local build is not proof of third-party licensing approval.

These are factual/legal approvals for the developer, not tasks an automated build can certify.

## 6. Build, tests and unresolved verification

The release verification/checksum JSON identifies the signed APK/AAB, signer continuity, ABI coverage, ZIP/native alignment and packaged manifest checks. Unit, Python/static, lint and secret-scan totals plus Git commit identity are recorded in the adjacent machine-readable compliance verification file after final validation. The OSV result lists the timestamp and actually queried dependencies; zero known matches is not proof of vulnerability absence.

The Pixel 6 Pro reconnected unlocked late in this session. The matching development-signed APK was installed and launched with the checked updater, preserving the package/signing identity and issuing no uninstall, reset, downgrade or permission-grant command. Eleven phone tests passed: LegalConsentTest (3), AccessibilityPrivacyTest (6), SettingsRoutingTest (1) and LegalReaderTest (1). The reader check covers four documents in both themes at 320dp/1.5 font scale (8 states). Navigation tests preserve real goals, selections, theme, restart state and history; consent tests use isolated preference data. Android accessibility remains enabled; the revised in-app disclosure requires renewed owner consent before real tracking. Continuous Miso counting/statistics, reminder scenarios and the earlier 30-consecutive-Short test remain pending. No artificial setup animation is presented as real-device evidence.

All four public HTTPS checks failed from this environment; there has been no deployment or certificate validation. GitHub creation/push/privacy cannot be verified before authenticated access. The local Git snapshot is prepared safely outside the root's protected `.git` path; no history was discarded or force-pushed.

## 7. Owner's Play Console checklist

- Verify developer identity/account type/contact, device verification where applicable and distribution countries.
- Confirm package availability and existing Play signing/upload-key enrollment; keep the release key safe and use the correct upload key for your actual listing. Do not rotate identity to evade a security classification.
- Publish/verify all four legal pages and enter the privacy URL in Console.
- Complete Data Safety, accessibility declaration/video, target audience, content rating, ads declaration (no ads in current build), app access (no account), contact and other applicable App Content declarations.
- Prepare accurate title/descriptions, icon, feature graphic and genuine device screenshots. Describe the Accessibility API use and counting/intervention limitations; do not market guaranteed blocking or exact counts.
- Upload the signed AAB to internal testing, inspect pre-launch/vitals/permission and native compatibility results, then perform required closed testing/production-access application if applicable.
- Validate denial/revocation, daily boundaries/restart/time logic, notifications, reboot/break behavior and multiple supported platform/Android versions without bypassing controls or erasing real user data.
- Submit for Google's review only after the pending items pass. A successful build or target SDK alone is not approval.

## 8. Changed implementation files

`AccessibilityConsent.kt`, `AccessibilityDisclosure.kt`, `TrackerStore.kt`, `MisoPage.kt`, `MisoLegalPage.kt`, `LegalDocuments.kt`, `LegalReader.kt`, `ExternalLinks.kt`, `SettingsSections.kt`, `MainActivity.kt`, `app/build.gradle.kts`; legal JSON assets, static site source/content/headers; new legal/consent, static-site and secret-audit tests; .gitignore/signing template/README; deployment, GitHub, audit and verification tools/docs. Manifest permissions and tracking algorithms are unchanged from 1.0.21.
