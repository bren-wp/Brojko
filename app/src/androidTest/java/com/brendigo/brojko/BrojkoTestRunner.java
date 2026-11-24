package com.brendigo.brojko;

import androidx.test.runner.AndroidJUnitRunner;

/** The owner reference suite is deliberately Croatian, independent of device locale. */
public final class BrojkoTestRunner extends AndroidJUnitRunner {
    @Override public void onStart(){
        runOnMainSync(()->{
            if(!LocaleManager.select(getTargetContext(),LocaleManager.Language.CROATIAN))
                throw new AssertionError("Unable to set Croatian reference test locale");
        });
        super.onStart();
    }
}
