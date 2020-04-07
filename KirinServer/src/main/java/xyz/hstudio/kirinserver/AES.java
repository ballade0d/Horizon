package xyz.hstudio.kirinserver;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class AES {

    public static byte[] encrypt(final String msg, final byte[] key) throws Exception {
        SecretKey secretKey = new SecretKeySpec(key, 0, key.length, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(msg.getBytes(StandardCharsets.UTF_8));
    }

    public static String decrypt(final byte[] msg, final byte[] key) throws Exception {
        SecretKey secretKey = new SecretKeySpec(key, 0, key.length, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return new String(cipher.doFinal(msg));
    }
}