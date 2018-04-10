package tech.rfprojects.mybatisboost.core.util;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;

public abstract class MapperUtils {

    public static Class<?> getEntityTypeFromMapper(Class<?> mapperType, String className) {
        try {
            Class<?> type = Class.forName(className);
            return (Class<?>) Arrays.stream(type.getGenericInterfaces())
                    .filter(i -> i instanceof ParameterizedType).filter(i -> i == mapperType)
                    .map(i -> (ParameterizedType) i)
                    .findAny().map(t -> t.getActualTypeArguments()[0]).orElse(null);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
