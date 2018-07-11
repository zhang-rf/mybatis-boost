package cn.mybatisboost.lang.provider;

import cn.mybatisboost.core.SqlProvider;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListParameterEnhancement implements SqlProvider {

    private static final Pattern PATTERN_PLACEHOLDER = Pattern.compile("(?<!')\\B\\?\\B(?!')");

    @Override
    public void replace(MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        org.apache.ibatis.session.Configuration configuration =
                (org.apache.ibatis.session.Configuration) metaObject.getValue("delegate.configuration");

        Map<Integer, List<?>> listMap =
                getLists(metaObject, boundSql.getParameterObject(), boundSql.getParameterMappings(), configuration);
        if (!listMap.isEmpty()) {
            StringBuilder sqlBuilder = new StringBuilder(boundSql.getSql());
            replacePlaceholders(listMap, sqlBuilder);
            metaObject.setValue("delegate.boundSql.sql",
                    String.format(sqlBuilder.toString(), buildNewPlaceholders(listMap)));
            refreshParameterMappings(boundSql.getParameterMappings(), configuration, listMap);
        }
    }

    private Map<Integer, List<?>> getLists(MetaObject metaObject, Object parameterObject,
                                           List<ParameterMapping> parameterMappings,
                                           org.apache.ibatis.session.Configuration configuration) {
        Map<Integer, List<?>> listMap = new HashMap<>();
        if (parameterMappings.isEmpty()) {
            if (parameterObject instanceof Map) {
                metaObject.setValue("delegate.boundSql.parameterMappings", parameterMappings = new ArrayList<>());

                Map<?, ?> map = (Map<?, ?>) parameterObject;
                if (map.size() == 2 && map.containsKey("collection") && map.containsKey("list")) {
                    listMap.put(0, (List<?>) map.get("list"));
                    parameterMappings.add(new ParameterMapping.Builder
                            (configuration, "list", Object.class).build());
                } else {
                    String key;
                    for (int i = 1; map.containsKey(key = "param" + i); i++) {
                        Object property = map.get(key);
                        if (property instanceof List) {
                            listMap.put(i - 1, (List<?>) property);
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
                if (property instanceof List) {
                    listMap.put(i, (List<?>) property);
                }
            }
            if (!listMap.isEmpty()) {
                metaObject.setValue("delegate.boundSql.parameterMappings", new ArrayList<>(parameterMappings));
            }
        }
        return listMap;
    }

    private void replacePlaceholders(Map<Integer, List<?>> listMap, StringBuilder sqlBuilder) {
        Matcher matcher = PATTERN_PLACEHOLDER.matcher(sqlBuilder.toString());
        int previousIndex = 0;
        for (Integer nextIndex : listMap.keySet()) {
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
        int index = 0;
        for (Integer i : listMap.keySet()) {
            index += i;
            String property = parameterMappings.remove(index).getProperty();

            int n = 0;
            for (Object ignored : listMap.get(i)) {
                parameterMappings.add(index++, new ParameterMapping.Builder
                        (configuration, property + '[' + n++ + ']', Object.class).build());
            }
            index--;
        }
    }
}
