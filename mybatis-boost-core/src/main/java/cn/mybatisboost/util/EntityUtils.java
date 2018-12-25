package cn.mybatisboost.util;

import cn.mybatisboost.core.adaptor.NameAdaptor;
import cn.mybatisboost.util.function.UncheckedFunction;
import cn.mybatisboost.util.function.UncheckedPredicate;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public abstract class EntityUtils {

    private static ConcurrentMap<Class<?>, String> tableNameCache = new ConcurrentHashMap<>();
    private static ConcurrentMap<Class<?>, String> idPropertyCache = new ConcurrentHashMap<>();
    private static ConcurrentMap<Class<?>, List<String>> propertiesCache = new ConcurrentHashMap<>();
    private static ConcurrentMap<Class<?>, List<Field>> generatedFieldCache = new ConcurrentHashMap<>();

    public static String getTableName(Class<?> type, NameAdaptor converter) {
        return tableNameCache.computeIfAbsent(type, k -> {
            if (type.isAnnotationPresent(Table.class)) {
                Table table = type.getAnnotation(Table.class);
                String catalog = table.catalog();
                if (StringUtils.isEmpty(catalog)) {
                    catalog = table.schema();
                }
                if (StringUtils.isEmpty(catalog)) {
                    return table.name();
                } else {
                    return String.format("`%s`.`%s`", catalog, table.name());
                }
            } else {
                return converter.adapt(type.getSimpleName());
            }
        });
    }

    public static String getIdProperty(Class<?> type) {
        return idPropertyCache.computeIfAbsent(type, k -> {
            try {
                return type.getDeclaredField("id").getName();
            } catch (NoSuchFieldException e) {
                return Arrays.stream(type.getDeclaredFields()).filter(f -> f.isAnnotationPresent(Id.class))
                        .findFirst().map(Field::getName)
                        .orElseThrow(() -> new NoSuchFieldError("Id property not found in " + type.getName()));
            }
        });
    }

    public static List<String> getProperties(Class<?> type) {
        return new ArrayList<>(propertiesCache.computeIfAbsent(type, UncheckedFunction.of(k ->
                Collections.unmodifiableList(Arrays.stream(Introspector.getBeanInfo(type).getPropertyDescriptors())
                        .map(PropertyDescriptor::getName)
                        .filter(p -> !Objects.equals(p, "class"))
                        .collect(Collectors.toList())))));
    }

    public static List<String> getProperties(Object entity, boolean selective) {
        Class<?> type = entity.getClass();
        List<String> properties = getProperties(type);
        if (selective) {
            properties = properties.stream()
                    .map(UncheckedFunction.of(type::getDeclaredField))
                    .peek(f -> f.setAccessible(true))
                    .filter(UncheckedPredicate.of(f -> f.get(entity) != null))
                    .map(UncheckedFunction.of(Field::getName))
                    .collect(Collectors.toList());
        }
        return properties;
    }

    public static List<String> getProperties(List<?> entities) {
        if (entities.isEmpty()) return Collections.emptyList();

        Class<?> type = entities.get(0).getClass();
        List<String> properties = getProperties(type);
        Iterator<String> iterator = properties.iterator();
        while (iterator.hasNext()) {
            try {
                Field field = type.getDeclaredField(iterator.next());
                boolean nonNull = entities.stream()
                        .anyMatch(UncheckedPredicate.of(it -> field.get(it) != null));
                if (!nonNull) iterator.remove();
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
        return properties;
    }

    public static List<Field> getGeneratedFields(Class<?> type) {
        return generatedFieldCache.computeIfAbsent(type, k ->
                Collections.unmodifiableList(Arrays.stream(type.getDeclaredFields())
                        .filter(it -> it.isAnnotationPresent(GeneratedValue.class))
                        .map(ReflectionUtils::makeAccessible).collect(Collectors.toList())));
    }
}
