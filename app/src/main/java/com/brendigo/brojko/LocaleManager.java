package com.brendigo.brojko;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import java.util.Locale;

/** Shared resource access during the staged Canvas localization migration. */
final class LocaleManager {
    private LocaleManager() {}

    // Preserve the existing Croatian UI until a complete English UI is ready.
    // Never expose partially translated languages as selectable.
    static Locale legacyUiLocale() { return Locale.forLanguageTag("hr-HR"); }

    static boolean usesCroatianArtwork(Locale locale) {
        return "hr".equals(locale.getLanguage());
    }

    static Resources resourcesFor(Context context, Locale locale) {
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(locale);
        return context.createConfigurationContext(configuration).getResources();
    }
}
