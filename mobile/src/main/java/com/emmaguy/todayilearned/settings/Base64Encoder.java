package com.emmaguy.todayilearned.settings;

import android.util.Base64;

/**
 * Created by emma on 14/06/15.
 */
public class Base64Encoder implements Encoder {
    public static final int ENCODING_FLAGS = Base64.NO_WRAP;

    public String encode(byte[] bytes) {
        return Base64.encodeToString(bytes, ENCODING_FLAGS);
    }

    @Override public byte[] decode(String string) {
        return Base64.decode(string, ENCODING_FLAGS);
    }
}
