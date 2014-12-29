package com.emmaguy.todayilearned.data.response;

public class MarkAllReadResponse {
    private boolean mHasErrors;
    private String mErrors;

    @Override
    public String toString() {
        return "MarkAllReadResponse has errors: " + mHasErrors + " " + mErrors;
    }

    public void setErrors(String errors) {
        mHasErrors = true;
        mErrors = errors;
    }

    public boolean hasErrors() {
        return mHasErrors;
    }
}