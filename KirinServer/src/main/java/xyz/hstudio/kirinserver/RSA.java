package xyz.hstudio.kirinserver;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.security.PrivateKey;

public final class RSA {

    private RSA() {
    }

    public static byte[] decrypt(final byte[] raw, final PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        int inputLen = raw.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        for (int i = 0; inputLen - offSet > 0; offSet = i * 128) {
            byte[] cache;
            if (inputLen - offSet > 128) {
                cache = cipher.doFinal(raw, offSet, 128);
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