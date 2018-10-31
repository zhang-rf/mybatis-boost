package cn.mybatisboost;

import cn.mybatisboost.core.Configuration;
import cn.mybatisboost.core.ConfigurationAware;
import cn.mybatisboost.core.SqlProvider;
import cn.mybatisboost.util.MyBatisUtils;
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
public class MybatisBoostInterceptor implements Interceptor {

    private Configuration configuration;
    private List<SqlProvider> preprocessors = new ArrayList<>();
    private List<SqlProvider> providers = new ArrayList<>();

    public MybatisBoostInterceptor(Configuration configuration) {
        this.configuration = configuration;
    }

    public synchronized void appendPreprocessor(SqlProvider provider) {
        preprocessors.add(provider);
        if (provider instanceof ConfigurationAware) {
            ((ConfigurationAware) provider).setConfiguration(configuration);
        }
    }

    public synchronized void appendProvider(SqlProvider provider) {
        providers.add(provider);
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Connection connection = (Connection) invocation.getArgs()[0];
        MetaObject metaObject = MyBatisUtils.getRealMetaObject(invocation.getTarget());
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
        preprocessors.forEach(p -> p.handle(connection, metaObject, mappedStatement, boundSql));
        providers.forEach(p -> p.handle(connection, metaObject, mappedStatement, boundSql));
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
