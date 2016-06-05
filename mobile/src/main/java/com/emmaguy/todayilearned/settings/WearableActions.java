package com.emmaguy.todayilearned.settings;

import android.content.Context;
import android.content.res.Resources;

import com.emmaguy.todayilearned.R;
import com.emmaguy.todayilearned.common.PocketUtils;
import com.emmaguy.todayilearned.storage.TokenStorage;

import java.util.LinkedHashMap;

import javax.inject.Inject;

import static com.emmaguy.todayilearned.sharedlib.Constants.ACTION_ORDER_DOWNVOTE;
import static com.emmaguy.todayilearned.sharedlib.Constants.ACTION_ORDER_OPEN_ON_PHONE;
import static com.emmaguy.todayilearned.sharedlib.Constants.ACTION_ORDER_REPLY;
import static com.emmaguy.todayilearned.sharedlib.Constants.ACTION_ORDER_SAVE_TO_POCKET;
import static com.emmaguy.todayilearned.sharedlib.Constants.ACTION_ORDER_UPVOTE;
import static com.emmaguy.todayilearned.sharedlib.Constants.ACTION_ORDER_VIEW_COMMENTS;
import static com.emmaguy.todayilearned.sharedlib.Constants.ACTION_ORDER_VIEW_IMAGE;

/**
 * Wrapper around all the possible wearable actions a user can enable/disable/reorder
 */
public class WearableActions {
    private final TokenStorage mTokenStorage;
    private final Resources mResources;
    private final Context mContext;

    @Inject
    public WearableActions(Context context, Resources resources, TokenStorage tokenStorage) {
        mContext = context;
        mResources = resources;
        mTokenStorage = tokenStorage;
    }

    public LinkedHashMap<Integer, Action> getAllActions() {
        LinkedHashMap<Integer, Action> actions = new LinkedHashMap<>();
        addAction(new Action(ACTION_ORDER_VIEW_IMAGE,
                mResources.getString(R.string.action_view_image),
                R.drawable.view_image), actions);
        addAction(new Action(ACTION_ORDER_VIEW_COMMENTS,
                mResources.getString(R.string.action_view_comments),
                R.drawable.view_comments), actions);

        final String requiresLogin = mResources.getString(R.string.requires_login);
        final boolean isLoggedIn = mTokenStorage.isLoggedIn();

        addAction(new Action(ACTION_ORDER_REPLY,
                mResources.getString(R.string.action_reply),
                R.drawable.reply,
                isLoggedIn,
                requiresLogin), actions);
        addAction(new Action(ACTION_ORDER_UPVOTE,
                mResources.getString(R.string.action_upvote),
                R.drawable.upvote,
                isLoggedIn,
                requiresLogin), actions);
        addAction(new Action(ACTION_ORDER_DOWNVOTE,
                mResources.getString(R.string.action_downvote),
                R.drawable.downvote,
                isLoggedIn,
                requiresLogin), actions);

        addAction(new Action(ACTION_ORDER_SAVE_TO_POCKET,
                mResources.getString(R.string.action_save_to_pocket),
                R.drawable.pocket,
                PocketUtils.isPocketInstalled(mContext),
                mResources.getString(R.string.requires_pocket_app_installed)), actions);

        addAction(new Action(ACTION_ORDER_OPEN_ON_PHONE,
                mResources.getString(R.string.action_open_on_phone),
                R.drawable.open_on_phone), actions);
        return actions;
    }

    private void addAction(Action action, LinkedHashMap<Integer, Action> actions) {
        actions.put(action.getId(), action);
    }
}
