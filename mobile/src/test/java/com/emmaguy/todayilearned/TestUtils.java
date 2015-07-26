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

    public static String loadFile(String filename) throws IOException {
        return convertStreamToString(ClassLoader.getSystemResourceAsStream(filename));
    }

    private static String convertStreamToString(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder out = new StringBuilder();
        String newLine = System.getProperty("line.separator");
        String line;
        while ((line = reader.readLine()) != null) {
            out.append(line);
            out.append(newLine);
        }
        return out.toString();
    }
}
