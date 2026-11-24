package com.brendigo.brojko;

import android.content.Context;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public final class LanguagePickerTest {
    @Test public void supportedDefaultAndUnsupportedTags(){
        assertEquals(LocaleManager.Language.ENGLISH,LocaleManager.languageOrEnglish(""));
        assertEquals(LocaleManager.Language.ENGLISH,LocaleManager.languageOrEnglish("unsupported"));
        assertEquals(LocaleManager.Language.CROATIAN,LocaleManager.languageOrEnglish("hr-HR"));
        assertFalse(LocaleManager.usesCroatianArtwork(LocaleManager.Language.ENGLISH.locale()));
        assertTrue(LocaleManager.usesCroatianArtwork(LocaleManager.Language.CROATIAN.locale()));
    }

    @Test public void localePersistsAndPreservesSessionOnRecreation(){
        Context context=ApplicationProvider.getApplicationContext();
        try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)){
            final String[] session=new String[1];
            scenario.onActivity(activity->{
                activity.game=new TileGame("classic","",734L);
                activity.persist();
                activity.navigate("language");
                session[0]=activity.game.toJson().toString();
                assertEquals(LocaleManager.Language.CROATIAN,activity.uiLanguage);
                activity.selectLanguage(LocaleManager.Language.ENGLISH);
            });
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            // Explicit recreation is also a process-lifecycle regression,
            // independent of the framework's asynchronous locale relaunch.
            scenario.recreate();
            scenario.onActivity(activity->{
                assertEquals(LocaleManager.Language.ENGLISH,activity.uiLanguage);
                assertEquals("Language",activity.screen.uiText(R.string.settings_language));
                assertEquals("language",activity.page);
                assertEquals(session[0],activity.game.toJson().toString());
                assertEquals(LocaleManager.Language.ENGLISH,LocaleManager.selected(context));
            });
        }finally{
            InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{
                assertTrue(LocaleManager.select(context,LocaleManager.Language.CROATIAN));
            });
        }
    }
}
