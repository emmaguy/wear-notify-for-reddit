package com.emmaguy.todayilearned.refresh;

import java.util.List;

class ListingResponse {
    private String kind;
    private DataResponse data;

    public DataResponse getData() {
        return data;
    }

    static class DataResponse {
        private String after;
        private String before;
        private String modhash;
        private List<PostResponse> children;

        public List<PostResponse> getChildren() {
            return children;
        }
    }
}
