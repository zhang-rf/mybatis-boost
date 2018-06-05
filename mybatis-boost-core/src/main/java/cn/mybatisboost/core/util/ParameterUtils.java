package cn.mybatisboost.core.util;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public abstract class ParameterUtils {

    public static Map<String, Object> buildParameterMap(List<String> properties, Class<?> type, List<?> entities) {
        Map<String, Object> parameterMap = new HashMap<>(properties.size() * entities.size());
        List<Method> readMethods = new ArrayList<>();

        try {
            PropertyDescriptor[] descriptors = Introspector.getBeanInfo(type).getPropertyDescriptors();
            for (String property : properties) {
                readMethods.add(Arrays.stream(descriptors).filter(d -> Objects.equals(d.getName(), property))
                        .findAny().orElseThrow(NoSuchFieldError::new).getReadMethod());
            }
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < entities.size(); i++) {
            try {
                for (int n = 0; n < readMethods.size(); n++) {
                    parameterMap.put(properties.get(n) + i, readMethods.get(n).invoke(entities.get(i)));
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        parameterMap.put("list", entities);
        return parameterMap;
    }
}
