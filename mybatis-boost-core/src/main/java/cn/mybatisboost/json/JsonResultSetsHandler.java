package cn.mybatisboost.json;

import cn.mybatisboost.util.ReflectionUtils;
import cn.mybatisboost.util.function.UncheckedFunction;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.*;

import java.lang.reflect.Field;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Intercepts(@Signature(type = ResultSetHandler.class, method = "handleResultSets", args = Statement.class))
public class JsonResultSetsHandler implements Interceptor {

    private ConcurrentMap<String, Field> fieldCache = new ConcurrentHashMap<>();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        try {
            List<?> proceed = (List<?>) invocation.proceed();
            List<String> properties = JsonTypeHandler.tlProperties.get();
            if (proceed.isEmpty() || properties.isEmpty()) return proceed;

            Class<?> type = proceed.get(0).getClass();
            List<String> results = JsonTypeHandler.tlResults.get();
            for (int i = 0; i < results.size(); i++) {
                String content = results.get(i);
                if (content != null) {
                    Field field = fieldCache.computeIfAbsent(properties.get(i % properties.size()),
                            UncheckedFunction.of(key -> ReflectionUtils.makeAccessible(type.getDeclaredField(key))));
                    field.set(proceed.get(i / properties.size()),
                            JsonTypeHandler.objectMapper.readValue(content, field.getType()));
                }
            }
            return proceed;
        } finally {
            JsonTypeHandler.tlProperties.remove();
            JsonTypeHandler.tlResults.remove();
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}
