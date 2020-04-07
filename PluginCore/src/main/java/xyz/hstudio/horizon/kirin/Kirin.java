package xyz.hstudio.horizon.kirin;

import xyz.hstudio.horizon.kirin.encrypt.AES;

import java.io.File;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

public class Kirin {

    private final byte[] clientKey = AES.generate();
    private final PublicKey publicKey;
    private final String licence;

    public Kirin(final File keyFile, final String licence) throws Exception {
        byte[] keyBytes = Files.readAllBytes(keyFile.toPath());
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        this.publicKey = kf.generatePublic(spec);

        this.licence = licence;

        new Thread(() -> {

        }).start();
    }
}