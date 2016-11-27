package com.example.android.baloongame.utils;


import android.content.Context;
import android.content.SharedPreferences;

public class HighScoreHelper {

    private static final String PREFS_GLOBAL = "prefs_global";
    private static final String PREF_TOP_SCORE = "pref_top_score";

    private static SharedPreferences getPreferences(Context context){
        return context.getSharedPreferences(PREFS_GLOBAL, Context.MODE_PRIVATE);
    }

    public static boolean isTopScore(Context context, int newScore){
        return newScore>getTopScore(context);
    }
    public static int getTopScore(Context context){
        return getPreferences(context).getInt(PREF_TOP_SCORE, 0);
    }
    public static void setTopScore(Context context, int score){
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putInt(PREF_TOP_SCORE, score);
        editor.apply();
    }

}
