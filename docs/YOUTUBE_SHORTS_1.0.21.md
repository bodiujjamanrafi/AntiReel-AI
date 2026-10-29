# YouTube Shorts counting — 1.0.21 (version code 22)

## Scope and findings

This patch changes YouTube tracking, not the UI, settings, daily limits, other platform recognition rules, or their counting state machines.

The audit confirmed several paths in the previous implementation that could lose transitions:

1. `ReelTracker` allowed unindexed scroll evidence only while `now - transition <= 1500`. YouTube's delayed/missing caption updates could arrive after that window, leaving an otherwise valid transition permanently uncountable.
2. YouTube used the shared `ReelSession` caption/page pairing window (450 ms) and generic pager tokens. Native recycled-page notifications and asynchronous metadata do not have that guaranteed ordering or timing.
3. `ReelPager` treated adapter index differences as page direction/position. YouTube can reuse indices; these are not stable video positions and must not authorize or reject YouTube video identities.
4. A snapshot was paired with the latest pager state at worker completion, rather than the state when inspection began. A scroll arriving during that scan could associate an older tree with a newer transition.
5. An event inspection that ran while `busy` could be lost until the periodic fallback tick. Requests arriving during a scan now schedule a bounded follow-up.
6. Generic fingerprint selection could include unstable unrelated text; offscreen recycled nodes could report `isVisibleToUser` even when their bounds were outside the viewport.

These are confirmed code-level defects. No connected phone was available during this patch, so they are not a newly reproduced trace of the user's current YouTube version. Earlier project notes recorded full-screen `reel_recycler` events with all indices -1, scrollY 0, and scrollDeltaY 0.

## Implementation

- Dedicated main-thread `YouTubeShortsTracker` joins player-scroll evidence and separately settled video identity. Initial player entry requires settled identity too.
- Only full-size, vertical native player scroll/selection events authorize transitions. Comments, horizontal carousels, sliders and ordinary lists do not. API 34+ targeted-scroll events are also observed when their source satisfies the same native-player checks.
- `TYPE_WINDOW_CONTENT_CHANGED`, window changes and existing selected/scrolled events request bounded asynchronous tree snapshots. No event object or node is retained after processing; node recycling/cancellation remain intact.
- A scroll opens a pending transition without a metadata deadline. Matching metadata must remain stable for at least 80 ms across observations. Multiple callbacks and intermediate caption updates cannot consume the same transition twice.
- Every scan captures a generation before dispatch. If a scroll occurs during it, the old snapshot is rejected and a fresh event-requested scan follows.
- YouTube adapter indices and absolute scrollY are ignored for video position/direction. An explicit relative vertical delta supplies direction where available; otherwise direction remains unknown. The existing generic pager is unchanged for the other platforms.
- Recognition prefers an exposed Shorts/video URL, then known title/description nodes, then visible caption-region metadata. View resource IDs and `uniqueId` are not treated as video IDs. Captions/creator evidence is hashed using SHA-256 and retained only in bounded memory.
- A known negative direction rejects backward navigation. Previously seen identities are rejected across player exits and reopenings during the current service/day lifetime. Ads never increment the counter.
- Leaving the player resets positional state while preserving the seen set. Pauses, selection changes, disconnects, stale service tickets and data resets retain their existing lifecycle safeguards. History deletion also clears YouTube's transient seen set.
- Existing 45 ms event coalescing, 90 ms inspection floor, bounded tree traversal, and periodic fallback intervals remain unchanged. There is no swipe injection, gesture interception, touch exploration, additional accessibility capability, or higher polling frequency.

## Diagnostics

Logging is off unless the Android log tag is explicitly enabled:

```sh
adb shell setprop log.tag.AntiReelShorts DEBUG
python3 tools/capture_youtube_diagnostics.py --seconds 240 --output /tmp/antireel-shorts-test.json
adb shell setprop log.tag.AntiReelShorts INFO
```

The capture is passive: manually open and scroll YouTube Shorts. It does not open apps, swipe, change goals, clear history, grant permissions, or reset logs. It captures only this tag after its own start marker. The report contains numeric event fields, transition generations, reason codes and count decisions; no screen labels, captions, creators, video URLs or identity hashes. Check `count_increments` against manually annotated new Shorts; event totals are not a video-count ground truth.

## Validation completed

- 154 Kotlin unit tests pass, including 21 new YouTube tests. A synthetic 30-Short sequence counts exactly 30 and repeated callbacks count zero additional videos. Tests cover delayed metadata beyond the old deadline, event bursts, both metadata/scroll arrival orders, stale snapshots, backward transitions, repeated identities, ads, player exits/reentries, edited captions, missing identity and URL/metadata selection.
- Existing multi-platform recognition, counting, lifecycle and limit tests continue to pass.
- Debug APK and instrumentation APK compile. Added a native accessibility snapshot regression for offscreen recycled nodes; it is compiled but not device-executed in this session.
- 10 Python installer/packaging/diagnostic-summary tests pass.
- Release APK/AAB and signature/integrity validation are supplied in the distribution verification JSON. Lint results are reported in the release build; warnings are not a claim of a warning-free project.

## Physical test still required

ADB `devices -l` and mDNS discovery returned no devices. Therefore this version was not installed on a phone and no 30-consecutive-Short physical test was performed. Do not describe the synthetic sequence as a successful device test.

On a connected, unlocked phone with user-approved Accessibility enabled, manually compare visible new Shorts against count increments across at least 30 consecutive organic videos. Include fast and slow transitions, pauses, canceled drags, ads, backward revisits, repeated videos, exits/reopens, and entry from Home/Search/Subscriptions. Check that the other selected platforms retain their behavior. Respect the existing daily goal: do not raise it, consume the one-time restart, clear history, or bypass the limit to complete testing. If fewer than 30 reels remain, arrange an explicit test profile or another day with the user.

## Accessibility limits

YouTube controls which nodes/events it exposes. An exposed Shorts URL is a strong identity, but it is not available on every player version. Metadata is a conservative fallback, not a guaranteed globally unique video ID. Different videos with identical metadata may be indistinguishable; caption expansion/edits overlapping a scroll can also be ambiguous. Videos skipped before a distinct, settled accessible frame exists cannot be counted exactly. A scroll event alone never increments a count.

Unknown scroll direction cannot distinguish forward navigation from backward navigation to an unseen video. Known backward deltas and seen identities are rejected, but unknown-direction unseen backward content remains ambiguous. Unset -1 deltas are normalized using the existing Android-sentinel rule. Deduplication is in-memory, bounded to 2048 identities, and does not survive service/process recreation; old evicted videos or videos revisited after recreation may count again. The tracker supports an independently indexed fallback internally, but the YouTube service deliberately does not pass recycled adapter indices to it.

Exact, universal detection is not promised where YouTube exposes insufficient information. This patch should be verified against the installed YouTube/Android version before declaring the reported issue resolved on the user's phone.

Android reference: https://developer.android.com/reference/android/view/accessibility/AccessibilityEvent
