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

    @Test public void explicitDisplayLocaleDoesNotChangeDateIdentity(){
        Date date=new Date(1705320000000L);
        Locale en=Locale.US;
        Locale hr=Locale.forLanguageTag("hr-HR");
        assertEquals(java.text.DateFormat.getDateInstance(java.text.DateFormat.LONG,en)
            .format(date),GameCalendar.dailyLongDate(date,en));
        assertEquals(new SimpleDateFormat("d. MMMM yyyy.",hr).format(date),
            GameCalendar.dailyLongDate(date,hr));
        assertEquals(java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM,en)
            .format(date),GameCalendar.historyDate(date,en));
        assertEquals(java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT,en)
            .format(date),GameCalendar.compactTime(date,en));
        assertEquals(new SimpleDateFormat("HH:mm",hr).format(date),
            GameCalendar.compactTime(date,hr));
        assertNotEquals(GameCalendar.dailyLongDate(date,en),GameCalendar.dailyLongDate(date,hr));
        assertEquals(new SimpleDateFormat("yyyy-MM-dd",Locale.ROOT).format(new Date()),
            GameCalendar.today());
    }

    @Test public void periodCountdownNeverDisplaysNegativeAmounts(){
        String daily=GameCalendar.timeRemainingInPeriod(false);
        String weekly=GameCalendar.timeRemainingInPeriod(true);
        assertTrue(daily.matches("\\d+h \\d+min"));
        assertTrue(weekly.matches("(\\d+h|\\d+d \\d+h)"));
    }
}
