package com.brendigo.brojko;

import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Persists completed games before the Activity may replace an active session.
 * No Android UI, navigation, network or time dependency lives in this class.
 */
final class LocalHistoryRepository {
    private static final int MAX_HISTORY_ENTRIES=20;
    private static final int MAX_INSPECTED_LEGACY_ENTRIES=200;

    private LocalHistoryRepository() {}

    static boolean archive(SharedPreferences prefs,TileGame game,String day,String time){
        if(game==null||game.archived||game.moves==0)return true;
        try{
            JSONObject row=new JSONObject();
            row.put("score",game.score).put("moves",game.moves)
                .put("peak",game.maxTile()).put("mode",game.mode)
                .put("day",day).put("time",time)
                .put("won",game.reached2048);

            // A corrupt legacy history must never prevent saving this game.
            JSONArray previous;
            try{previous=new JSONArray(prefs.getString("history","[]"));}
            catch(Exception invalidHistory){previous=new JSONArray();}
            JSONArray recent=prepend(row,previous);

            // commit(), not apply(): never replace the current game before the
            // newly archived row is durably persisted.
            if(!prefs.edit().putString("history",recent.toString()).commit())
                return false;
            game.archived=true;
            return true;
        }catch(Exception error){
            return false;
        }
    }

    /** Preserves the old ordering, 20-row cap and corrupted-row tolerance. */
    static JSONArray prepend(JSONObject latest,JSONArray previous){
        JSONArray result=new JSONArray();
        result.put(latest);
        for(int i=0;i<Math.min(MAX_INSPECTED_LEGACY_ENTRIES,previous.length())&&
            result.length()<MAX_HISTORY_ENTRIES;i++){
            JSONObject prior=previous.optJSONObject(i);
            if(prior!=null)result.put(prior);
        }
        return result;
    }
}
