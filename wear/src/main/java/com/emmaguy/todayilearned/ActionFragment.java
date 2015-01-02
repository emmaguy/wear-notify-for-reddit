package com.emmaguy.todayilearned;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.emmaguy.todayilearned.sharedlib.Post;
import com.google.gson.Gson;

import java.util.List;

public class ActionFragment extends Fragment implements View.OnClickListener {
    private static final String ARGS_KEY_REPLIES = "key_replies";
    private OnActionListener mListener;
    private String mReplies;

    public static Fragment create(List<Post> replies) {
        Bundle args = new Bundle();
        args.putString(ARGS_KEY_REPLIES, new Gson().toJson(replies));

        ActionFragment f = new ActionFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mReplies = getArguments().getString(ARGS_KEY_REPLIES);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnActionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnActionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_action, container, false);
        v.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View v) {
        mListener.onActionPerformed(mReplies);
    }

    public interface OnActionListener {
        public void onActionPerformed(String replies);
    }
}

