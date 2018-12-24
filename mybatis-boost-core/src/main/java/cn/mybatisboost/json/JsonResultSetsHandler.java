package cn.mybatisboost.json;

import cn.mybatisboost.support.Property;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.*;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Intercepts(@Signature(type = ResultSetHandler.class, method = "handleResultSets", args = Statement.class))
public class JsonResultSetsHandler implements Interceptor {

    private ConcurrentMap<Class<?>, List<Field>> fieldCache = new ConcurrentHashMap<>();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        List<?> proceed = (List<?>) invocation.proceed();
        if (proceed.isEmpty()) return proceed;

        Class<?> type = proceed.get(0).getClass();
        List<Field> fields = fieldCache.computeIfAbsent(type,
                key -> Collections.unmodifiableList(Arrays.stream(type.getDeclaredFields())
                        .filter(it -> it.getType() == Property.class)
                        .peek(it -> it.setAccessible(true)).collect(Collectors.toList())));
        if (fields.isEmpty()) return proceed;

        for (Object object : proceed) {
            for (Field field : fields) {
                Object value = field.get(object);
                if (value != null) {
                    value = ((Property<?>) value).get();
                }
                if (value instanceof String) {
                    field.set(object, Property.of(JsonTypeHandler.objectMapper.readValue((String) value,
                            (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0])));
                }
            }
        }
        return proceed;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}
