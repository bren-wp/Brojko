package com.brendigo.brojko;

import org.json.JSONArray;
import org.json.JSONObject;

/** Shared history metric lookup, independent of Canvas layout and Activity state. */
final class StatisticsChartData {
    private StatisticsChartData() {}

    static int value(JSONObject row, int metric) {
        if (row == null) return 0;
        return Math.max(0, row.optInt(metric == 0 ? "score" : "peak", 0));
    }

    static int maximum(JSONArray rows, int count, int metric) {
        int maximum = 0;
        for (int i = 0; i < count; i++)
            maximum = Math.max(maximum, value(rows.optJSONObject(i), metric));
        return maximum;
    }
}
