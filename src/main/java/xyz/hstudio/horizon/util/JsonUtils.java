package xyz.hstudio.horizon.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class JsonUtils {

    public static JSONObject readAsObject(final URL url) throws IOException {
        InputStreamReader stream = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8);
        String text = IOUtils.toString(stream);
        return JSON.parseObject(text);
    }
}