package com.emmaguy.todayilearned;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;

import com.emmaguy.todayilearned.sharedlib.Post;

import java.util.ArrayList;
import java.util.List;

public class CommentsGridPagerAdapter extends FragmentGridPagerAdapter {
    private final Context mContext;
    private List<Row> mRows;

    public CommentsGridPagerAdapter(Context context, FragmentManager fm, ArrayList<Post> comments) {
        super(fm);

        mContext = context;
        mRows = new ArrayList<CommentsGridPagerAdapter.Row>();

        for (Post p : comments) {
            Fragment cardFragment = cardFragment(p.getAuthor(), p.getDescription(), p.getUps(), p.getDowns());

            if (p.getReplies() != null && !p.getReplies().isEmpty()) {
                Fragment actionFragment = ActionFragment.create(p.getReplies());
                mRows.add(new Row(cardFragment, actionFragment));
            } else {
                mRows.add(new Row(cardFragment));
            }
        }
    }

    private Fragment cardFragment(String title, String text, long ups, long downs) {
        CardFragment fragment = RedditCommentCardFragment.create(title, text, ups, downs);
        // Add some extra bottom margin to leave room for the page indicator
        fragment.setCardMarginBottom(mContext.getResources().getDimensionPixelSize(R.dimen.card_margin_bottom));
        return fragment;
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
}
