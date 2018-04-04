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
import java.util.stream.Stream;

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
                        .filter(f -> Objects.equals(f.getName(), "id") || f.isAnnotationPresent(Id.class))
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

    public static List<String> getProperties(Object entity, boolean selective, boolean insertable, boolean updatable) {
        Class<?> type = entity.getClass();
        if (selective || insertable || updatable) {
            Stream<Field> stream = getProperties(type).stream()
                    .map(UncheckedFunction.of(type::getDeclaredField)).peek(f -> f.setAccessible(true));
            if (selective) {
                stream = stream.filter(UncheckedPredicate.of(f -> f.get(entity) != null));
            }
            if (insertable) {
                stream = stream.filter(f -> !f.isAnnotationPresent(Column.class) ||
                        f.getAnnotation(Column.class).insertable());
            }
            if (updatable) {
                stream = stream.filter(f -> !f.isAnnotationPresent(Column.class) ||
                        f.getAnnotation(Column.class).updatable());
            }
            return stream.map(Field::getName).collect(Collectors.toList());
        } else {
            return getProperties(type);
        }
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
