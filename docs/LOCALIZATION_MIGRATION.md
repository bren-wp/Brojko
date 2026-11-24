# Localization migration

English is the explicit product default. English, Croatian, German and French have
complete string-key inventories for the current migrated UI and are offered
in the functional Canvas language picker and Android 13+ app language settings.
Android 24–32 uses a separate private language preference. Android 13+ uses the
framework LocaleManager, with migration of older explicit preferences.

The existing Croatian reference suite uses a test-only runner selecting Croatian.
Production does not read test arguments. Croatian fixed artwork is used only
for Croatian; other languages use localized Canvas text. New history dates are
stored as ISO values; old v1 dates are displayed without rewriting backup data.

Static inventory parity is not native visual QA. Each language still requires
real-device review, layout/accessibility and lifecycle testing. Remaining 26
languages and full Arabic/Hebrew RTL mirroring and complex-script validation
are not advertised yet. English Fastlane copy is a draft; no Play Console
publication has been performed.
