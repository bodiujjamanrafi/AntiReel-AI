# AntiReel AI 1.0.20 — stable Settings transitions and restored hands

## Changes

- Settings pages use a short 140ms opacity-only entry. Horizontal translation and page scaling are removed for Settings, including Back and the Settings entry to Manage platforms.
- Animation state is initialized for the current route before its first draw, avoiding the previous rest-position → shifted-position jump after the first frame. Rapid route changes replace the animation state rather than queueing transitions. Reduced-motion behavior remains supported.
- The navigation bar stays visible within Settings and its Manage platforms view. Their usable content height therefore stays constant while scrolling, entering subpages and returning. The bar's design, colors, icons and interactions are unchanged; other screens retain their existing scroll behavior.
- Restored the two original hand meshes. The complete native body renderer is byte-identical to version 1.0.16, before the hands were removed. Other body parts, expressions, cached textures and behavior remain intact.

## Validation

133 unit tests, four device checks and eight Python packaging/installer safety tests pass. The Settings motion regression check samples 72 frames across repeated entry/Back transitions and confirms unchanged content position and dimensions. Native checks also cover all Settings routes and Android Back, deletion cancel, rapid tab switching, retained preferences/history, and Miso rendering at small/large sizes on both theme backgrounds. The native character export was visually reviewed.

Release APK/AAB signature, integrity and all 22 packaging checks pass. Lint: 0 errors, 81 warnings. The matching development APK is installed without uninstalling or clearing data. Tracking, allowance, notification and permission logic are unchanged. No universal-device or frame-rate guarantee is claimed.
