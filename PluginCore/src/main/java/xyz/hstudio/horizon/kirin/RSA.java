package xyz.hstudio.horizon.kirin;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.security.PublicKey;

public final class RSA {

    private RSA() {
    }

    public static byte[] encrypt(final byte[] raw, final PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        int inputLen = raw.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        for (int i = 0; inputLen - offSet > 0; offSet = i * 117) {
            byte[] cache;
            if (inputLen - offSet > 117) {
                cache = cipher.doFinal(raw, offSet, 117);
            } else {
                cache = cipher.doFinal(raw, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            ++i;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        return encryptedData;
    }
}