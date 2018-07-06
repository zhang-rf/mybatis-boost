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

public class RangeParameterEnhancement implements SqlProvider {

    private static final Pattern PATTERN_PLACEHOLDER = Pattern.compile("\\b\\?");

    @Override
    public void replace(MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        org.apache.ibatis.session.Configuration configuration =
                (org.apache.ibatis.session.Configuration) metaObject.getValue("delegate.configuration");

        Map<Integer, Collection<?>> collectionMap =
                getCollections(metaObject, boundSql.getParameterObject(), parameterMappings, configuration);
        if (!collectionMap.isEmpty()) {
            StringBuilder sqlBuilder = new StringBuilder(boundSql.getSql());
            replacePlaceholders(collectionMap, sqlBuilder);
            Object[] placeholders = buildPlaceholders(collectionMap);
            metaObject.setValue("delegate.boundSql.sql", String.format(sqlBuilder.toString(), placeholders));
            buildParameterMappings(parameterMappings, configuration, collectionMap);
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

    private Object[] buildPlaceholders(Map<Integer, Collection<?>> collectionMap) {
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

    private void buildParameterMappings(List<ParameterMapping> parameterMappings,
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
