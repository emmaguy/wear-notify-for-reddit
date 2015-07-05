package com.emmaguy.todayilearned.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.text.TextUtils;

import com.emmaguy.todayilearned.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by emma on 05/07/15.
 */
public class WearableActionStorage implements ActionStorage {
    private static final String NONE_SELECTED_VALUE = "-1";

    private final SharedPreferences mSharedPreferences;
    private final WearableActions mWearableActions;
    private final Resources mResources;
    private final Context mContext;

    @Inject
    public WearableActionStorage(WearableActions wearableActions, SharedPreferences sharedPreferences, Resources resources, Context context) {
        mWearableActions = wearableActions;
        mSharedPreferences = sharedPreferences;
        mResources = resources;
        mContext = context;
    }

    @Override public ArrayList<Integer> getSelectedActionIds() {
        String commaSeparatedActions = mSharedPreferences.getString(mContext.getString(R.string.prefs_key_actions_order), "");
        ArrayList<Integer> selectedActions = parseActionsFromString(commaSeparatedActions);

        if (selectedActions == null) {
            selectedActions = toActionIdsList(mWearableActions.getAllActions(), true);
        }

        return selectedActions;
    }

    @Override public List<Integer> getActionIds() {
        String commaSeparatedOrderedActions = mSharedPreferences.getString(mResources.getString(R.string.prefs_key_actions_order_ordered), "");
        List<Integer> actions = parseActionsFromString(commaSeparatedOrderedActions);

        final LinkedHashMap<Integer, Action> allActions = mWearableActions.getAllActions();
        if (actions == null) {
            actions = toActionIdsList(allActions, false);
        }

        if (actions.size() != allActions.size()) {
            // Find the missing action(s) and add them
            for (Integer action : allActions.keySet()) {
                if (!actions.contains(action)) {
                    actions.add(allActions.get(action).getId());
                }
            }
        }
        return actions;
    }

    @Override public void save(ArrayList<Action> actions, HashMap<Integer, Action> selectedActions) {
        mSharedPreferences
                .edit()
                .putString(mContext.getString(R.string.prefs_key_actions_order_ordered), TextUtils.join(",", getOrderedActionIds(actions)))
                .putString(mContext.getString(R.string.prefs_key_actions_order), getSelectedOrderedActions(actions, selectedActions))
                .apply();
    }

    private ArrayList<Integer> parseActionsFromString(String commaSeparatedActions) {
        ArrayList<Integer> actions = new ArrayList<>();

        if (commaSeparatedActions.equals(NONE_SELECTED_VALUE)) {
            return actions;
        }

        if (TextUtils.isEmpty(commaSeparatedActions)) {
            return null;
        }

        String[] split = commaSeparatedActions.split(",");
        for (String s : split) {
            actions.add(Integer.parseInt(s));
        }
        return actions;
    }

    private ArrayList<Integer> toActionIdsList(LinkedHashMap<Integer, Action> actions, boolean excludeDisabled) {
        ArrayList<Integer> arrayList = new ArrayList<>();

        for (Action action : actions.values()) {
            if (!excludeDisabled) {
                arrayList.add(action.getId());
            } else if (action.isEnabled()) {
                arrayList.add(action.getId());
            }
        }

        return arrayList;
    }

    private String getSelectedOrderedActions(ArrayList<Action> actions, HashMap<Integer, Action> selectedActions) {
        ArrayList<Integer> list = new ArrayList<>();

        for (Action a : actions) {
            if (selectedActions.containsKey(a.getId()) && a.isEnabled()) {
                list.add(a.getId());
            }
        }

        if (list.isEmpty()) {
            return WearableActionStorage.NONE_SELECTED_VALUE;
        }

        return TextUtils.join(",", list);
    }

    private ArrayList<Integer> getOrderedActionIds(ArrayList<Action> actions) {
        ArrayList<Integer> orderedActionIds = new ArrayList<>();
        for (Action a : actions) {
            orderedActionIds.add(a.getId());
        }
        return orderedActionIds;
    }
}
