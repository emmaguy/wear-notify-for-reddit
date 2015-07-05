package com.emmaguy.todayilearned.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface ActionStorage {
    ArrayList<Integer> getSelectedActionIds();
    List<Integer> getActionIds();
    void save(ArrayList<Action> actions, HashMap<Integer, Action> selectedActions);
}
