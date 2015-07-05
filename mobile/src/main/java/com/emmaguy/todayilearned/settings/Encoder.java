package com.emmaguy.todayilearned.settings;

/**
 * Created by emma on 14/06/15.
 */
public interface Encoder {
    String encode(byte[] bytes);
    byte[] decode(String string);
}
