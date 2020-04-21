package xyz.hstudio.kirinserver;

import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPOutputStream;

public class Verification {

    private static Verification server;

    public static void main(final String[] args) throws Exception {
        server = new Verification();
    }

    private static void genKeyAndSave() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(1024, new SecureRandom());
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

        {
            File priKey = new File("priKey.key");
            OutputStream stream = new FileOutputStream(priKey);
            stream.write(privateKey.getEncoded());
            stream.flush();
            stream.close();
        }

        {
            File pubKey = new File("pubKey.key");
            OutputStream stream = new FileOutputStream(pubKey);
            stream.write(publicKey.getEncoded());
            stream.flush();
            stream.close();
        }
    }

    private final PrivateKey privateKey;
    private final Map<User, File> userMap = new ConcurrentHashMap<>();

    public Verification() throws Exception {
        byte[] keyBytes = Files.readAllBytes(new File("priKey.key").toPath());

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");

        this.privateKey = kf.generatePrivate(spec);


        File folder = new File("users");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                userMap.put(JSON.parseObject(FileUtils.readFileToString(file, StandardCharsets.UTF_8), User.class), file);
            }
        }

        new Thread(() -> {
            try {
                ServerSocket server = new ServerSocket(28589);
                while (true) {
                    Socket socket = server.accept();
                    new ClientHandler(socket).start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @RequiredArgsConstructor
    private static class ClientHandler {

        private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(8);

        private final Socket socket;

        private void start() {
            THREAD_POOL.execute(() -> {
                try {
                    InputStream inputStream = socket.getInputStream();
                    byte[] rawData = IOUtils.toByteArray(inputStream);
                    byte[] decrypt = RSA.decrypt(rawData, Verification.server.privateKey);
                    User user = Verification.server.userMap
                            .keySet()
                            .stream()
                            .filter(u -> u.licence.equals(new String(decrypt)))
                            .findFirst()
                            .orElse(null);
                    OutputStream output = new GZIPOutputStream(socket.getOutputStream());
                    if (user == null) {
                        output.write("FAILED".getBytes());
                    } else {
                        output.write(user.name.getBytes());
                    }
                    output.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}