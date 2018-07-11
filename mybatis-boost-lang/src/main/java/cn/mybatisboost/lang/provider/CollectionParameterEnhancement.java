package cn.mybatisboost.lang.provider;

import cn.mybatisboost.core.SqlProvider;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CollectionParameterEnhancement implements SqlProvider {

    private static final Pattern PATTERN_PLACEHOLDER = Pattern.compile("(?<!')\\B\\?\\B(?!')");

    @Override
    public void replace(MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        org.apache.ibatis.session.Configuration configuration =
                (org.apache.ibatis.session.Configuration) metaObject.getValue("delegate.configuration");

        Map<Integer, Collection<?>> collectionMap = getCollections
                (metaObject, boundSql.getParameterObject(), boundSql.getParameterMappings(), configuration);
        if (!collectionMap.isEmpty()) {
            StringBuilder sqlBuilder = new StringBuilder(boundSql.getSql());
            replacePlaceholders(collectionMap, sqlBuilder);
            Object[] placeholders = buildNewPlaceholders(collectionMap);
            metaObject.setValue("delegate.boundSql.sql", String.format(sqlBuilder.toString(), placeholders));
            refreshParameterMappings(boundSql.getParameterMappings(), configuration, collectionMap);
        }
    }

    private Map<Integer, Collection<?>> getCollections(MetaObject metaObject, Object parameterObject,
                                                       List<ParameterMapping> parameterMappings,
                                                       org.apache.ibatis.session.Configuration configuration) {
        Map<Integer, Collection<?>> collectionMap = new HashMap<>();
        if (parameterMappings.isEmpty()) {
            if (parameterObject instanceof Map) {
                metaObject.setValue("delegate.boundSql.parameterMappings", parameterMappings = new ArrayList<>());

                Map<?, ?> map = (Map<?, ?>) parameterObject;
                if (map.size() == 2 && map.containsKey("collection") && map.containsKey("list")) {
                    collectionMap.put(0, (Collection<?>) map.get("collection"));
                    parameterMappings.add(new ParameterMapping.Builder
                            (configuration, "collection", Object.class).build());
                } else {
                    String key;
                    for (int i = 1; map.containsKey(key = "param" + i); i++) {
                        Object property = map.get(key);
                        if (property instanceof Collection) {
                            collectionMap.put(i - 1, (Collection<?>) property);
                            parameterMappings.add(new ParameterMapping.Builder
                                    (configuration, key, Object.class).build());
                        }
                    }
                }
            }
        } else {
            MetaObject parameterMetaObject = SystemMetaObject.forObject(parameterObject);
            for (int i = 0; i < parameterMappings.size(); i++) {
                Object property = parameterMetaObject.getValue(parameterMappings.get(i).getProperty());
                if (property instanceof Collection) {
                    collectionMap.put(i, (Collection<?>) property);
                }
            }
            if (!collectionMap.isEmpty()) {
                metaObject.setValue("delegate.boundSql.parameterMappings", new ArrayList<>(parameterMappings));
            }
        }
        return collectionMap;
    }

    private void replacePlaceholders(Map<Integer, Collection<?>> collectionMap, StringBuilder sqlBuilder) {
        Matcher matcher = PATTERN_PLACEHOLDER.matcher(sqlBuilder.toString());
        int previousIndex = 0;
        for (Integer nextIndex : collectionMap.keySet()) {
            for (int i = previousIndex; i <= nextIndex; i++) {
                if (!matcher.find()) {
                    throw new IndexOutOfBoundsException("SQL Placeholder not found");
                }
            }
            previousIndex = nextIndex + 1;
            int start = matcher.start();
            sqlBuilder.replace(start, start + 1, "%s");
        }
    }

    private Object[] buildNewPlaceholders(Map<Integer, Collection<?>> collectionMap) {
        Object[] placeholders = new Object[collectionMap.size()];
        StringBuilder placeholderBuilder = new StringBuilder();

        int index = 0;
        for (Collection<?> collection : collectionMap.values()) {
            placeholderBuilder.setLength(0);

            placeholderBuilder.append('(');
            collection.forEach(i -> placeholderBuilder.append("?, "));
            placeholderBuilder.setLength(placeholderBuilder.length() - 2);
            placeholderBuilder.append(')');
            placeholders[index++] = placeholderBuilder.toString();
        }
        return placeholders;
    }

    private void refreshParameterMappings(List<ParameterMapping> parameterMappings,
                                          Configuration configuration, Map<Integer, Collection<?>> collectionMap) {
        int index = 0;
        for (Integer i : collectionMap.keySet()) {
            index += i;
            String property = parameterMappings.remove(index).getProperty();

            int n = 0;
            for (Object ignored : collectionMap.get(i)) {
                parameterMappings.add(index++, new ParameterMapping.Builder
                        (configuration, property + '[' + n++ + ']', Object.class).build());
            }
            index--;
        }
    }
}
