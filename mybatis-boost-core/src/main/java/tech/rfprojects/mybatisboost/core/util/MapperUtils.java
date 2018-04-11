package tech.rfprojects.mybatisboost.core.util;

import tech.rfprojects.mybatisboost.core.mapper.GenericMapper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class MapperUtils {

    private static ConcurrentMap<String, Class<?>> entityTypeCache = new ConcurrentHashMap<>();

    public static Class<?> getEntityTypeFromMapper(String mapperClassName) {
        return entityTypeCache.computeIfAbsent(mapperClassName, k -> {
            try {
                Class<?> type = Class.forName(mapperClassName);
                Class<?>[] interfaces = type.getInterfaces();
                Type[] genericInterfaces = type.getGenericInterfaces();
                for (int i = 0; i < interfaces.length; i++) {
                    if (GenericMapper.class.isAssignableFrom(interfaces[i])) {
                        return (Class<?>) ((ParameterizedType) genericInterfaces[i]).getActualTypeArguments()[0];
                    }
                }
                throw new ClassNotFoundException("GenericMapper interface not found on the mapper class");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
