package com.emmaguy.todayilearned.refresh;

import com.google.gson.annotations.SerializedName;

/**
 * An element in a listing, directly from the server
 */
class PostResponse {
    @SerializedName("kind") private String kind;
    @SerializedName("data") private Data data;

    public String getKind() {
        return kind;
    }

    public Data getData() {
        return data;
    }

    static class Data {
        private String created_utc;
        private String subreddit;
        private String permalink;
        private String thumbnail;
        private String self_text;
        private String subject = "";
        private String author;
        private String title;
        private String body = "";
        private String name;
        private String url;
        private String id;

        private Media media;

        private boolean score_hidden;
        private boolean stickied;
        private int score;
        private int gilded;

        public String getTitle() {
            return title;
        }

        public String getSubreddit() {
            return subreddit;
        }

        public String getCreatedUtc() {
            return created_utc;
        }

        public String getPermalink() {
            return permalink;
        }

        public String getAuthor() {
            return author;
        }

        public String getThumbnail() {
            return thumbnail;
        }

        public String getId() {
            return id;
        }

        public int getScore() {
            return score;
        }

        public int getGilded() {
            return gilded;
        }

        public boolean isScoreHidden() {
            return score_hidden;
        }

        public String getUrl() {
            return url;
        }

        public Media getMedia() {
            return media;
        }

        public boolean isStickied() {
            return stickied;
        }

        public String getSelfText() {
            return self_text;
        }

        public String getSubject() {
            return subject;
        }

        public String getBody() {
            return body;
        }

        public String getName() {
            return name;
        }

        static class Media {
            private String type;
            private Oembed oembed;

            public Oembed getOembed() {
                return oembed;
            }

            static class Oembed {
                private String thumbnail_url;

                public String getThumbnailUrl() {
                    return thumbnail_url;
                }
            }
        }
    }
}
