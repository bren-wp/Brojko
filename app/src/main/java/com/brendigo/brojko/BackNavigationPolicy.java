package com.brendigo.brojko;

/**
 * Pure Back navigation decision tree. No Activity, renderer or persistent state
 * dependencies: a destination can be checked on the JVM without Android UI.
 *
 * Null means the Android Activity should finish (Home only).
 */
final class BackNavigationPolicy {
    private BackNavigationPolicy() {}

    static String destination(String page,boolean hasGame){
        if("game".equals(page))return "pause";
        if("pause".equals(page))return "game";
        if("historyDetail".equals(page))return "stats";
        if("privacy".equals(page)||"about".equals(page)||
           "language".equals(page)||"backup".equals(page)||
           "help".equals(page))return "settings";
        if("confirm".equals(page)||"confirmEnd".equals(page))
            return hasGame?"game":"home";
        if("home".equals(page))return null;
        return "home";
    }
}
