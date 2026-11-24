package com.brendigo.brojko;

/** Stable statistical card identities, independent of translated labels. */
enum StatMetric {
    BEST("crown",R.string.stats_best_top,R.string.stats_best_bottom),
    TOTAL("gamepad",R.string.stats_total_top,R.string.stats_total_bottom),
    PEAK("grid",R.string.stats_peak_top,R.string.stats_peak_bottom),
    AVERAGE("star",R.string.stats_average_top,R.string.stats_average_bottom),
    STREAK("flame",R.string.stats_streak_top,R.string.stats_streak_bottom);

    final String symbol;
    final int topResource;
    final int bottomResource;

    StatMetric(String symbol,int topResource,int bottomResource){
        this.symbol=symbol;
        this.topResource=topResource;
        this.bottomResource=bottomResource;
    }
}
