package com.emmaguy.todayilearned.common;

public class StringUtils {
    /**
     * Returns true if the string is null or 0-length.
     *
     * @param str the string to be examined
     * @return true if str is null or zero length
     */
    public static boolean isEmpty(String str) {
        return (str == null || str.length() == 0);
    }
}
