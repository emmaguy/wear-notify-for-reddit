package com.emmaguy.todayilearned.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.emmaguy.todayilearned.Logger;
import com.emmaguy.todayilearned.Utils;
import com.emmaguy.todayilearned.data.retrofit.RedditService;
import com.emmaguy.todayilearned.data.storage.UserStorage;
import com.emmaguy.todayilearned.sharedlib.Post;

import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by emma on 14/06/15.
 */
public class LatestPostsFromRedditRetriever {
    private static final int WATCH_SCREEN_SIZE = 320;
    private static final int MARKER = 65536;

    private final Context mContext;
    private final UserStorage mUserStorage;

    public LatestPostsFromRedditRetriever(Context context, UserStorage userStorage) {
        mContext = context;
        mUserStorage = userStorage;
    }

    public Observable<List<Post>> getPosts(final RedditService reddit) {
        return reddit.latestPosts(mUserStorage.getSubreddit(), mUserStorage.getSortType(), mUserStorage.getNumberToRequest())
                .lift(LatestPostsFromRedditRetriever.<Post>flattenList())
                .filter(new Func1<Post, Boolean>() {
                    @Override
                    public Boolean call(Post post) {
                        // Check that this post is new (i.e. we haven't retrieved it before)
                        // In debug, never ignore posts - we want content to test with
                        return (post.getCreatedUtc() > mUserStorage.getCreatedUtcOfRetrievedPosts()) || Utils.sIsDebug;
                    }
                })
                .doOnNext(new Action1<Post>() {
                    @Override
                    public void call(Post post) {
                        mUserStorage.setRetrievedPostCreatedUtc(post.getCreatedUtc());

                        // Default to just getting the thumbnail, if available
                        String imageUrl = post.getThumbnail();
                        boolean hasHighResAvailable = false;

                        // If user has chosen to get full images, only do so if we actually have a image based url
                        if (mUserStorage.downloadFullSizedImages()) {
                            if (isImage(post.getUrl())) {
                                imageUrl = post.getUrl();
                                hasHighResAvailable = true;
                            }
                        }

                        if (post.hasThumbnail() || hasHighResAvailable) {
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

                                post.setImage(byteStream.toByteArray());
                                post.setHasHighResImage(hasHighResAvailable);

                                bitmap.recycle();
                            } catch (Exception e) {
                                Logger.sendThrowable(mContext, "Failed to download image", e);
                            }
                        }
                    }
                })
                .toList();
    }

    public static <T> Observable.Operator<T, List<T>> flattenList() {
        return new Observable.Operator<T, List<T>>() {
            @Override
            public Subscriber<? super List<T>> call(final Subscriber<? super T> subscriber) {
                return new Subscriber<List<T>>() {
                    @Override
                    public void onCompleted() {
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onError(Throwable e) {
                        subscriber.onError(e);
                    }

                    @Override
                    public void onNext(List<T> list) {
                        for (T c : list) {
                            subscriber.onNext(c);
                        }
                    }
                };
            }
        };
    }

    private boolean isImage(String pictureFileName) {
        return pictureFileName.endsWith(".png")
                || pictureFileName.endsWith(".jpg")
                || pictureFileName.endsWith(".jpeg");
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
        Logger.Log("inSampleSize: " + inSampleSize);
        return inSampleSize;
    }
}
