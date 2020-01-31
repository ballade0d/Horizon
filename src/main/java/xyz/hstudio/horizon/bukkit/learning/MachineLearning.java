package xyz.hstudio.horizon.bukkit.learning;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import xyz.hstudio.horizon.bukkit.Logger;
import xyz.hstudio.horizon.bukkit.learning.core.KnnClassification;
import xyz.hstudio.horizon.bukkit.thread.Async;
import xyz.hstudio.horizon.bukkit.util.JsonUtils;
import xyz.hstudio.horizon.bukkit.util.Vec2D;

import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class MachineLearning {

    @Getter
    private KnnClassification<Vec2D> base = new KnnVector();
    @Getter
    private KnnClassification<Vec2D> category = new KnnVector();
    @Getter
    private boolean loaded = false;

    public MachineLearning() {
        Logger.msg("ML", "Downloading MachineLearning data...");
        Callable<Boolean> base = () -> {
            JSONObject object = JsonUtils.readAsObject(new URL("https://horizon.hstudio.xyz/horizon/ml_base.json"));

            List<Vec2D> cheatData = object.getJSONArray("Cheat").toJavaList(Vec2D.class);
            for (Vec2D vec2D : cheatData) {
                this.base.addRecord(vec2D, "Cheat");
            }

            List<Vec2D> vanillaData = object.getJSONArray("Vanilla").toJavaList(Vec2D.class);
            for (Vec2D vec2D : vanillaData) {
                this.base.addRecord(vec2D, "Vanilla");
            }
            return true;
        };
        Callable<Boolean> category = () -> {
            JSONObject object = JsonUtils.readAsObject(new URL("https://horizon.hstudio.xyz/horizon/ml_category.json"));

            for (String path : object.keySet()) {
                List<Vec2D> data = object.getJSONArray(path).toJavaList(Vec2D.class);
                for (Vec2D vec2D : data) {
                    this.category.addRecord(vec2D, path);
                }
            }
            return true;
        };
        Async.execute(() -> {
            try {
                loaded = Async.submit(base).get(1, TimeUnit.MINUTES) && Async.submit(category).get(1, TimeUnit.MINUTES);
            } catch (Exception ignore) {
            }
            Logger.msg("ML", isLoaded() ? "MachineLearning is fully loaded!" : "Failed to download the data!");
        });
    }
}