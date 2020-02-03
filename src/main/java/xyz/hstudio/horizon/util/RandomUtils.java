package xyz.hstudio.horizon.util;

import java.util.concurrent.ThreadLocalRandom;

public class RandomUtils {
    public static short nextShort() {
        return (short) ThreadLocalRandom.current().nextInt(32767);
    }

    public static boolean nextBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }
}