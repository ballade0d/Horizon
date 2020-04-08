package xyz.hstudio.horizon.kirin;

import xyz.hstudio.horizon.util.SystemClassLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

public class Kirin {

    private final PublicKey publicKey;
    private final String licence;

    public Kirin(final File keyFile, final String licence) throws Exception {
        byte[] keyBytes = Files.readAllBytes(keyFile.toPath());
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        this.publicKey = kf.generatePublic(spec);

        this.licence = licence;

        new Thread(() -> {
            try {
                Socket socket = new Socket("127.0.0.1", 5000);
                byte[] licenceByte = RSA.encrypt(this.licence.getBytes(StandardCharsets.UTF_8), this.publicKey);
                socket.getOutputStream().write(licenceByte);
                socket.shutdownOutput();

                InputStream inputStream = socket.getInputStream();

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] cacheData = new byte[16384];
                while ((nRead = inputStream.read(cacheData, 0, cacheData.length)) != -1) {
                    buffer.write(cacheData, 0, nRead);
                }

                byte[] code = buffer.toByteArray();

                try {
                    Constructor<?> ctr = SystemClassLoader.define(code).getDeclaredConstructor();
                    ctr.setAccessible(true);
                    ctr.newInstance();
                    ctr.setAccessible(false);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    throw new IllegalStateException(new String(code));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}