# AntiReel AI 1.0.7 — Miso destination and live display controls

## Changes

- Removed the outer fill and frame from Reels today and Daily allowance. Their progress rings, numbers and icons remain.
- Replaced framed platform Open/Install controls with aligned text actions. Selection checkmarks remain frameless.
- Added a fifth navigation destination, Miso, with an unfilled sketch of Miso’s asymmetric ears, flower and happy face. Existing Today, Platforms, Activity and Settings destinations retain their indices and names.
- Start opens the Miso page when the companion is inactive; Pause still pauses immediately. The Access action and settings connection actions also open this page. Android Back uses the existing destination history.
- Replaced the Connect Miso dialog with an ordinary scrolling page. It shows genuine connection state, the highlighted 20-second setup video, three individually selectable steps, the supplied Android screenshots, narration, clear access purpose/privacy disclosure, explicit consent and expandable troubleshooting.
- No success is inferred from opening Settings or enabling only the optional shortcut. Framework access plus the actual service callback is required to show connected. No permission confirmations are tapped by AntiReel.
- The three screenshot resources come from image(6).png, image(7).png and image(8).png supplied by the user. They are examples, explicitly labelled as such. The video preserves the Settings screens and adds a moving pointer and tap indicators. Android labels vary by device.
- Reworked Miso & display into a shared live preview and frameless controls. Size, position, Miso transparency, number transparency, platform name, countdown mode, expressions and reduced motion all affect the preview. A local limit-reaction preview does not change activity history.
- Dragging sliders changes local preview state; preference writes occur once when the drag ends. A fixed-size native view is scaled in its graphics layer so drags do not regenerate Miso’s 3D body bitmap for every size value. Only the selected page and optional video are composed; the video pauses on backgrounding and releases when closed.
- Launcher artwork and the transparent header logo now use an actual CompanionView render, rather than generated lookalike artwork. The themed launcher silhouette uses the same alpha mask. Native export and standard asset preparation are reproducible with MisoAssetExportTest and tools/prepare_launcher_assets.py.

## Validation

The matching debug build was installed as a data-preserving update on the connected Pixel 6 Pro. The service was not enabled during these checks, so live social-platform tracking is not verified by this UI update. No uninstall, data reset, permission grant, installer disguise or device-protection change was performed.

Validation completed:

- 86 Kotlin/JUnit tests pass, including platform-share calculations. Release lint reports zero errors and 65 warnings.
- Eight Python installer/packaging safety tests pass.
- On Pixel 6 Pro, the extended HomeNavigationTest passes: home metrics, Activity, Platforms, Miso guidance, Settings → Miso & display, and the visibility preview control. Native Miso rendering on both theme backgrounds and asset export also passed. An initial UI test race during root registration was addressed by waiting for each destination’s semantics; the final test completed successfully.
- Exact own-app UI previews were exported from the Compose root for internal inspection; no screenshots of other apps were captured or bundled.
- Native launcher export includes happy Miso, transparent background, normal and themed Android density assets.
- Release APK/AAB packaging validates signer continuity, ZIP integrity and all 22 existing APK audit checks.

 These checks do not establish universal frame rates, universal platform recognition or Play Protect approval. The signed release and the installed debug build have different certificates and are not interchangeable as an in-place update.
