package tech.rfprojects.mybatisboost.lang;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import tech.rfprojects.mybatisboost.core.Configuration;
import tech.rfprojects.mybatisboost.core.util.MyBatisUtils;

import java.sql.Connection;
import java.util.*;

@Intercepts(@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}))
public class LangInterceptor implements Interceptor {


    private Configuration configuration;

    public LangInterceptor() {
        this(new Configuration());
    }

    public LangInterceptor(Configuration configuration) {
        this.configuration = configuration;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MetaObject metaObject = MyBatisUtils.getRealMetaObject(invocation.getTarget());
        BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        Object parameterObject = boundSql.getParameterObject();

        MetaObject parameterMetaObject = SystemMetaObject.forObject(parameterObject);
        Map<Integer, ParameterMapping> listParameterMappingMap = new TreeMap<>();
        List<Collection<?>> collections = new ArrayList<>();
        for (int i = 0; i < parameterMappings.size(); i++) {
            ParameterMapping mapping = parameterMappings.get(i);

            Object value = parameterMetaObject.getValue(mapping.getProperty());
            Collection<?> collection = null;
            if (value instanceof Collection) {
                collection = (Collection<?>) value;
            } else if (value.getClass().isArray()) {
                collection = Arrays.asList((Object[]) value);
            }

            if (collection != null) {
                listParameterMappingMap.put(i, mapping);
                collections.add(collection);
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
                            (configuration, key = "collection-" + index + '-' + n, Object.class).build());
                    parameterMap.put(key, o);
                }
            }

            if (newParameterObject) {
                metaObject.setValue("delegate.boundSql.parameterObject", parameterMap);
                metaObject.setValue("delegate.parameterHandler.parameterObject", parameterMap);
            }
            metaObject.setValue("delegate.boundSql.sql", String.format(sqlBuilder.toString(), placeholders));
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}
