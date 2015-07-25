package com.emmaguy.todayilearned;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Utils for testing, loads a file from the 'resources' directory and converts to String
 */
public class TestUtils {
    public static InputStream loadFileFromStream(String filename) {
        return ClassLoader.getSystemResourceAsStream(filename);
    }

    public static String loadFile(String filename) {
        return convertStreamToString(ClassLoader.getSystemResourceAsStream(filename));
    }

    private static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
