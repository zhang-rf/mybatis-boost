package cn.mybatisboost.core;

import cn.mybatisboost.core.util.MultipleMapKey;
import cn.mybatisboost.core.util.MyBatisUtils;
import cn.mybatisboost.core.util.function.UncheckedFunction;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;

import java.sql.Connection;
import java.sql.Statement;
import java.util.*;

@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}),
        @Signature(type = StatementHandler.class, method = "batch", args = {Statement.class}),
        @Signature(type = StatementHandler.class, method = "update", args = {Statement.class}),
        @Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class})})
public class MybatisBoostInterceptor implements Interceptor {

    private Configuration configuration;
    private List<SqlProvider> preprocessors = new ArrayList<>();
    private Map<MultipleMapKey, List<Interceptor>> interceptorMap = new HashMap<>();

    public MybatisBoostInterceptor(Configuration configuration) {
        this.configuration = configuration;
    }

    public synchronized void appendPreprocessor(SqlProvider provider) {
        preprocessors.add(provider);
        if (provider instanceof ConfigurationAware) {
            ((ConfigurationAware) provider).setConfiguration(configuration);
        }
    }

    public synchronized void appendInterceptor(Class<?> target, String methodName, Interceptor interceptor) {
        interceptorMap.computeIfAbsent(new MultipleMapKey(target, methodName), k -> new ArrayList<>()).add(interceptor);
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Class<?> type = invocation.getTarget().getClass();
        String name = invocation.getMethod().getName();
        List<Interceptor> interceptors = interceptorMap.get(interceptorMap.keySet().stream()
                .filter(it -> ((Class<?>) it.getKeys()[0]).isAssignableFrom(type) &&
                        Objects.equals(it.getKeys()[1], name))
                .findAny().orElse(null));
        if (interceptors != null) {
            MetaObject metaObject = MyBatisUtils.getRealMetaObject(invocation.getTarget());
            MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
            BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
            preprocessors.forEach(p -> p.replace(metaObject, mappedStatement, boundSql));
            return interceptors.stream().map(UncheckedFunction.of(i -> i.intercept(invocation)))
                    .filter(Objects::nonNull).findAny().orElse(invocation.proceed());
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
