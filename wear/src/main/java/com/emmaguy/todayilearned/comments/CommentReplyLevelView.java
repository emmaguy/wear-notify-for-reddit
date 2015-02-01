package com.emmaguy.todayilearned.comments;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.emmaguy.todayilearned.R;

public class CommentReplyLevelView extends View {
    private final int mBitmapHeight;
    private final int mBitmapWidth;
    private final int mHorizontalSpacing;
    private Bitmap mOnBitmap;
    private int mReplyLevel;

    public CommentReplyLevelView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray style = getContext().obtainStyledAttributes(attrs, R.styleable.CommentReplyLevelView, 0, 0);

        mHorizontalSpacing = style.getDimensionPixelSize(R.styleable.CommentReplyLevelView_horizontalSpacing, 0);

        int onResourceId = style.getResourceId(R.styleable.CommentReplyLevelView_drawable, 0);
        if (onResourceId <= 0 || mHorizontalSpacing <= 0) {
            throw new RuntimeException("Mandatory custom attribute not set, CommentReplyLevelView requires horizontalSpacing and drawable");
        }

        mOnBitmap = BitmapFactory.decodeResource(getResources(), onResourceId);
        mBitmapWidth = mOnBitmap.getWidth();
        mBitmapHeight = mOnBitmap.getHeight();

        style.recycle();

        // So we can see the view at design time
        if (isInEditMode()) {
            setReplyLevel(5);
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(widthMeasureSpec, mBitmapHeight);
    }

    public void setReplyLevel(int replyLevel) {
        mReplyLevel = replyLevel;

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int x = 0;
        int y = 0;
        for (int i = 0; i < mReplyLevel; i++) {
            canvas.drawBitmap(mOnBitmap, x, y, null);
            x += mBitmapWidth + mHorizontalSpacing;
        }
    }
}
