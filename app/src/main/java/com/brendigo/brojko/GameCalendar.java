package com.brendigo.brojko;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Extracted Croatian day, week, history and countdown logic, unchanged. */
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

    static String compactTime(){return new SimpleDateFormat("HH:mm",new Locale("hr","HR")).format(new Date());}

    static String historyDate(){return new SimpleDateFormat("d. MMM yyyy.",new Locale("hr","HR")).format(new Date());}

    static String dailyLongDate(){
        return new SimpleDateFormat("d. MMMM yyyy.",new Locale("hr","HR")).format(new Date());
    }

    static String timeRemainingInPeriod(boolean week){
        Calendar now=Calendar.getInstance();
        Calendar end=(Calendar)now.clone();
        if(week){
            int daysUntilMonday=(Calendar.MONDAY-end.get(Calendar.DAY_OF_WEEK)+7)%7;
            end.add(Calendar.DAY_OF_YEAR,daysUntilMonday==0?7:daysUntilMonday);
        }else end.add(Calendar.DAY_OF_YEAR,1);
        end.set(Calendar.HOUR_OF_DAY,0);end.set(Calendar.MINUTE,0);
        end.set(Calendar.SECOND,0);end.set(Calendar.MILLISECOND,0);
        long remaining=Math.max(0,
            (end.getTimeInMillis()-now.getTimeInMillis())/60000);
        return week?(remaining<1440?(remaining/60)+"h":
            (remaining/1440)+"d "+((remaining%1440)/60)+"h"):
            (remaining/60)+"h "+(remaining%60)+"min";
    }
}
