package tech.rfprojects.mybatisboost.core.util;

import tech.rfprojects.mybatisboost.core.mapper.GenericMapper;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;

public abstract class MapperUtils {

    public static Class<?> getEntityTypeFromMapper(String mapperClassName) {
        try {
            return (Class<?>) Arrays.stream(Class.forName(mapperClassName).getGenericInterfaces())
                    .filter(i -> i instanceof ParameterizedType).filter(i -> i == GenericMapper.class)
                    .map(i -> (ParameterizedType) i)
                    .findAny().map(t -> t.getActualTypeArguments()[0]).orElse(null);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
