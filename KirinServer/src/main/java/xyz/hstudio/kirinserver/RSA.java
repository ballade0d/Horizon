package xyz.hstudio.kirinserver;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.security.interfaces.RSAPrivateKey;

public class RSA {

    public static byte[] decrypt(final byte[] raw, final RSAPrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        int inputLen = raw.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        for (int i = 0; inputLen - offSet > 0; offSet = i * 256) {
            byte[] cache;
            if (inputLen - offSet > 256) {
                cache = cipher.doFinal(raw, offSet, 256);
            } else {
                cache = cipher.doFinal(raw, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            ++i;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();
        return decryptedData;
    }
}