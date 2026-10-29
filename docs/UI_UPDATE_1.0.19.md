# AntiReel AI 1.0.19 — Settings refinement and responsive interactions

## Scope

Completes the grouped Settings subpages and refines their controls, motion and layout. The application header and bottom navigation design, colors, icons, names and destination order remain intact. Existing preference keys and domain operations are reused.

## Controls and layout

- Miso & Display has four simultaneously visible category controls in a responsive 2×2 grid: Size, Position, Miso visibility and Number visibility. There is no horizontal selector or rectangular category border. Each category name appears once; selection uses typography, icon color and a small pink underline.
- Expressions & motion remains a separate group with every existing behavior preference. The native Miso preview remains mounted while the small slider panel crossfades.
- Preview size, position and both visibility controls update local drawing state during drags. A fixed AndroidView and graphics-layer scale/translation/alpha avoid native view remeasurement or body regeneration while dragging. Preferences are applied on gesture completion using the existing bounds and setters.
- The slider retains Material gesture, range and accessibility behavior with a 4dp track, 18dp thumb and prominent numeric value. The larger preview reserves space for the full character and scales its height for larger fonts.
- Preference switches observe their own SharedPreferences keys rather than depending only on a page refresh. Their event handlers read current state, update it synchronously, and invoke the same asynchronous preference writes. This prevents stale parent state and rapid repeated taps from losing the final value. External preference changes are observed and listeners are disposed when the UI leaves composition.
- Goal, reminder and Accessibility subpages also receive the existing revision signal to refresh non-boolean summaries and external edits. Goal drafts retain the correction that prevents the next edit being replaced by an already-saved value.
- Notification preferences remain distinct from Android permission. When Android delivery is blocked, the relevant rows explain the requirement and retain the existing notification-settings action. No permission is granted automatically.
- Settings dividers use opaque, theme-aware 1dp lines. Destructive action text uses readable light/dark error colors. Contrast checks cover both.
- Activity & storage retains real record-day and file-size statistics. Metric cards have equal heights at ordinary font scales and stack for larger text. Storage metadata is loaded off the UI thread. Information is grouped concisely; the warning treatment is understated. Delete still opens the original confirmation and preserves the original data-protection behavior.
- Language & personal preferences keeps the profile, language, allowances, Miso voice and answer editor. Duplicate theme controls are removed from this Settings page. Appearance remains the sole Settings destination with a theme selector. Existing onboarding theme selection is unchanged.
- All other Settings destinations retain the grouped component system, short headers, readable labels, expandable help and original actions. Manage platforms retains the original Platforms destination and callbacks, with a compact Settings entry presentation.

## Motion and navigation

| Interaction | Implementation |
| --- | --- |
| Settings list/detail entry or Back | 16dp directional translation and fade, 220ms emphasized easing |
| Main tab entry | 160ms incoming fade/scale, immediate destination update |
| Miso category indicator and slider panel | 160ms indicator/color transition and panel crossfade |
| Expanded help | 200ms expand / 180ms collapse with a short fade |
| Switch thumb | 150ms draw-phase transition |
| Navigation selection | Critically damped spring, stiffness 700; existing notch and floating indicator drawing |
| Navigation icon and label colors | 140ms transition |

Page motion uses one cancellable incoming layer. It does not retain outgoing charts, videos or native Miso views. Repeated route changes replace the previous transition rather than queueing it. The rest of the screen remains interactive throughout.

App Reduced motion and disabled Android animators remove the new transforms/transitions. Compose also applies Android's animator duration scale. Each Settings page and its index have separate scroll state. Settings content uses a visible viewport above the floating navigation bar, preventing rows brought into view from receiving taps underneath the bar; the space is reclaimed when the bar hides. Other main-screen layouts are unchanged.

## Verification

133 Kotlin unit tests, 11 native device checks and eight Python packaging/installer safety tests pass. All 12 Settings subpages were rendered in English/Bengali and light/dark at 320dp and 1.5 font scale (48 rendered states). Targeted checks verify the four-category selector, uncommitted live drag, first-tap and rapid switch state, external preference updates, persistence, theme-control placement, external goal edits, Android Back, deletion confirmation/cancel and rapid tab changes. Debug/instrumentation APKs compile. Release APK/AAB signatures, integrity and all 22 packaging checks pass. Lint: 0 errors, 81 warnings. The final matching-signature development APK was installed without uninstalling or resetting data. Native tests use isolated preference files for edits; the real-profile navigation test verifies Back, tab destinations, deletion cancel, rapid tab selection, unchanged goals/platform/theme/language and recorded history. No real user allowance, restart bonus, history or Android permission is changed for testing.

A byte comparison with the last delivered source confirms that the manifest, tracking source files, TrackerStore, BreakReminders, MisoSpeaker, CompanionView and CompanionBodyCache are unchanged (17 files compared). This is not new live-platform counting verification.

Native UI checks and rendered-screen inspection do not establish an FPS guarantee or performance across every Android device. No frame-rate benchmark is claimed.

## Motion references

- [Material motion overview](https://m3.material.io/styles/motion/overview)
- [Android Material motion patterns](https://developer.android.com/codelabs/material-motion-android)
- [Compose animation quick guide](https://developer.android.com/develop/ui/compose/animation/quick-guide)

## Installation

The included development APK matches the signer of the connected phone installation. The production APK/AAB retain the existing production signer. Never uninstall or clear app data to resolve a certificate conflict.
