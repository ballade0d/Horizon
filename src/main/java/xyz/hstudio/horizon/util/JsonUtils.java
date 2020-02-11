package xyz.hstudio.horizon.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class JsonUtils {

    public static JsonObject readAsObject(final URL url) throws IOException {
        InputStreamReader stream = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8);
        String text = IOUtils.toString(stream);
        return new JsonParser().parse(text).getAsJsonObject();
    }
}