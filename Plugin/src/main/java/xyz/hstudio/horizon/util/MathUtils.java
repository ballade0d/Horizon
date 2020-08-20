package xyz.hstudio.horizon.util;

public class MathUtils {

    public static int getPingInTicks(long ping) {
        return (int) Math.floor(ping / 50D);
    }
}