package cn.mybatisboost.core;

import cn.mybatisboost.core.util.MyBatisUtils;
import cn.mybatisboost.core.util.function.UncheckedConsumer;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Intercepts(@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}))
public class MybatisInterceptor implements Interceptor {

    private Configuration configuration;
    private List<SqlProvider> preprocessors = new ArrayList<>();
    private List<Interceptor> interceptors = new ArrayList<>();

    public MybatisInterceptor(Configuration configuration) {
        this.configuration = configuration;
    }

    public synchronized void appendPreprocessor(SqlProvider provider) {
        preprocessors.add(provider);
        if (provider instanceof ConfigurationAware) {
            ((ConfigurationAware) provider).setConfiguration(configuration);
        }
    }

    public synchronized void appendInterceptor(Interceptor interceptor) {
        interceptors.add(interceptor);
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MetaObject metaObject = MyBatisUtils.getRealMetaObject(invocation.getTarget());
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
        preprocessors.forEach(p -> p.replace(metaObject, mappedStatement, boundSql));
        interceptors.forEach(UncheckedConsumer.of(i -> i.intercept(invocation)));
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
