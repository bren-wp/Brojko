package com.brendigo.brojko;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public final class LocaleSecurityTest {
    @Test public void searchNormalizesDiacriticsWithoutChangingStoredNames(){
        assertEquals("francais",LanguageSearchDialog.normalized("  Français "));
        assertEquals("espanol",LanguageSearchDialog.normalized("ESPAÑOL"));
    }
    @Test public void importedHistoryCannotInjectBidiOverridesOrControls(){
        assertEquals("2026-11-24",GameCalendar.safeHistoryLabel("2026-\u202e11-\n24"));
        assertEquals("",GameCalendar.safeHistoryLabel(null));
        assertEquals(64,GameCalendar.safeHistoryLabel(new String(new char[100]).replace('\0','a')).length());
    }
}
