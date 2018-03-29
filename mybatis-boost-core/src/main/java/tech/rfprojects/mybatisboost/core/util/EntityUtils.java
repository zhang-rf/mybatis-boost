package tech.rfprojects.mybatisboost.core.util;

import org.apache.commons.lang3.StringUtils;
import tech.rfprojects.mybatisboost.core.adaptor.NameAdaptor;
import tech.rfprojects.mybatisboost.core.util.function.UncheckedFunction;
import tech.rfprojects.mybatisboost.core.util.function.UncheckedPredicate;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public abstract class EntityUtils {

    private static ConcurrentMap<Class<?>, String> tableNameCache = new ConcurrentHashMap<>();
    private static ConcurrentMap<Class<?>, List<Integer>> idIndexesCache = new ConcurrentHashMap<>();
    private static ConcurrentMap<Class<?>, List<String>> propertiesCache = new ConcurrentHashMap<>();
    private static ConcurrentMap<List<String>, List<String>> columnsCache = new ConcurrentHashMap<>();

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

    public static List<Integer> getIdIndexes(Class<?> type, List<String> properties) {
        return idIndexesCache.computeIfAbsent(type, k ->
                Collections.unmodifiableList(Arrays.stream(type.getDeclaredFields())
                        .filter(f -> f.isAnnotationPresent(Id.class))
                        .map(f -> properties.indexOf(f.getName()))
                        .collect(Collectors.toList())));
    }

    public static List<String> getProperties(Class<?> type) {
        return propertiesCache.computeIfAbsent(type, UncheckedFunction.of(k ->
                Collections.unmodifiableList(Arrays.stream(Introspector.getBeanInfo(type).getPropertyDescriptors())
                        .map(PropertyDescriptor::getName)
                        .filter(p -> !Objects.equals(p, "class"))
                        .collect(Collectors.toList()))));
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

    public static List<String> getColumns(Class<?> type, List<String> properties, boolean mapUnderscoreToCamelCase) {
        return columnsCache.computeIfAbsent(properties, k ->
                Collections.unmodifiableList(properties.stream().map(UncheckedFunction.of(property -> {
                    Field field = type.getDeclaredField(property);
                    if (field.isAnnotationPresent(Column.class)) {
                        return field.getAnnotation(Column.class).name();
                    } else {
                        if (mapUnderscoreToCamelCase) {
                            String[] words = StringUtils.splitByCharacterTypeCamelCase(property);
                            return Arrays.stream(words)
                                    .peek(StringUtils::uncapitalize).collect(Collectors.joining("_"));
                        } else {
                            return StringUtils.capitalize(property);
                        }
                    }
                })).collect(Collectors.toList()))
        );
    }
}
