package com.emmaguy.todayilearned.settings;

import android.app.AlertDialog;
import android.content.Context;
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

import com.emmaguy.todayilearned.App;
import com.emmaguy.todayilearned.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

public class DragReorderActionsPreference extends Preference implements CheckChangedListener {
    private final HashMap<Integer, Action> mSelectedActions = new HashMap<>();
    private final ArrayList<Action> mActions = new ArrayList<>();
    private final Context mContext;

    @Inject WearableActionStorage mWearableActionStorage;
    @Inject WearableActions mWearableActions;

    public DragReorderActionsPreference(Context context) {
        this(context, null);
    }

    public DragReorderActionsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        App.with(context).getAppComponent().inject(this);
    }

    @Override
    protected void onClick() {
        super.onClick();

        showDragReorderDialog();
    }

    @Override
    public CharSequence getSummary() {
        initActions();

        ArrayList<String> actions = new ArrayList<>();

        for (Action action : mActions) {
            if (mSelectedActions.containsKey(action.mId)) {
                actions.add(getActionFromAllActions(action.mId).mName);
            }
        }

        if (actions.isEmpty()) {
            return mContext.getString(R.string.none);
        }

        return TextUtils.join(", ", actions);
    }

    private void initActions() {
        mActions.clear();
        mSelectedActions.clear();

        List<Integer> actions = mWearableActionStorage.getActionIds();
        List<Integer> selectedActions = mWearableActionStorage.getSelectedActionIds();

        for (Integer integer : actions) {
            mActions.add(getActionFromAllActions(integer));
        }

        for (Integer integer : selectedActions) {
            mSelectedActions.put(integer, getActionFromAllActions(integer));
        }
    }

    @Override
    public void onCheckedChanged(Integer id, boolean isChecked) {
        if (isChecked) {
            Action action = getActionFromAllActions(id);
            mSelectedActions.put(id, action);
        } else {
            mSelectedActions.remove(id);
        }

        mWearableActionStorage.save(mActions, mSelectedActions);
        setSummary(getSummary());
    }

    private Action getActionFromAllActions(Integer id) {
        return mWearableActions.getAllActions().get(id);
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

                mWearableActionStorage.save(mActions, mSelectedActions);
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

    private final class ActionViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener {
        public TextView mTextView;
        public CheckBox mCheckBox;
        private CheckChangedListener mListener;

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
}
