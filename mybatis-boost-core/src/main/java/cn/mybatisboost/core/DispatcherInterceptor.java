package cn.mybatisboost.core;

import cn.mybatisboost.util.MyBatisUtils;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;

import java.sql.Connection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

@Intercepts(@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}))
public class DispatcherInterceptor implements Interceptor {

    private Configuration configuration;
    private List<SqlProvider> preprocessors = new CopyOnWriteArrayList<>();
    private List<SqlProvider> providers = new CopyOnWriteArrayList<>();

    public DispatcherInterceptor(Configuration configuration) {
        this.configuration = configuration;
    }

    public void appendPreprocessor(SqlProvider provider) {
        preprocessors.add(provider);
        if (provider instanceof ConfigurationAware) {
            ((ConfigurationAware) provider).setConfiguration(configuration);
        }
    }

    public void appendProvider(SqlProvider provider) {
        providers.add(provider);
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Connection connection = (Connection) invocation.getArgs()[0];
        MetaObject metaObject = MyBatisUtils.getRealMetaObject(invocation.getTarget());
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
        preprocessors.forEach(p -> p.replace(connection, metaObject, mappedStatement, boundSql));
        providers.forEach(p -> p.replace(connection, metaObject, mappedStatement, boundSql));
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
