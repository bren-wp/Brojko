package com.brendigo.brojko;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public final class StatisticsChartDataTest {
    @Test public void selectsScoreOrTile() throws Exception {
        JSONObject row = new JSONObject().put("score", 3000).put("peak", 256);
        assertEquals(3000, StatisticsChartData.value(row, 0));
        assertEquals(256, StatisticsChartData.value(row, 1));
    }
    @Test public void nullAndNegativeValuesRemainZero() throws Exception {
        assertEquals(0, StatisticsChartData.value(null, 0));
        JSONObject row = new JSONObject().put("score", -1).put("peak", -4);
        assertEquals(0, StatisticsChartData.value(row, 0));
        assertEquals(0, StatisticsChartData.value(row, 1));
    }
    @Test public void malformedHistoryRowsDoNotCreateValues() {
        JSONArray rows = new JSONArray().put("invalid").put(JSONObject.NULL);
        assertEquals(0, StatisticsChartData.maximum(rows, 2, 0));
    }
    @Test public void maximumRespectsRequestedWindowAndMetric() throws Exception {
        JSONArray rows = new JSONArray()
            .put(new JSONObject().put("score", 5).put("peak", 64))
            .put(new JSONObject().put("score", 12).put("peak", 8))
            .put(new JSONObject().put("score", 999).put("peak", 1024));
        assertEquals(12, StatisticsChartData.maximum(rows, 2, 0));
        assertEquals(64, StatisticsChartData.maximum(rows, 2, 1));
        assertEquals(0, StatisticsChartData.maximum(rows, 0, 0));
    }
}
