package com.emmaguy.todayilearned.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
    private static Set<String> DefaultSubreddits = new HashSet<String>(Arrays.asList("todayilearned", "Android", "crazyideas", "worldnews", "britishproblems", "showerthoughts", "pics", "AskReddit"));

    public SubredditPreference(Context context) {
        super(context, null);
    }

    public SubredditPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onClick() {
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
        Set<String> subreddits = getSharedPreferences().getStringSet(key, Constants.sDefaultSelectedSubreddits);
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

    public void saveSubreddits(List<String> srs) {
        getSharedPreferences().edit().putStringSet(getKey(), new HashSet<String>(srs)).apply();
    }

    public void saveSelectedSubreddits(List<String> selectedSubreddits) {
        getSharedPreferences().edit().putStringSet(getSelectedSubredditsKey(), new HashSet<String>(selectedSubreddits)).apply();
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

        new AlertDialog.Builder(getContext(), R.style.AppCompatAlertDialogStyle)
                .setTitle(getContext().getResources().getString(R.string.select_subreddits))
                .setMultiChoiceItems(savedSubreddits.toArray(new String[savedSubreddits.size()]), selected, new DialogInterface.OnMultiChoiceClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        selected[i] = b;
                        ((AlertDialog) dialogInterface).getListView().setItemChecked(i, b);
                    }
                })
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
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
                    }
                })
                .setNeutralButton(R.string.add_subreddit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        showAddSubredditDialog();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show();
    }

    private void showAddSubredditDialog() {
        final View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_subreddit, null, false);

        new AlertDialog.Builder(getContext())
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final EditText input = (EditText) view.findViewById(R.id.subreddit_edittext);

                        String subreddit = input.getText().toString().trim();
                        if (subreddit.length() > 0) {
                            addSubreddit(subreddit);
                        }
                        showSelectSubredditsDialog();
                    }
                })
                .setTitle(R.string.add_subreddit)
                .setMessage(R.string.enter_subreddit_name)
                .setView(view)
                .create()
                .show();
    }
}
