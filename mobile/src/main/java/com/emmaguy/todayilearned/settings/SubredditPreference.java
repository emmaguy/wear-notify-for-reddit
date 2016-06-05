package com.emmaguy.todayilearned.settings;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.emmaguy.todayilearned.R;
import com.emmaguy.todayilearned.sharedlib.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SubredditPreference extends Preference {
    private static Set<String> DefaultSubreddits = new HashSet<String>(Arrays.asList("todayilearned",
            "Android",
            "AndroidWear",
            "crazyideas",
            "worldnews",
            "britishproblems",
            "showerthoughts",
            "pics",
            "AskReddit"));

    public SubredditPreference(Context context) {
        super(context, null);
    }

    public SubredditPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override protected void onClick() {
        super.onClick();

        showSelectSubredditsDialog();
    }

    public String getSelectedSubredditsKey() {
        return getContext().getString(R.string.prefs_key_selected_subreddits);
    }

    public List<String> getAllSubreddits() {
        Set<String> subreddits = getSharedPreferences().getStringSet(getKey(), DefaultSubreddits);

        ArrayList<String> sortedSubreddits = new ArrayList(subreddits);
        Collections.sort(sortedSubreddits);

        return sortedSubreddits;
    }

    public List<String> getAllSelectedSubreddits() {
        final String key = getSelectedSubredditsKey();
        Set<String> subreddits = getSharedPreferences().getStringSet(key,
                Constants.sDefaultSelectedSubreddits);
        return new ArrayList(subreddits);
    }

    /**
     * Adds the subreddit and selects it
     */
    public void addSubreddit(String subreddit) {
        updateSubreddits(subreddit);
        updateSelectedSubreddits(subreddit);
    }

    private void updateSubreddits(String subreddit) {
        List<String> subreddits = getAllSubreddits();
        subreddits.add(subreddit);
        saveSubreddits(subreddits);
    }

    private void updateSelectedSubreddits(String subreddit) {
        List<String> selectedSubreddits = getAllSelectedSubreddits();
        selectedSubreddits.add(subreddit);
        saveSelectedSubreddits(selectedSubreddits);
    }

    public void saveSubreddits(List<String> subreddits) {
        final HashSet<String> values = new HashSet<>(subreddits);
        values.addAll(getAllSubreddits());
        getSharedPreferences().edit().putStringSet(getKey(), values).apply();
    }

    public void saveSelectedSubreddits(List<String> selectedSubreddits) {
        getSharedPreferences().edit()
                .putStringSet(getSelectedSubredditsKey(), new HashSet<>(selectedSubreddits))
                .apply();
    }

    private void showSelectSubredditsDialog() {
        final List<String> savedSubreddits = getAllSubreddits();
        final List<String> selectedSubreddits = getAllSelectedSubreddits();
        final boolean[] selected = new boolean[savedSubreddits.size()];

        int i = 0;
        for (String s : savedSubreddits) {
            selected[i] = selectedSubreddits.contains(s);

            i++;
        }

        new AlertDialog.Builder(getContext(),
                R.style.AppCompatAlertDialogStyle).setTitle(getContext().getResources()
                .getString(R.string.select_subreddits))
                .setMultiChoiceItems(savedSubreddits.toArray(new String[savedSubreddits.size()]),
                        selected,
                        (dialogInterface, i1, b) -> {
                            selected[i1] = b;
                            ((AlertDialog) dialogInterface).getListView().setItemChecked(i1, b);
                        })
                .setPositiveButton(R.string.save, (dialogInterface, i1) -> {
                    List<String> selectedSrs = new ArrayList<String>();

                    int index = 0;
                    for (boolean b : selected) {
                        if (b) {
                            selectedSrs.add(savedSubreddits.get(index));
                        }
                        index++;
                    }
                    saveSubreddits(savedSubreddits);
                    saveSelectedSubreddits(selectedSrs);
                })
                .setNeutralButton(R.string.add_subreddit, (dialogInterface, i1) -> {
                    showAddSubredditDialog();
                })
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show();
    }

    private void showAddSubredditDialog() {
        @SuppressLint("InflateParams") final View view = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_subreddit, null, false);

        new AlertDialog.Builder(getContext()).setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, (dialogInterface, i) -> {
                    final EditText input = (EditText) view.findViewById(R.id.subreddit_edittext);

                    String subreddit = input.getText().toString().replaceAll("\\s+", "");
                    if (subreddit.length() > 0) {
                        addSubreddit(subreddit);
                    }
                    showSelectSubredditsDialog();
                })
                .setTitle(R.string.add_subreddit)
                .setMessage(R.string.enter_subreddit_name)
                .setView(view)
                .create()
                .show();
    }
}
