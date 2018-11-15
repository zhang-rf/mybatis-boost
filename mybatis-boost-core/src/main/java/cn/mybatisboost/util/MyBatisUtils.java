package cn.mybatisboost.util;

import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class MyBatisUtils {

    public static MetaObject getRealMetaObject(Object target) {
        MetaObject metaObject;
        while ((metaObject = SystemMetaObject.forObject(target)).hasGetter("h")) {
            target = metaObject.getValue("h.target");
        }
        return metaObject;
    }

    public static List<ParameterMapping> getParameterMappings(Configuration configuration, List<String> properties) {
        return properties.stream()
                .map(property -> new ParameterMapping.Builder(configuration, property, Object.class).build())
                .collect(Collectors.toCollection(() -> new ArrayList<>(properties.size())));
    }

    public static List<ParameterMapping> getListParameterMappings
            (Configuration configuration, List<String> properties, int size) {
        List<ParameterMapping> parameterMappings = new ArrayList<>(properties.size() * size);
        for (int i = 0; i < size; i++) {
            for (String property : properties) {
                parameterMappings.add(new ParameterMapping.Builder
                        (configuration, "list[" + i + "]." + property, Object.class).build());
            }
        }
        return parameterMappings;
    }
}
