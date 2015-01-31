package com.emmaguy.todayilearned;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Loader;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;

import com.emmaguy.todayilearned.sharedlib.Constants;

import java.io.File;
import java.io.FileInputStream;

public class ViewImageActivity extends Activity implements LoaderManager.LoaderCallbacks<Bitmap> {
    private PanView mImageView;

    private String mImageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_image);

        mImageName = getIntent().getStringExtra(Constants.KEY_HIGHRES_IMAGE_NAME);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mImageView = (PanView) stub.findViewById(R.id.fullscreen_panview);
                getLoaderManager().initLoader(0, null, ViewImageActivity.this);
            }
        });
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
        mImageView.setImage(data);
    }

    @Override
    public void onLoaderReset(Loader<Bitmap> loader) {
        mImageView.setImage(null);
    }
}
