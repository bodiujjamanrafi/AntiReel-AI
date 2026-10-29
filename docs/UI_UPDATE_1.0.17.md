# AntiReel AI 1.0.17 — shorter setup and grouped Settings

## Changes

- Removed eight requested questionnaire prompts. Retained name, platform selection, daily reel allowance and confirmation. Language and theme stages remain. No after-answer reaction speech is introduced.
- Stable answer IDs remain 0, 2, 8, 11; drafts on removed stages resume at the next retained question. Required-answer validation and the review list only use retained questions. Stored old answer fields are preserved, without displaying or requiring them.
- Existing daily time allowance is preserved; new reel-only profiles use no time cap. The time cap remains editable in Settings. Existing reminder style is retained rather than being overwritten from a removed question.
- Review answers have separate subtle 12dp outlined rectangular cards, clipped press feedback, clear question/answer hierarchy and Edit actions.
- Settings index groups all existing destinations into Personalization, Goals & wellbeing, Platforms & tracking, Privacy & security, and Support & app. Manage platforms retains its original destination. Help and App info are retained in the final group.
- Theme-aware neutral rounded cards, small headings, 60dp minimum touch rows, inset dividers and pink icons. Labels wrap at narrow widths or large font scales. Press feedback is a local color change, with no layout animation or shadows. Settings detail entry fades lightly over 120ms, respecting Reduced motion.
- Header and bottom navigation source are unchanged. Existing Settings detail actions and preference keys remain intact.
- Quick Access retains all four actions and routes, using neutral surfaces, understated outlines and simpler 14dp corners instead of tinted frames.
- Only the two native Miso hand meshes are removed. Head, ears, body, feet, accessory, expressions, speech and reactions remain unchanged. Shared body caching remains in place.

## Validation

131 unit tests and eight Python packaging/installer safety tests pass. Debug and instrumentation APKs compile. Release APK/AAB signature, integrity and all 22 packaging checks pass. Lint reports 0 errors and 82 warnings. No device installation, screenshot review, frame-time measurements or live reel-watching tests are performed for this update. Keep the user's 20-reel goal and history intact.

The development APK is the same-signing-key update for the previously connected development installation. The production release APK and AAB use the existing production certificate. Do not uninstall or clear app data to resolve a signer conflict.
