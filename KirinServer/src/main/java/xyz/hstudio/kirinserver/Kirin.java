package xyz.hstudio.kirinserver;

import com.alibaba.fastjson.JSON;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Kirin {

    public static void main(final String[] args) throws Exception {
        new Kirin();
    }

    private final PrivateKey privateKey;
    private final Map<User, File> userMap = new ConcurrentHashMap<>();

    public Kirin() throws Exception {
        File folder = new File("users");
        if (folder.exists()) {
            folder.mkdirs();
        }
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                userMap.put(JSON.parseObject(FileUtils.readFileToString(file, StandardCharsets.UTF_8), User.class), file);
            }
        }

        byte[] keyBytes = Files.readAllBytes(new File("priKey.key").toPath());

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");

        this.privateKey = kf.generatePrivate(spec);
    }
}