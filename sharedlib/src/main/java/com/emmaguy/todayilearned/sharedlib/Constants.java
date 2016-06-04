package com.emmaguy.todayilearned.sharedlib;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class Constants {
    public final static Set<String> sDefaultSelectedSubreddits = new LinkedHashSet<>(Arrays.asList("todayilearned", "AskReddit"));

    public static final String WEB_URL_REDDIT = "https://www.reddit.com";
    public static final String ENDPOINT_URL_SSL_REDDIT = "https://ssl.reddit.com/";
    public static final String ENDPOINT_URL_OAUTH_REDDIT = "https://oauth.reddit.com/";

    public static final String AUTHORIZATION = "Authorization";

    public static final String GRANT_TYPE_AUTHORISATION_CODE = "authorization_code";
    public static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
    public static final String GRANT_TYPE_INSTALLED_CLIENT = "https://oauth.reddit.com/grants/installed_client";

    public static final String PATH_REDDIT_POSTS = "/redditwear";
    public static final String PATH_REFRESH = "/refresh";
    public static final String PATH_OPEN_ON_PHONE = "/openonphone";
    public static final String PATH_REPLY = "/replytopost";
    public static final String PATH_SAVE_TO_POCKET = "/send_to_pocket";
    public static final String PATH_VOTE = "/vote";
    public static final String PATH_COMMENTS = "/comments";
    public static final String PATH_NO_NEW_POSTS = "/no_new_posts";
    public static final String PATH_LOGGING = "/logging";

    public static final String KEY_REDDIT_POSTS = "posts";
    public static final String KEY_POST_PERMALINK = "post_permalink";

    public static final String PATH_POST_REPLY_RESULT_SUCCESS = "/post_reply_success";
    public static final String PATH_POST_REPLY_RESULT_FAILURE = "/post_reply_failure";

    public static final String PATH_KEY_MESSAGE = "post_message";
    public static final String PATH_KEY_POST_FULLNAME = "post_fullname";
    public static final String PATH_KEY_IS_DIRECT_MESSAGE = "is_direct";
    public static final String PATH_KEY_MESSAGE_SUBJECT = "msg_subject";
    public static final String PATH_KEY_MESSAGE_TO_USER = "msg_to_user";

    public static final String PATH_SAVE_TO_POCKET_RESULT_SUCCESS = "/save_pocket_success";
    public static final String PATH_SAVE_TO_POCKET_RESULT_FAILED = "/save_pocket_failed";

    public static final String PATH_VOTE_RESULT_SUCCESS = "/vote_success";
    public static final String PATH_VOTE_RESULT_FAILED = "/vote_failed";
    public static final String PATH_GET_COMMENTS_RESULT_FAILED = "/getting_comments";

    public static final String KEY_CONFIRMATION_MESSAGE = "confirm_message";
    public static final String KEY_PATH = "receiver_path";
    public static final String KEY_POST_VOTE_DIRECTION = "vote_direction";
    public static final String KEY_CONFIRMATION_ANIMATION = "key_confirm_animation";
    public static final String KEY_DISMISS_AFTER_ACTION = "open_on_phone_dismisses";
    public static final String KEY_NOTIFICATION_ID = "notification_id";
    public static final String KEY_ACTION_ORDER = "action_order";
    public static final String KEY_HIGHRES_IMAGE_NAME = "view_image";

    public static final int ACTION_ORDER_VIEW_COMMENTS = 0;
    public static final int ACTION_ORDER_REPLY = 1;
    public static final int ACTION_ORDER_UPVOTE = 2;
    public static final int ACTION_ORDER_DOWNVOTE = 3;
    public static final int ACTION_ORDER_SAVE_TO_POCKET = 4;
    public static final int ACTION_ORDER_OPEN_ON_PHONE = 5;
    public static final int ACTION_ORDER_VIEW_IMAGE = 6;
}
