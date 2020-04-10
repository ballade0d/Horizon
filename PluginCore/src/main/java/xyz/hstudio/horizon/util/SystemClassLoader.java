package xyz.hstudio.horizon.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SystemClassLoader {

    private static ClassLoader LOADER;
    private static Method DEFINE;

    public static void init(final ClassLoader loader) {
        LOADER = loader;
        Method define = null;
        try {
            define = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
            define.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        DEFINE = define;
    }

    public static Class<?> define(final byte[] code) throws Throwable {
        Class<?> result;
        try {
            result = (Class<?>) DEFINE.invoke(LOADER, null, code, 0, code.length);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
        return result;
    }
}