package com.emmaguy.todayilearned.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.preference.Preference;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.emmaguy.todayilearned.Logger;
import com.emmaguy.todayilearned.PocketUtil;
import com.emmaguy.todayilearned.R;
import com.emmaguy.todayilearned.Utils;
import com.emmaguy.todayilearned.sharedlib.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class DragReorderActionsPreference extends Preference implements CheckChangedListener {
    public static final String NONE_SELECTED_VALUE = "-1";
    private final HashMap<Integer, Action> mSelectedActions = new HashMap<>();
    private final ArrayList<Action> mActions = new ArrayList<>();

    private Context mContext;

    public DragReorderActionsPreference(Context context) {
        super(context, null);

        init(context);
    }

    public DragReorderActionsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    private void init(Context context) {
        mContext = context;
    }

    @Override
    protected void onClick() {
        super.onClick();

        Logger.sendEvent(mContext, Logger.LOG_EVENT_CUSTOMISE_ACTIONS, "");

        showDragReorderDialog();
    }

    private void initActions() {
        mActions.clear();
        mSelectedActions.clear();

        String commaSeparatedOrderedActions = getSharedPreferences().getString(getActionsOrderedKey(), "");
        String commaSeparatedActions = getSharedPreferences().getString(getKey(), "");

        List<Integer> actions = parseActionsFromString(commaSeparatedOrderedActions);
        List<Integer> selectedActions = parseActionsFromString(commaSeparatedActions);

        if (actions == null) {
            actions = toActionIdsList(getAllActions(), false);
        }

        if (selectedActions == null) {
            selectedActions = toActionIdsList(getAllActions(), true);
        }

        for (Integer integer : actions) {
            mActions.add(getAllActions().get(integer));
        }

        for (Integer integer : selectedActions) {
            mSelectedActions.put(integer, getAllActions().get(integer));
        }
    }

    private void showDragReorderDialog() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_action_preference_layout, null);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.actions_reorder_recyclerview);
        ReorderRecyclerView.ReorderAdapter adapter = new ReorderRecyclerView.ReorderAdapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View inflate = LayoutInflater.from(mContext).inflate(R.layout.row_actions_reorder, parent, false);
                return new ActionViewHolder(inflate, DragReorderActionsPreference.this);
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder h, int position) {
                Action action = mActions.get(position);
                ActionViewHolder holder = (ActionViewHolder) h;

                holder.mCheckBox.setEnabled(action.mEnabled);

                holder.mCheckBox.setChecked(mSelectedActions.containsKey(action.mId));
                holder.mCheckBox.setTag(action.mId);
                holder.mTextView.setText(action.getName());
            }

            @Override
            public int getItemCount() {
                return mActions.size();
            }

            @Override
            public long getItemId(int position) {
                return mActions.get(position).mId;
            }

            @Override
            public void swapElements(int fromIndex, int toIndex) {
                Action temp = mActions.get(fromIndex);
                mActions.set(fromIndex, mActions.get(toIndex));
                mActions.set(toIndex, temp);

                update();
            }
        };
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);

        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new InsetDecoration(mContext));
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        new AlertDialog.Builder(mContext).setView(view).create().show();
    }

    @Override
    public CharSequence getSummary() {
        initActions();

        ArrayList<String> actions = new ArrayList<>();

        for (Integer action : getOrderedActionIds()) {
            if (mSelectedActions.containsKey(action)) {
                actions.add(getAllActions().get(action).mName);
            }
        }

        if (actions.isEmpty()) {
            return mContext.getString(R.string.none);
        }

        return TextUtils.join(", ", actions);
    }

    private LinkedHashMap<Integer, Action> getAllActions() {
        return getAllActions(getSharedPreferences(), mContext);
    }

    private static LinkedHashMap<Integer, Action> getAllActions(SharedPreferences prefs, Context context) {
        boolean loggedIn = Utils.isLoggedIn(prefs, context);

        LinkedHashMap<Integer, Action> actions = new LinkedHashMap<>();
        addToAllActions(actions, new Action(Constants.ACTION_ORDER_VIEW_COMMENTS, context.getString(R.string.action_view_comments)));

        addToAllActions(actions, new Action(Constants.ACTION_ORDER_REPLY, context.getString(R.string.action_reply), loggedIn, context.getString(R.string.requires_login)));
        addToAllActions(actions, new Action(Constants.ACTION_ORDER_UPVOTE, context.getString(R.string.action_upvote), loggedIn, context.getString(R.string.requires_login)));
        addToAllActions(actions, new Action(Constants.ACTION_ORDER_DOWNVOTE, context.getString(R.string.action_downvote), loggedIn, context.getString(R.string.requires_login)));

        addToAllActions(actions, new Action(Constants.ACTION_ORDER_SAVE_TO_POCKET, context.getString(R.string.action_save_to_pocket), PocketUtil.isPocketInstalled(context), context.getString(R.string.requires_pocket_app_installed)));

        addToAllActions(actions, new Action(Constants.ACTION_ORDER_OPEN_ON_PHONE, context.getString(R.string.action_open_on_phone)));

        return actions;
    }

    private static void addToAllActions(LinkedHashMap<Integer, Action> actions, Action action) {
        actions.put(action.mId, action);
    }

    private static ArrayList<Integer> parseActionsFromString(String commaSeparatedActions) {
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

    @Override
    public void onCheckedChanged(Integer id, boolean isChecked) {
        if (isChecked) {
            Action action = getAllActions().get(id);
            mSelectedActions.put(id, action);
        } else {
            mSelectedActions.remove(id);
        }

        update();
    }

    private void update() {
        getSharedPreferences()
                .edit()
                .putString(getActionsOrderedKey(), TextUtils.join(",", getOrderedActionIds()))
                .putString(getKey(), getSelectedOrderedActions())
                .apply();
        setSummary(getSummary());
    }

    private String getActionsOrderedKey() {
        return getKey() + "_ordered";
    }

    private ArrayList<Integer> getOrderedActionIds() {
        ArrayList<Integer> actions = new ArrayList<>();
        for (Action a : mActions) {
            actions.add(a.mId);
        }
        return actions;
    }

    private String getSelectedOrderedActions() {
        ArrayList<Integer> actions = new ArrayList<>();

        for (Action a : mActions) {
            if (mSelectedActions.containsKey(a.mId) && a.mEnabled) {
                actions.add(a.mId);
            }
        }

        if (actions.isEmpty()) {
            return NONE_SELECTED_VALUE;
        }

        return TextUtils.join(",", actions);
    }

    public static ArrayList<Integer> getSelectedActionsOrDefault(SharedPreferences sharedPreferences, String key, Context context) {
        String commaSeparatedActions = sharedPreferences.getString(key, "");
        ArrayList<Integer> actions = parseActionsFromString(commaSeparatedActions);

        if (actions == null) {
            return toActionIdsList(getAllActions(sharedPreferences, context), true);
        }
        return actions;
    }

    private static ArrayList<Integer> toActionIdsList(LinkedHashMap<Integer, Action> actions, boolean excludeDisabled) {
        ArrayList<Integer> arrayList = new ArrayList<>();

        for (Action action : actions.values()) {
            if (!excludeDisabled) {
                arrayList.add(action.mId);
            } else if (action.mEnabled) {
                arrayList.add(action.mId);
            }
        }

        return arrayList;
    }

    private final class ActionViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener {
        private CheckChangedListener mListener;

        public TextView mTextView;
        public CheckBox mCheckBox;

        public ActionViewHolder(View itemView, CheckChangedListener listener) {
            super(itemView);

            mListener = listener;

            mTextView = (TextView) itemView.findViewById(R.id.action_textview);
            mCheckBox = (CheckBox) itemView.findViewById(R.id.action_checkbox);
            mCheckBox.setOnCheckedChangeListener(this);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Integer tag = (Integer) buttonView.getTag();
            if (tag != null) {
                mListener.onCheckedChanged(tag, isChecked);
            }
        }
    }

    private static final class InsetDecoration extends RecyclerView.ItemDecoration {
        private final int mInsets;

        public InsetDecoration(Context context) {
            mInsets = context.getResources().getDimensionPixelSize(R.dimen.card_insets);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.set(mInsets, mInsets, mInsets, mInsets);
        }
    }
}
