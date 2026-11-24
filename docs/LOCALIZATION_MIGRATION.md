# Localization migration

This branch is a staged resource migration, not a release of a multilingual UI.
The active Canvas locale remains Croatian until English coverage and behavior
are verified end-to-end. Existing owner reference art includes Croatian glyph
contours that require language-aware fallback rendering before enabling English.

Migrated areas: reminders, Settings and language page, Help, Backup, About,
Privacy, game result and confirmation dialogs, Home, selected accessibility
announcements and activity messages. Internal game IDs, backup schema, app ID,
signing and date keys remain unchanged.

Remaining: gameplay HUD, missions, stats, history, themes, branded headline
contours, all dynamic plural and number formatting, persisted language choice,
Android per-app locale integration, RTL and complex-script text shaping,
28 additional complete translations and localized store screenshots. The
English Fastlane listing is a draft for use after the English UI is complete.

The owner visual QA must still compare the Croatian render on the same viewport
and verify actual screenshots for each enabled locale. Do not claim 1:1 or
complete translations based solely on resource-key parity.
