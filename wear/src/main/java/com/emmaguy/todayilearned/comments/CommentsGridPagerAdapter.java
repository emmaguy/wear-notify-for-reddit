package com.emmaguy.todayilearned.comments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridPagerAdapter;
import android.util.LruCache;

import com.emmaguy.todayilearned.R;
import com.emmaguy.todayilearned.sharedlib.Post;

import java.util.ArrayList;
import java.util.List;

public class CommentsGridPagerAdapter extends FragmentGridPagerAdapter {
    private static final int TRANSITION_DURATION_MILLIS = 100;
    LruCache<Point, Drawable> mPageBackgrounds = new LruCache<Point, Drawable>(1) {
        @Override
        protected Drawable create(final Point page) {
            TransitionDrawable background = new TransitionDrawable(new Drawable[]{
                    new ColorDrawable(R.color.primary),
                    new ColorDrawable(R.color.primary_darkest)
            });
            mPageBackgrounds.put(page, background);
            notifyPageBackgroundChanged(page.y, page.x);
            background.startTransition(TRANSITION_DURATION_MILLIS);

            return background;
        }
    };
    private final Context mContext;
    private final List<Row> mRows;

    public CommentsGridPagerAdapter(Context context, FragmentManager fm, ArrayList<Post> comments) {
        super(fm);

        mContext = context;
        mRows = new ArrayList<CommentsGridPagerAdapter.Row>();

        for (Post p : comments) {
            Fragment cardFragment = cardFragment(p);

            if (p.getReplies() != null && !p.getReplies().isEmpty()) {
                Fragment actionFragment = ActionFragment.create(p.getReplies());
                mRows.add(new Row(cardFragment, actionFragment));
            } else {
                mRows.add(new Row(cardFragment));
            }
        }
    }

    private Fragment cardFragment(Post p) {
        CardFragment fragment = RedditCommentCardFragment.create(p);
        // Add some extra bottom margin to leave room for the page indicator
        fragment.setCardMarginBottom(mContext.getResources().getDimensionPixelSize(R.dimen.card_margin_bottom));
        return fragment;
    }

    @Override
    public Drawable getBackgroundForPage(final int row, final int column) {
        if (column != 0) {
            return mPageBackgrounds.get(new Point(column, row));
        }
        return GridPagerAdapter.BACKGROUND_NONE;
    }

    @Override
    public Fragment getFragment(int row, int col) {
        Row adapterRow = mRows.get(row);
        return adapterRow.getColumn(col);
    }

    @Override
    public int getRowCount() {
        return mRows.size();
    }

    @Override
    public int getColumnCount(int rowNum) {
        return mRows.get(rowNum).getColumnCount();
    }

    private class Row {
        final List<Fragment> columns = new ArrayList<Fragment>();

        public Row(Fragment... fragments) {
            for (Fragment f : fragments) {
                add(f);
            }
        }

        public void add(Fragment f) {
            columns.add(f);
        }

        Fragment getColumn(int i) {
            return columns.get(i);
        }

        public int getColumnCount() {
            return columns.size();
        }
    }
}
