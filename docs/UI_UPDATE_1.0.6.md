# AntiReel AI 1.0.6 — home, activity and activation update

- Home: matching metric cards, smaller proportionate rings, readable totals, video/hourglass icons. Standby has no frame.
- Navigation: 100 ms draw-phase notch movement, 80 ms icon color feedback, 70 ms subtle content fade. Section selection is immediate; the animation does not gate navigation. Only the active page is composed.
- Platforms: selection checkmarks have no box or ripple, while retaining checkbox semantics and a 52 × 44 dp target.
- Activity: chart history reloads only after real usage/count changes, clearing history, or returning to the foreground. Preference transactions coalesce for 60 ms before reading a new immutable history snapshot. Service data is persisted in approximately three-second batches and flushed when the app resumes. No artificial ticking or invented total app usage is shown.
- Donut: matching colored platform segments, leader lines, platform names and percentages around the ring. Side lanes distribute labels without overlaps. All active recorded platforms are included, with a complete accessible description. Time and Reels remain distinct views; historical dates remain historical.
- Setup: a highlighted video action, 20-second / 24 fps portrait motion demonstration with moving pointer, tap pulses, service switch, consent and optional shortcut. Guided steps appear first. Accessibility troubleshooting expands on demand. The required access purpose and privacy disclosure stays visible before consent; consent and Not now remain pinned. Video pauses when the Activity leaves the foreground and releases playback when closed.
- Guidance is explicitly an illustrated demonstration. The supplied activation screenshot sequence could not be resolved: the retrieved recent images contained signing dialogs or older app UI. No private signing screenshots were embedded. Exact Settings screenshots still need to be attached.
- AntiReel does not monitor or draw over Android permission dialogs to infer intermediate setup progress. Only actual framework service connection is used as success; manual steps and video are labelled guidance.

## Validation

Validation results and device status are appended after the final build. Universal frame rates, recognition on every player, and Play Protect approval cannot be inferred from local tests.
