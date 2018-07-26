package cn.mybatisboost.core.util;

import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class MyBatisUtils {

    private static ConcurrentMap<String, ParameterMapping> parameterMappingCache = new ConcurrentHashMap<>();

    public static MetaObject getRealMetaObject(Object target) {
        MetaObject metaObject;
        while ((metaObject = SystemMetaObject.forObject(target)).hasGetter("h")) {
            target = metaObject.getValue("h.target");
        }
        return metaObject;
    }

    public static List<ParameterMapping> getParameterMappings(Configuration configuration, List<String> properties) {
        List<ParameterMapping> parameterMappings = new ArrayList<>(properties.size());
        for (String property : properties) {
            ParameterMapping pm = parameterMappingCache.computeIfAbsent(property, k ->
                    new ParameterMapping.Builder(configuration, property, Object.class).build());
            parameterMappings.add(pm);
        }
        return parameterMappings;
    }
}
