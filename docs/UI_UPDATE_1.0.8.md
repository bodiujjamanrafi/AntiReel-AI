# AntiReel AI 1.0.8

## Guidance

Connect Miso automatically plays a larger portrait video with embedded offline Miso narration. Each spoken direction is muxed into the same timeline as the demonstrated Settings screen, moving finger, tap contact effect and native Miso character. Pause, replay and full-screen playback keep narration synchronized. The media is 720 × 1280 at 24 fps, approximately 40 seconds long. It has no added captions or text overlays; the original Android screen labels and full permission notice remain visible.

The separate image walkthrough, How to connect section and 20-second setup button have been removed. The Settings screens appear on the right inside the video; this is an in-app demonstration, not an overlay on live Android permission dialogs. Leaving this destination or backgrounding the Activity pauses/releases playback. Runtime narration does not depend on an installed text-to-speech engine because audio is bundled in the video.

The required disclosure still appears before the Agree & open Settings action. This button has a restrained 12 dp corner radius. Android permission approval remains the user's own action.

## Layout and settings

- Miso occupies the middle navigation position. Destination identifiers are preserved so existing navigation and Back behavior do not change meaning.
- Home contains only centered Start/Pause and Hear Miso controls. The headline is slightly lower.
- Platform search appears directly below the hero. Frameless Selected/Installed totals appear underneath, without icons.
- Weekly chart platform entries no longer have surrounding frames.
- Settings detail sections use a shared frameless spacing system. Accessibility help is expandable inside Accessibility. A separate App info section contains the version and Open AntiReel App info action. Opening App info does not trigger the activation-return flow.
- Miso & display centers the live character. Extra preview captions and the limit-preview action are removed. Size, position, transparency, counter and behavior controls retain live visual feedback. Slider drags remain local until release to avoid repeated preference writes and chart refreshes.
- Header and launcher assets use transparent PNGs from the exact native Miso render. Foregrounds and legacy icons have transparent alpha, without a baked-in background square. Android launchers can still apply their own adaptive icon mask or background treatment.
- Settings entry/Back and destination changes use a short draw-phase fade/translation. Navigation retains its short notch/icon animation; standard button feedback remains responsive. Outgoing screens and video players are not retained for crossfades.

## Validation

Validation completed:

- 86 Kotlin unit tests and eight Python installer/packaging tests pass.
- MisoNarrationExportTest generated three fixed-instruction WAV files using the same installed offline English voice selection, pitch and speech rate as Miso.
- ffprobe confirms H.264 video and AAC audio at 720 × 1280, 24 fps, 39.916667 seconds; file size is 934,598 bytes.
- Pixel 6 Pro UI checks pass, including video autoplay advancing its playback position, full-screen entry/exit, navigation and centered display controls. A native video-surface frame was copied with PixelCopy and inspected, confirming actual decoded video output. Own-app screenshots and native video frames were used only for internal inspection and are not bundled as deliverables.
- Transparent PNG alpha was checked (0 through 255). Header, launcher foreground and five legacy densities retain transparency.
- Final release APK/AAB packaging passes all 22 APK checks, signer continuity and bundle validation. Lint reports 0 errors and 73 warnings.
- The final matching debug build was installed and launched on the Pixel without clearing data. Accessibility was off during these checks; live social-platform tracking remains unverified.

 This UI update does not certify universal frame rates, universal feed recognition or Play Protect approval. No user data is reset and no device permissions are automatically granted.
