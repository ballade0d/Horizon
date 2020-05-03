package xyz.hstudio.horizon.kirin;

import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.Logger;
import xyz.hstudio.horizon.kirin.module.Kirin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.zip.GZIPInputStream;

public class Verification {

    public Verification(final File keyFile, final String licence) throws Exception {
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
            socket.shutdownInput();

            byte[] code = out.toByteArray();

            String result = new String(code);

            if ("FAILED".equals(result)) {
                Logger.msg("Kirin", "Invalid key.");
            } else {
                new Kirin(Horizon.getInst(), result);
            }
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}