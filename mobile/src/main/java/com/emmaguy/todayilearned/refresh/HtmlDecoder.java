package com.emmaguy.todayilearned.refresh;

import android.support.annotation.NonNull;
import android.text.Html;

public class HtmlDecoder {
    @NonNull public String decode(String encodedUrl) {
        return Html.fromHtml(encodedUrl).toString();
    }
}
