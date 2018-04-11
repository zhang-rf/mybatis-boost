package tech.rfprojects.mybatisboost.core.util;

import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public abstract class MyBatisUtils {

    private static ConcurrentMap<List<String>, WeakReference<List<ParameterMapping>>> parameterMappingCache = new ConcurrentHashMap<>();

    public static MetaObject getRealMetaObject(Object target) {
        MetaObject metaObject;
        while ((metaObject = SystemMetaObject.forObject(target)).hasGetter("h")) {
            target = metaObject.getValue("h.target");
        }
        return metaObject;
    }

    public static List<ParameterMapping> getParameterMapping(Configuration configuration, List<String> properties) {
        return parameterMappingCache.compute(properties, (k, v) -> {
            if (v != null && v.get() != null) {
                return v;
            }
            return new WeakReference<>(Collections.unmodifiableList(properties.stream()
                    .map(p -> new ParameterMapping.Builder(configuration, p, Object.class).build())
                    .collect(Collectors.toList())));
        }).get();
    }
}
