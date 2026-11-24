package com.brendigo.brojko;

import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Locale-independent date identities and locale-aware display formatting. */
final class GameCalendar {
    private GameCalendar(){}
    static String today(){return new SimpleDateFormat("yyyy-MM-dd",Locale.ROOT).format(new Date());}

    static String currentWeekStart(){
        Calendar c=Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR,-((c.get(Calendar.DAY_OF_WEEK)+5)%7));
        return new SimpleDateFormat("yyyy-MM-dd",Locale.ROOT).format(c.getTime());
    }

    static String yesterday(){
        Calendar c=Calendar.getInstance();c.add(Calendar.DAY_OF_YEAR,-1);
        return new SimpleDateFormat("yyyy-MM-dd",Locale.ROOT).format(c.getTime());
    }

    // Compatibility wrappers preserve the current Croatian renderer geometry.
    static String compactTime(){return compactTime(new Date(),LocaleManager.legacyUiLocale());}
    static String historyDate(){return historyDate(new Date(),LocaleManager.legacyUiLocale());}
    static String dailyLongDate(){return dailyLongDate(new Date(),LocaleManager.legacyUiLocale());}

    static String compactTime(Date date,Locale locale){
        return "hr".equals(locale.getLanguage())
            ? new SimpleDateFormat("HH:mm",locale).format(date)
            : DateFormat.getTimeInstance(DateFormat.SHORT,locale).format(date);
    }

    static String historyDate(Date date,Locale locale){
        return "hr".equals(locale.getLanguage())
            ? new SimpleDateFormat("d. MMM yyyy.",locale).format(date)
            : DateFormat.getDateInstance(DateFormat.MEDIUM,locale).format(date);
    }

    static String dailyLongDate(Date date,Locale locale){
        return "hr".equals(locale.getLanguage())
            ? new SimpleDateFormat("d. MMMM yyyy.",locale).format(date)
            : DateFormat.getDateInstance(DateFormat.LONG,locale).format(date);
    }

    static String timeRemainingInPeriod(boolean week){
        long remaining=remainingMinutes(week);
        return week?(remaining<1440?(remaining/60)+"h":
            (remaining/1440)+"d "+((remaining%1440)/60)+"h"):
            (remaining/60)+"h "+(remaining%60)+"min";
    }

    private static Date parseExact(String value,String pattern,Locale locale){
        SimpleDateFormat format=new SimpleDateFormat(pattern,locale);
        format.setLenient(false);
        ParsePosition position=new ParsePosition(0);
        Date result=format.parse(value,position);
        return result!=null&&position.getIndex()==value.length()?result:null;
    }

    /** Render new invariant dates and old v1 Croatian display strings without rewriting backups. */
    // Do not render bidi overrides or control characters from imported legacy data.
    static String safeHistoryLabel(String value){
        if(value==null)return "";
        StringBuilder safe=new StringBuilder(Math.min(64,value.length()));
        for(int i=0;i<value.length()&&safe.length()<64;){
            int cp=value.codePointAt(i);i+=Character.charCount(cp);
            if(Character.isISOControl(cp)||Character.getType(cp)==Character.FORMAT)continue;
            if(safe.length()+Character.charCount(cp)>64)break;
            safe.appendCodePoint(cp);
        }
        return safe.toString();
    }

    static String displayHistoryDate(String value,Locale locale){
        value=safeHistoryLabel(value);
        if(value==null||value.isEmpty())return "";
        Date parsed=parseExact(value,"yyyy-MM-dd",Locale.ROOT);
        if(parsed!=null)return historyDate(parsed,locale);
        if("hr".equals(locale.getLanguage()))return value;
        parsed=parseExact(value,"d. MMM yyyy.",LocaleManager.legacyUiLocale());
        if(parsed==null)parsed=parseExact(value,"d.M.yyyy.",Locale.ROOT);
        if(parsed==null)parsed=parseExact(value,"d. MMMM yyyy.",LocaleManager.legacyUiLocale());
        return parsed==null?value:historyDate(parsed,locale);
    }

    static String displayHistoryTime(String value,Locale locale){
        value=safeHistoryLabel(value);
        if(value==null||value.isEmpty())return "";
        if("hr".equals(locale.getLanguage()))return value;
        Date parsed=parseExact(value,"HH:mm",Locale.ROOT);
        return parsed==null?value:compactTime(parsed,locale);
    }

    static long remainingMinutes(boolean week){
        Calendar now=Calendar.getInstance();
        Calendar end=(Calendar)now.clone();
        if(week){
            int daysUntilMonday=(Calendar.MONDAY-end.get(Calendar.DAY_OF_WEEK)+7)%7;
            end.add(Calendar.DAY_OF_YEAR,daysUntilMonday==0?7:daysUntilMonday);
        }else end.add(Calendar.DAY_OF_YEAR,1);
        end.set(Calendar.HOUR_OF_DAY,0);end.set(Calendar.MINUTE,0);
        end.set(Calendar.SECOND,0);end.set(Calendar.MILLISECOND,0);
        return Math.max(0,(end.getTimeInMillis()-now.getTimeInMillis())/60000);
    }
}
