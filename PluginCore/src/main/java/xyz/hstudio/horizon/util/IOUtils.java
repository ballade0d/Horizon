package xyz.hstudio.horizon.util;

import java.io.BufferedReader;
import java.io.Reader;

public class IOUtils {

    private IOUtils() {
    }

    public static String toString(final Reader reader) {
        try {
            BufferedReader in = new BufferedReader(reader);
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}