package com.emmaguy.todayilearned.refresh;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.emmaguy.todayilearned.common.Logger;

import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Created by emma on 19/07/15.
 */
public class ImageDownloader {
    private static final int WATCH_SCREEN_SIZE = 320;
    private static final int MARKER = 65536;

    private final Context mContext;

    @Inject public ImageDownloader(Context context) {
        mContext = context;
    }

    public byte[] downloadImage(String imageUrl) {
        byte[] bytes = null;
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();

            MarkableInputStream markStream = new MarkableInputStream(connection.getInputStream());
            long mark = markStream.savePosition(MARKER);

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(markStream, null, options);

            options.inSampleSize = calculateInSampleSize(options, WATCH_SCREEN_SIZE, WATCH_SCREEN_SIZE);
            options.inJustDecodeBounds = false;

            markStream.reset(mark);

            final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(markStream, null, options);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);

            bytes = byteStream.toByteArray();
            bitmap.recycle();
        } catch (Exception e) {
            Timber.e(e, "Failed to download image");
        }

        return bytes;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
