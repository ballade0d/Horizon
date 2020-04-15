package xyz.hstudio.horizon.util;

import java.util.concurrent.ThreadLocalRandom;

public final class RandomUtils {

    private RandomUtils() {
    }

    public static short nextShort() {
        return (short) ThreadLocalRandom.current().nextInt(32767);
    }

    public static int randomBoundaryInt(final int min, final int randomBoundary) {
        return min + ThreadLocalRandom.current().nextInt(randomBoundary);
    }
}