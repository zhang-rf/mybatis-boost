package cn.mybatisboost.lang;

import cn.mybatisboost.core.Configuration;
import cn.mybatisboost.core.SqlProvider;
import cn.mybatisboost.core.util.MyBatisUtils;
import cn.mybatisboost.lang.provider.CollectionArgumentEnhancement;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;

import java.sql.Connection;
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
        providerList = Collections.unmodifiableList(Collections.singletonList(new CollectionArgumentEnhancement()));
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
