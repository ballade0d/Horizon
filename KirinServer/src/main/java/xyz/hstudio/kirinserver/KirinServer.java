package xyz.hstudio.kirinserver;

import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPOutputStream;

public class KirinServer {

    private static KirinServer server;

    public static void main(final String[] args) throws Exception {
        server = new KirinServer();
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
    private final List<String> moduleCode;
    private final List<String> classPath;

    public KirinServer() throws Exception {
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

        this.moduleCode = new ArrayList<>();
        Reader reader = new InputStreamReader(new FileInputStream(new File("Module.java")));
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            this.moduleCode.add(line);
        }

        this.classPath = new ArrayList<>();
        File libsDir = new File("libs");
        if (!libsDir.exists()) {
            libsDir.mkdir();
        }
        File[] libs = libsDir.listFiles();
        if (libs != null) {
            for (File lib : libs) {
                this.classPath.add(lib.getPath());
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

        private static final ExecutorService threadPool = Executors.newFixedThreadPool(8);

        private final Socket socket;

        private void start() {
            threadPool.execute(() -> {
                try {
                    InputStream inputStream = socket.getInputStream();
                    byte[] rawData = IOUtils.toByteArray(inputStream);
                    byte[] decrypt = RSA.decrypt(rawData, KirinServer.server.privateKey);
                    User user = KirinServer.server.userMap
                            .keySet()
                            .stream()
                            .filter(u -> u.licence.equals(new String(decrypt)))
                            .findFirst()
                            .orElse(null);
                    OutputStream output = new GZIPOutputStream(socket.getOutputStream());
                    if (user == null) {
                        output.write("Invalid key".getBytes());
                    } else {
                        File cacheDir = new File(UUID.randomUUID().toString());
                        cacheDir.mkdir();

                        File cache = new File(cacheDir, "Module_" + UUID.randomUUID() + ".java");
                        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(cache));

                        for (String line : server.moduleCode) {
                            bufferedWriter.write(line
                                    .replace("{time}", String.valueOf(System.currentTimeMillis()))
                                    .replace("{user}", user.name));
                            bufferedWriter.newLine();
                        }
                        bufferedWriter.flush();
                        bufferedWriter.close();

                        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
                        List<String> optionList = new ArrayList<>();
                        optionList.add("-classpath");
                        StringBuilder builder = new StringBuilder();
                        for (String classPath : server.classPath) {
                            builder.append(classPath.replace("\\", "/")).append(";");
                        }
                        optionList.add(builder.substring(0, builder.length() - 1));
                        Iterable<? extends JavaFileObject> input =
                                compiler.getStandardFileManager(null, null, null)
                                        .getJavaFileObjects(cache.getAbsolutePath());
                        compiler.getTask(null, null, null, optionList, null, input).call();

                        File cacheClass = new File(cacheDir, "Module.class");

                        byte[] code = SystemClassLoader.getClassData(cacheClass.toPath().toString());

                        output.write(code);

                        cache.deleteOnExit();
                        cacheClass.deleteOnExit();
                        cacheDir.deleteOnExit();
                    }
                    output.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}