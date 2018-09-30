package cn.mybatisboost.core.util;

import javassist.ClassPool;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class MapperUtils {

    private static ConcurrentMap<String, Class<?>> entityTypeCache = new ConcurrentHashMap<>();
    private static ClassLoader classLoader = new MapperClassLoader();
    private static final Class<?> GENERIC_MAPPER_CLASS;

    static {
        try {
            GENERIC_MAPPER_CLASS = classLoader.loadClass("cn.mybatisboost.core.GenericMapper");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Class<?> getEntityTypeFromMapper(String mapperClassName) {
        return entityTypeCache.computeIfAbsent(mapperClassName, k -> {
            try {
                Class<?> type = classLoader.loadClass(mapperClassName);
                Class<?>[] interfaces = type.getInterfaces();
                Type[] genericInterfaces = type.getGenericInterfaces();
                for (int i = 0; i < interfaces.length; i++) {
                    if (GENERIC_MAPPER_CLASS.isAssignableFrom(interfaces[i])) {
                        return (Class<?>) ((ParameterizedType) genericInterfaces[i]).getActualTypeArguments()[0];
                    }
                }
                throw new ClassNotFoundException("GenericMapper interface not found on the mapper class");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static class MapperClassLoader extends ClassLoader {
        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            synchronized (getClassLoadingLock(name)) {
                try {
                    byte[] bytecode = ClassPool.getDefault().get(name).toBytecode();
                    return defineClass(name, bytecode, 0, bytecode.length);
                } catch (Exception e) {
                    return super.loadClass(name, resolve);
                }
            }
        }
    }
}
