package com.emmaguy.todayilearned;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Utils {
    private static Set<String> DefaultSubReddits = new HashSet<String>(Arrays.asList("todayilearned", "Android", "crazyideas", "worldnews", "britishproblems", "showerthoughts"));
    private static Set<String> DefaultSelectedSubReddits = new HashSet<String>(Arrays.asList("todayilearned"));

    private static SharedPreferences getSharedPreferences(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c.getApplicationContext());
    }

    public static List<String> allSubReddits(Context c) {
        Set<String> subReddits = getSharedPreferences(c).getStringSet(SettingsActivity.PREFS_SUBREDDITS, DefaultSubReddits);

        ArrayList<String> sortedSubreddits = new ArrayList(subReddits);
        Collections.sort(sortedSubreddits);

        return sortedSubreddits;
    }

    public static List<String> selectedSubReddits(Context c) {
        Set<String> subReddits = getSharedPreferences(c).getStringSet(SettingsActivity.PREFS_SELECTED_SUBREDDITS, DefaultSelectedSubReddits);

        return new ArrayList(subReddits);
    }

    public static void saveSubreddits(Context c, List<String> srs) {
        getSharedPreferences(c).edit().putStringSet(SettingsActivity.PREFS_SUBREDDITS, new HashSet<String>(srs)).apply();
    }

    public static void saveSelectedSubreddits(Context c, List<String> selectedSubreddits) {
        getSharedPreferences(c).edit().putStringSet(SettingsActivity.PREFS_SELECTED_SUBREDDITS, new HashSet<String>(selectedSubreddits)).apply();
    }

    public static void addSubreddit(Context c, String subreddit) {
        List<String> subreddits = allSubReddits(c);
        subreddits.add(subreddit);

        saveSubreddits(c, subreddits);
    }
}
