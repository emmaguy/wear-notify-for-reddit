package com.emmaguy.todayilearned.comments;

import android.os.Bundle;
import android.support.wearable.view.CardFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.emmaguy.todayilearned.R;
import com.emmaguy.todayilearned.sharedlib.Post;

public class RedditCommentCardFragment extends CardFragment {
    private static final String ARGS_KEY_TITLE = "card_title";
    private static final String ARGS_KEY_TEXT = "card_text";
    private static final String ARGS_KEY_SCORE_HIDDEN = "card_score_hidden";
    private static final String ARGS_KEY_SCORE = "card_score";
    private static final String ARGS_KEY_GILDED = "card_gilded";
    private static final String ARGS_KEY_REPLY_LEVEL = "card_reply_level";

    private String mTitle;
    private String mText;

    private boolean mIsScoreHidden;
    private int mScore;
    private int mGildedCount;
    private int mReplyLevel;

    public static CardFragment create(Post p) {
        Bundle args = new Bundle();
//        args.putString(ARGS_KEY_TITLE, p.getAuthor());
//        args.putString(ARGS_KEY_TEXT, p.getDescription());
//        args.putBoolean(ARGS_KEY_SCORE_HIDDEN, p.isScoreHidden());
//        args.putInt(ARGS_KEY_SCORE, p.getScore());
//        args.putInt(ARGS_KEY_GILDED, p.getGilded());
//        args.putInt(ARGS_KEY_REPLY_LEVEL, p.getReplyLevel());

        RedditCommentCardFragment fragment = new RedditCommentCardFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTitle = getArguments().getString(ARGS_KEY_TITLE);
        mText = getArguments().getString(ARGS_KEY_TEXT);
        mScore = getArguments().getInt(ARGS_KEY_SCORE);
        mIsScoreHidden = getArguments().getBoolean(ARGS_KEY_SCORE_HIDDEN);
        mGildedCount = getArguments().getInt(ARGS_KEY_GILDED);
        mReplyLevel = getArguments().getInt(ARGS_KEY_REPLY_LEVEL);
    }

    @Override
    public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.comment_content, null);

        TextView title = (TextView) v.findViewById(R.id.title);
        TextView text = (TextView) v.findViewById(R.id.text);
        TextView score = (TextView) v.findViewById(R.id.score);
        TextView gilded = (TextView) v.findViewById(R.id.gilded_count);
        CommentReplyLevelView levelView = (CommentReplyLevelView) v.findViewById(R.id.reply_level);

        title.setText(mTitle);
        text.setText(mText);
        if (mIsScoreHidden) {
            score.setText(getString(R.string.score_hidden));
        } else {
            score.setText(mScore + " " + getResources().getQuantityString(R.plurals.points, mScore));
        }
        levelView.setReplyLevel(mReplyLevel);

        if (mGildedCount <= 0) {
            gilded.setVisibility(View.GONE);
        } else {
            gilded.setVisibility(View.VISIBLE);
            if (mGildedCount > 1) {
                gilded.setText("x" + mGildedCount);
            }
        }
        return v;
    }
}
