package xyz.hstudio.horizon.bukkit.util;

import java.util.concurrent.ThreadLocalRandom;

public class RandomUtils {
    public static short nextShort() {
        return (short) ThreadLocalRandom.current().nextInt(32767);
    }
}