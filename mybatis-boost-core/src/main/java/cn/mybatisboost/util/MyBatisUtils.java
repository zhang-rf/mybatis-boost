package cn.mybatisboost.util;

import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class MyBatisUtils {

    private static final DefaultReflectorFactory DEFAULT_REFLECTOR_FACTORY = new DefaultReflectorFactory();

    public static MetaObject getMetaObject(Object target) {
        MetaObject metaObject;
        while ((metaObject = forObject(target)).hasGetter("h")) {
            target = metaObject.getValue("h.target");
        }
        return metaObject;
    }

    private static MetaObject forObject(Object object) {
        return MetaObject.forObject(object, SystemMetaObject.DEFAULT_OBJECT_FACTORY,
                SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY, DEFAULT_REFLECTOR_FACTORY);
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
