package cn.mybatisboost.core.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class SafeProperty {

    private static ConcurrentMap<MultipleMapKey, Boolean> cache = new ConcurrentHashMap<>();

    public static String[] of(Class<?> type, String... properties) {
        cache.computeIfAbsent(new MultipleMapKey(type, properties), k -> {
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
