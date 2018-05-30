package cn.mybatisboost.lang.provider;

import cn.mybatisboost.core.SqlProvider;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.util.*;

public class RangeParametersEnhancement implements SqlProvider {

    @Override
    @SuppressWarnings("unchecked")
    public void replace(MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        Object parameterObject = boundSql.getParameterObject();

        MetaObject parameterMetaObject = SystemMetaObject.forObject(parameterObject);
        Map<Integer, ParameterMapping> listParameterMappingMap = new TreeMap<>();
        List<Collection<?>> collections = new ArrayList<>();
        if (parameterMappings.size() == 1 && !(parameterObject instanceof Map) &&
                !parameterMetaObject.hasGetter(parameterMappings.get(0).getProperty())) {
            if (parameterMetaObject.isCollection()) {
                listParameterMappingMap.put(0, parameterMappings.get(0));
                collections.add((Collection<?>) parameterObject);
            } else {
                return;
            }
        } else {
            for (int i = 0; i < parameterMappings.size(); i++) {
                ParameterMapping mapping = parameterMappings.get(i);
                Object value = parameterMetaObject.getValue(mapping.getProperty());
                if (value instanceof Collection) {
                    listParameterMappingMap.put(i, mapping);
                    collections.add((Collection<?>) value);
                }
            }
        }

        if (!listParameterMappingMap.isEmpty()) {
            Map parameterMap;
            boolean newParameterObject = false;
            if (parameterObject instanceof Map) {
                parameterMap = (Map) parameterObject;
            } else {
                newParameterObject = true;
                parameterMap = new HashMap<>();
                parameterMappings.forEach((m) ->
                        parameterMap.put(m.getProperty(), parameterMetaObject.getValue(m.getProperty())));
            }

            StringBuilder sqlBuilder = new StringBuilder(boundSql.getSql());
            Iterator<Integer> indexIterator = listParameterMappingMap.keySet().iterator();
            int lastIndex = 0, previousIndex = 0;
            while (indexIterator.hasNext()) {
                int nextIndex = indexIterator.next();
                for (int i = previousIndex; i <= nextIndex; i++) {
                    lastIndex = sqlBuilder.indexOf("?", lastIndex + 1);
                }
                previousIndex = nextIndex + 1;
                sqlBuilder.replace(lastIndex, lastIndex + 1, "%s");
            }

            Object[] placeholders = new Object[listParameterMappingMap.size()];
            StringBuilder placeholderBuilder = new StringBuilder();
            for (int i = 0; i < listParameterMappingMap.size(); i++) {
                placeholderBuilder.setLength(0);

                placeholderBuilder.append('(');
                collections.get(i).forEach((c) -> placeholderBuilder.append("?, "));
                placeholderBuilder.setLength(placeholderBuilder.length() - 2);
                placeholderBuilder.append(')');
                placeholders[i] = placeholderBuilder.toString();
            }

            org.apache.ibatis.session.Configuration configuration =
                    (org.apache.ibatis.session.Configuration) metaObject.getValue("delegate.configuration");
            Iterator<ParameterMapping> mappingIterator = listParameterMappingMap.values().iterator();
            for (int i = 0; i < listParameterMappingMap.size(); i++) {
                ParameterMapping mapping = mappingIterator.next();
                int index = parameterMappings.indexOf(mapping);
                parameterMappings.remove(index);

                int n = 0;
                for (Object o : collections.get(i)) {
                    String key;
                    parameterMappings.add(index + n++, new ParameterMapping.Builder
                            (configuration, key = "_collection-" + index + '-' + n, Object.class).build());
                    parameterMap.put(key, o);
                }
            }

            if (newParameterObject) {
                metaObject.setValue("delegate.boundSql.parameterObject", parameterMap);
                metaObject.setValue("delegate.parameterHandler.parameterObject", parameterMap);
            }
            metaObject.setValue("delegate.boundSql.sql", String.format(sqlBuilder.toString(), placeholders));
        }
    }
}
