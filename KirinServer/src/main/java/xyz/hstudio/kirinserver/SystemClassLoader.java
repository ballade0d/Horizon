package xyz.hstudio.kirinserver;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class SystemClassLoader {

    private SystemClassLoader() {
    }

    public static byte[] getClassData(final String path) {
        try {
            InputStream is = new FileInputStream(new File(path));
            byte[] buff = new byte[1024 * 4];
            int len;
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            while ((len = is.read(buff)) != -1) {
                stream.write(buff, 0, len);
            }
            return stream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Server error".getBytes();
    }
}