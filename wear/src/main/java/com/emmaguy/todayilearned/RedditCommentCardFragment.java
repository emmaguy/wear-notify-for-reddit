package com.emmaguy.todayilearned;

import android.os.Bundle;
import android.support.wearable.view.CardFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class RedditCommentCardFragment extends CardFragment {
    private static final String ARGS_KEY_TITLE = "card_title";
    private static final String ARGS_KEY_TEXT = "card_text";
    private static final String ARGS_KEY_UPS = "card_ups";
    private static final String ARGS_KEY_DOWNS = "card_downs";

    private String mTitle;
    private String mText;

    private long mUps;
    private long mDowns;

    public static CardFragment create(String title, String text, long ups, long downs) {
        Bundle args = new Bundle();
        args.putString(ARGS_KEY_TITLE, title);
        args.putString(ARGS_KEY_TEXT, text);
        args.putLong(ARGS_KEY_UPS, ups);
        args.putLong(ARGS_KEY_DOWNS, downs);

        RedditCommentCardFragment fragment = new RedditCommentCardFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTitle = getArguments().getString(ARGS_KEY_TITLE);
        mText = getArguments().getString(ARGS_KEY_TEXT);
        mUps = getArguments().getLong(ARGS_KEY_UPS);
        mDowns = getArguments().getLong(ARGS_KEY_DOWNS);
    }

    @Override
    public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.comment_content, null);

        TextView title = (TextView) v.findViewById(R.id.title);
        TextView text = (TextView) v.findViewById(R.id.text);
        TextView ups = (TextView) v.findViewById(R.id.upvote_count);
        TextView downs = (TextView) v.findViewById(R.id.downvote_count);

        title.setText(mTitle);
        text.setText(mText);
        ups.setText("" + mUps);

        if (mDowns == 0) {
            downs.setVisibility(View.GONE);
        } else {
            downs.setVisibility(View.VISIBLE);
            downs.setText("" + mDowns);
        }
        return v;
    }
}
