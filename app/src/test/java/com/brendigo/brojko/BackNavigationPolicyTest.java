package com.brendigo.brojko;

import org.junit.Test;
import static org.junit.Assert.*;

/** Navigation regression suite: same Back destinations as original Activity. */
public final class BackNavigationPolicyTest {
    @Test public void gameAndPauseAreReversible(){
        assertEquals("pause",BackNavigationPolicy.destination("game",true));
        assertEquals("game",BackNavigationPolicy.destination("pause",true));
    }

    @Test public void detailsAndSettingsReturnToTheCorrectParent(){
        assertEquals("stats",BackNavigationPolicy.destination("historyDetail",false));
        for(String page:new String[]{"privacy","about","language","backup","help"})
            assertEquals(page, "settings",BackNavigationPolicy.destination(page,true));
    }

    @Test public void confirmationRestoresAnExistingSessionOrHome(){
        for(String page:new String[]{"confirm","confirmEnd"}){
            assertEquals("game",BackNavigationPolicy.destination(page,true));
            assertEquals("home",BackNavigationPolicy.destination(page,false));
        }
    }

    @Test public void finishedStatesAndSplashReturnToHome(){
        for(String page:new String[]{
            "win","over","dailyWin","dailyFail","splash","daily","missions",
            "stats","themes","settings","unknown",""}){
            assertEquals(page,"home",BackNavigationPolicy.destination(page,true));
        }
    }

    @Test public void homeFinishesActivityAndNullIsHandledSafely(){
        assertNull(BackNavigationPolicy.destination("home",true));
        assertNull(BackNavigationPolicy.destination("home",false));
        assertEquals("home",BackNavigationPolicy.destination(null,false));
    }
}
