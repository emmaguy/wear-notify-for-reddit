package com.emmaguy.todayilearned;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Loader;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.wearable.view.DismissOverlayView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;

import com.emmaguy.todayilearned.sharedlib.Constants;

import java.io.File;
import java.io.FileInputStream;

public class ViewImageActivity extends Activity implements LoaderManager.LoaderCallbacks<Bitmap> {
    private PanView mImageView;
    private ProgressBar mProgressBar;
    private DismissOverlayView mDismissOverlay;

    private GestureDetector mDetector;

    private String mImageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_image);

        mImageName = getIntent().getStringExtra(Constants.KEY_HIGHRES_IMAGE_NAME);

        mImageView = (PanView) findViewById(R.id.view_image_panview);
        mProgressBar = (ProgressBar) findViewById(R.id.view_image_progressbar);

        getLoaderManager().initLoader(0, null, ViewImageActivity.this);

        mDismissOverlay = (DismissOverlayView) findViewById(R.id.view_image_dismiss_overlay);
        mDismissOverlay.setIntroText(R.string.long_press_information);
        mDismissOverlay.showIntroIfNecessary();

        mDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            public void onLongPress(MotionEvent ev) {
                mDismissOverlay.show();
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mDetector.onTouchEvent(ev) || super.onTouchEvent(ev);
    }

    @Override
    public Loader<Bitmap> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Bitmap>(this) {

            @Override
            protected void onStartLoading() {
                forceLoad();
            }

            @Override
            public Bitmap loadInBackground() {
                return readBitmapFromDisk(mImageName);
            }
        };
    }

    private Bitmap readBitmapFromDisk(String imageName) {
        try {
            File localCache = new File(getCacheDir(), imageName);
            return BitmapFactory.decodeStream(new FileInputStream(localCache));
        } catch (Exception e) {
            Logger.Log("Failed to get image from disk: ", e);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Bitmap> loader, Bitmap data) {
        mProgressBar.setVisibility(View.GONE);
        mImageView.setVisibility(View.VISIBLE);
        mImageView.setImage(data);
    }

    @Override
    public void onLoaderReset(Loader<Bitmap> loader) {
        mImageView.setImage(null);
    }
}
