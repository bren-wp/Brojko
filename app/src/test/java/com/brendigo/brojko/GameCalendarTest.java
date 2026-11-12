package com.brendigo.brojko;

import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.Test;
import static org.junit.Assert.*;

/** Calendar extraction regression. Do not depend on the device's local hour. */
public final class GameCalendarTest {
    @Test public void localeAndDateFormatsStayCompatible(){
        assertEquals(new SimpleDateFormat("yyyy-MM-dd",Locale.ROOT)
            .format(new Date()),GameCalendar.today());
        assertTrue(GameCalendar.compactTime().matches("[0-2][0-9]:[0-5][0-9]"));
        assertTrue(GameCalendar.historyDate().endsWith("."));
        assertTrue(GameCalendar.dailyLongDate().endsWith("."));
        assertTrue(GameCalendar.currentWeekStart().matches("\\d{4}-\\d{2}-\\d{2}"));
        assertTrue(GameCalendar.yesterday().matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test public void periodCountdownNeverDisplaysNegativeAmounts(){
        String daily=GameCalendar.timeRemainingInPeriod(false);
        String weekly=GameCalendar.timeRemainingInPeriod(true);
        assertTrue(daily.matches("\\d+h \\d+min"));
        assertTrue(weekly.matches("(\\d+h|\\d+d \\d+h)"));
    }
}
