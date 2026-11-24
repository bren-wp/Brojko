package com.brendigo.brojko;

import android.content.Context;
import android.content.res.Resources;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Locale;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public final class LocaleResourcesTest {
    @Test public void explicitLocalesResolveWithoutChangingApplicationConfiguration() {
        Context context=ApplicationProvider.getApplicationContext();
        String before=context.getResources().getConfiguration().getLocales().toLanguageTags();
        Resources en=LocaleManager.resourcesFor(context,Locale.ENGLISH);
        Resources hr=LocaleManager.resourcesFor(context,LocaleManager.legacyUiLocale());
        assertEquals("Brojko — new daily challenge",en.getString(R.string.reminder_title));
        assertEquals("Brojko — novi dnevni izazov",hr.getString(R.string.reminder_title));
        assertEquals("Language",en.getString(R.string.settings_language));
        assertEquals("Jezik",hr.getString(R.string.settings_language));
        assertEquals("Sound on",en.getString(R.string.settings_switch_on,"Sound"));
        assertEquals("Zvuk uključeno",hr.getString(R.string.settings_switch_on,"Zvuk"));
        assertEquals("crown",StatMetric.BEST.symbol);
        assertEquals("gamepad",StatMetric.TOTAL.symbol);
        assertEquals("Best",en.getString(StatMetric.BEST.topResource));
        assertEquals("Najbolji",hr.getString(StatMetric.BEST.topResource));
        assertEquals("score",en.getString(StatMetric.BEST.bottomResource));
        assertEquals("rezultat",hr.getString(StatMetric.BEST.bottomResource));
        Resources de=LocaleManager.resourcesFor(context,Locale.GERMAN);
        Resources fr=LocaleManager.resourcesFor(context,Locale.FRENCH);
        assertEquals("Sprache",de.getString(R.string.settings_language));
        assertEquals("Langue",fr.getString(R.string.settings_language));
        assertEquals("256 erreichen",de.getString(DailyGoal.TILE.titleResource));
        assertEquals("Fusionner 20 tuiles",fr.getString(MissionKind.MERGE.titleResource));
        assertEquals(before,context.getResources().getConfiguration().getLocales().toLanguageTags());
    }
}
