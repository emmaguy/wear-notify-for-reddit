package com.emmaguy.todayilearned.comments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.view.WindowInsets;

import com.emmaguy.todayilearned.R;
import com.emmaguy.todayilearned.sharedlib.Constants;
import com.emmaguy.todayilearned.sharedlib.Post;
import com.google.gson.Gson;

import java.util.ArrayList;

public class CommentsActivity extends Activity implements ActionFragment.OnActionListener {
    private final Gson mGson = new Gson();
    private GridViewPager mGridViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_comments);

        String stringComments = getIntent().getStringExtra(Constants.KEY_REDDIT_POSTS);

        final ArrayList<Post> comments = mGson.fromJson(stringComments, Post.getPostsListTypeToken());
        if (comments == null) {
            finish();
            return;
        }

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub watchViewStub) {
                mGridViewPager = (GridViewPager) findViewById(R.id.pager);
                mGridViewPager.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                        // A little extra horizontal spacing between pages looks a bit less crowded on a round display
                        int rowMargin = getResources().getDimensionPixelOffset(R.dimen.page_row_margin);
                        int colMargin = getResources().getDimensionPixelOffset(insets.isRound() ? R.dimen.page_column_margin_round : R.dimen.page_column_margin);
                        mGridViewPager.setPageMargins(rowMargin, colMargin);

                        // GridViewPager relies on insets to properly handle layout for round displays
                        // They must be explicitly applied since this listener has taken them over
                        mGridViewPager.onApplyWindowInsets(insets);
                        return insets;
                    }
                });
                mGridViewPager.setAdapter(new CommentsGridPagerAdapter(CommentsActivity.this, getFragmentManager(), comments));

                DotsPageIndicator dotsPageIndicator = (DotsPageIndicator) findViewById(R.id.page_indicator);
                dotsPageIndicator.setPager(mGridViewPager);
            }
        });
    }

    @Override
    public void onActionPerformed(String replies) {
        Intent intent = new Intent(this, CommentsActivity.class);
        intent.putExtra(Constants.KEY_REDDIT_POSTS, replies);
        startActivity(intent);
    }
}
