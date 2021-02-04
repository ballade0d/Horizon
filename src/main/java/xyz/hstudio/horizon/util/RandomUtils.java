package xyz.hstudio.horizon.util;

import java.util.concurrent.ThreadLocalRandom;

public class RandomUtils {

    public static int randomBoundaryInt(int min, int randomBoundary) {
        return min + ThreadLocalRandom.current().nextInt(randomBoundary);
    }
}