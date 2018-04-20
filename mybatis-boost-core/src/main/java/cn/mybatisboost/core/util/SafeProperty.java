package cn.mybatisboost.core.util;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class SafeProperty {

    private static ConcurrentMap<Integer, Boolean> cache = new ConcurrentHashMap<>();

    public static String[] of(Class<?> type, String... properties) {
        cache.computeIfAbsent(type.hashCode() ^ Arrays.hashCode(properties), (k) -> {
            for (String property : properties) {
                try {
                    type.getDeclaredField(property);
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
            }
            return true;
        });
        return properties;
    }
}
