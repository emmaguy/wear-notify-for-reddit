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
    private PanView mPanView;
    private ProgressBar mProgressBar;
    private DismissOverlayView mDismissOverlay;

    private GestureDetector mDetector;

    private String mImageName;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_image);

        mPanView = (PanView) findViewById(R.id.view_image_panview);
        mProgressBar = (ProgressBar) findViewById(R.id.view_image_progressbar);

        mDismissOverlay = (DismissOverlayView) findViewById(R.id.view_image_dismiss_overlay);
        mDismissOverlay.setIntroText(R.string.long_press_information);
        mDismissOverlay.showIntroIfNecessary();

        mPanView.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                return mDetector.onTouchEvent(event);
            }
        });

        mDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override public void onLongPress(MotionEvent ev) {
                mDismissOverlay.show();
            }
        });

        mImageName = getIntent().getStringExtra(Constants.KEY_HIGHRES_IMAGE_NAME);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override protected void onPause() {
        super.onPause();

        // If the activity goes off screen for any reason (e.g. screen time out), end it
        // We want to go back to the notification, not have an activity floating around
        finish();
    }

    @Override public Loader<Bitmap> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Bitmap>(this) {

            @Override protected void onStartLoading() {
                forceLoad();
            }

            @Override public Bitmap loadInBackground() {
                return readBitmapFromDisk(mImageName);
            }
        };
    }

    private Bitmap readBitmapFromDisk(String imageName) {
        try {
            File localCache = new File(getCacheDir(), imageName);
            return BitmapFactory.decodeStream(new FileInputStream(localCache));
        } catch (Exception e) {
            Logger.log("Failed to get image from disk: ", e);
        }
        return null;
    }

    @Override public void onLoadFinished(Loader<Bitmap> loader, Bitmap data) {
        mProgressBar.setVisibility(View.GONE);
        mPanView.setVisibility(View.VISIBLE);
        mPanView.setImage(data);
    }

    @Override public void onLoaderReset(Loader<Bitmap> loader) {
        mPanView.setImage(null);
        mPanView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }
}
