package com.brendigo.brojko;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Restore an opted-in reminder after device reboot. */
public final class AppReminderBoot extends BroadcastReceiver {
    @Override public void onReceive(Context context,Intent intent){
        if(!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()))return;
        if(context.getSharedPreferences("brojko",Context.MODE_PRIVATE)
            .getBoolean("notifications",false))
            AppReminder.configure(context,true);
    }
}
