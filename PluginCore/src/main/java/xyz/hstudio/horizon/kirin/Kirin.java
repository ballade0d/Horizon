package xyz.hstudio.horizon.kirin;

import xyz.hstudio.horizon.Horizon;
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
import java.util.zip.GZIPInputStream;

public class Kirin {

    public Kirin(final File keyFile, final String licence) throws Exception {
        byte[] keyBytes = Files.readAllBytes(keyFile.toPath());
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey publicKey = kf.generatePublic(spec);

        try {
            Socket socket = new Socket("mc3.mccsm.cn", 28589);
            socket.setSoTimeout(10000);
            byte[] licenceByte = RSA.encrypt(licence.getBytes(StandardCharsets.UTF_8), publicKey);
            socket.getOutputStream().write(licenceByte);
            socket.shutdownOutput();

            InputStream inputStream = new GZIPInputStream(socket.getInputStream());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[10240];
            int len;
            while ((len = inputStream.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }

            byte[] code = out.toByteArray();

            try {
                Constructor<?> ctr = SystemClassLoader.define(code)
                        .getDeclaredConstructor(Horizon.class);
                ctr.setAccessible(true);
                ctr.newInstance(Horizon.getInst());
                ctr.setAccessible(false);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}