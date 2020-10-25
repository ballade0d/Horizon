import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;

public class Encrypter {

    public static void main(final String[] args) {
        System.out.println(Arrays.toString(getClassData(new File("D:\\IdeaProject\\Horizon\\Interesting\\Shit.class"))));
    }

    public static byte[] getClassData(final File file) {
        try {
            InputStream is = new FileInputStream(file);
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
        return null;
    }
}