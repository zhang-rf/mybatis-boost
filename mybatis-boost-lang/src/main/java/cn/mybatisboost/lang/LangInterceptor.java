package cn.mybatisboost.lang;

import cn.mybatisboost.core.Configuration;
import cn.mybatisboost.core.ConfigurationAware;
import cn.mybatisboost.core.SqlProvider;
import cn.mybatisboost.core.util.MyBatisUtils;
import cn.mybatisboost.lang.provider.*;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@Intercepts(@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}))
public class LangInterceptor implements Interceptor {

    private Configuration configuration;
    private List<SqlProvider> providerList;

    public LangInterceptor() {
        this(new Configuration());
    }

    public LangInterceptor(Configuration configuration) {
        this.configuration = configuration;
        initProviderList();
    }

    protected void initProviderList() {
        providerList = Collections.unmodifiableList(Arrays.asList(
                new RangeParametersEnhancement(), new InsertEnhancement(), new UpdateEnhancement(),
                new TableEnhancement(), new ParameterMappingsEnhancement()));
        for (SqlProvider provider : providerList) {
            if (provider instanceof ConfigurationAware) {
                ((ConfigurationAware) provider).setConfiguration(configuration);
            }
        }
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MetaObject metaObject = MyBatisUtils.getRealMetaObject(invocation.getTarget());
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
        providerList.forEach(p -> p.replace(metaObject, mappedStatement, boundSql));
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
