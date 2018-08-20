package cn.mybatisboost.lang.provider;

import cn.mybatisboost.core.SqlProvider;
import cn.mybatisboost.core.util.SqlUtils;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;

public class ListParameterEnhancement implements SqlProvider {

    private static ConcurrentMap<String, Boolean> filterCache = new ConcurrentHashMap<>();

    @Override
    public void replace(MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        if (filter(mappedStatement)) {
            org.apache.ibatis.session.Configuration configuration =
                    (org.apache.ibatis.session.Configuration) metaObject.getValue("delegate.configuration");

            Map<Integer, List<?>> listMap =
                    getLists(boundSql.getParameterObject(), boundSql.getParameterMappings(), configuration);
            if (!listMap.isEmpty()) {
                StringBuilder sqlBuilder = new StringBuilder(boundSql.getSql());
                replacePlaceholders(listMap, sqlBuilder);
                metaObject.setValue("delegate.boundSql.sql",
                        String.format(sqlBuilder.toString(), buildNewPlaceholders(listMap)));
                refreshParameterMappings(boundSql.getParameterMappings(), configuration, listMap);
            }
        }
    }

    private boolean filter(MappedStatement mappedStatement) {
        return !(mappedStatement.getSqlSource() instanceof DynamicSqlSource) ||
                filterCache.computeIfAbsent(mappedStatement.getId(), k -> filter(Collections.singletonList((SqlNode)
                        SystemMetaObject.forObject(mappedStatement.getSqlSource()).getValue("rootSqlNode"))));
    }

    @SuppressWarnings("unchecked")
    private boolean filter(List<SqlNode> contents) {
        for (SqlNode content : contents) {
            if (content instanceof ForEachSqlNode) return false;
            for (Field field : content.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    if (SqlNode.class.isAssignableFrom(field.getType())) {
                        if (!filter(Collections.singletonList((SqlNode) field.get(content)))) return false;
                    } else if (Objects.equals(field.getGenericType().getTypeName(),
                            "java.util.List<org.apache.ibatis.scripting.xmltags.SqlNode>")) {
                        if (!filter((List<SqlNode>) field.get(content))) return false;
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return true;
    }

    private Map<Integer, List<?>> getLists(Object parameterObject, List<ParameterMapping> parameterMappings,
                                           org.apache.ibatis.session.Configuration configuration) {
        Map<Integer, List<?>> listMap = new HashMap<>();
        if (parameterMappings.isEmpty()) {
            if (parameterObject instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) parameterObject;
                for (int i = 1; map.containsKey("param" + i); i++) {
                    Object property = map.get("param" + i);
                    if (property instanceof List) {
                        listMap.put(i - 1, (List<?>) property);
                        parameterMappings.add(new ParameterMapping.Builder
                                (configuration, "param" + i, Object.class).build());
                    }
                }
            }
        } else {
            MetaObject parameterMetaObject = SystemMetaObject.forObject(parameterObject);
            for (int i = 0; i < parameterMappings.size(); i++) {
                try {
                    Object property = parameterMetaObject.getValue(parameterMappings.get(i).getProperty());
                    if (property instanceof List) {
                        listMap.put(i, (List<?>) property);
                    }
                } catch (Exception ignored) {
                    return Collections.emptyMap();
                }
            }
        }
        return listMap;
    }

    private void replacePlaceholders(Map<Integer, List<?>> listMap, StringBuilder sqlBuilder) {
        Matcher matcher = SqlUtils.PATTERN_PLACEHOLDER.matcher(sqlBuilder.toString());
        int previousIndex = 0;
        for (Integer nextIndex : listMap.keySet()) {
            for (int i = previousIndex; i <= nextIndex; i++) {
                if (!matcher.find()) {
                    throw new IndexOutOfBoundsException("SQL Placeholder not found");
                }
            }
            int start = matcher.start() + previousIndex;
            previousIndex = nextIndex + 1;
            sqlBuilder.replace(start, start + 1, "%s");
        }
    }

    private Object[] buildNewPlaceholders(Map<Integer, List<?>> listMap) {
        Object[] placeholders = new Object[listMap.size()];
        StringBuilder placeholderBuilder = new StringBuilder();

        int index = 0;
        for (List<?> list : listMap.values()) {
            placeholderBuilder.setLength(0);

            placeholderBuilder.append('(');
            list.forEach(i -> placeholderBuilder.append("?, "));
            placeholderBuilder.setLength(placeholderBuilder.length() - 2);
            placeholderBuilder.append(')');
            placeholders[index++] = placeholderBuilder.toString();
        }
        return placeholders;
    }

    private void refreshParameterMappings(List<ParameterMapping> parameterMappings,
                                          Configuration configuration, Map<Integer, List<?>> listMap) {
        int offset = 0;
        for (Integer i : listMap.keySet()) {
            String property = parameterMappings.remove(i + offset).getProperty();

            int n = 0;
            for (Object ignored : listMap.get(i)) {
                parameterMappings.add(i + offset++, new ParameterMapping.Builder
                        (configuration, property + '[' + n++ + ']', Object.class).build());
            }
            offset--;
        }
    }
}
