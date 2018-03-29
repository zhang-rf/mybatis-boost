package tech.rfprojects.mybatisboost.mapper;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import tech.rfprojects.mybatisboost.core.Configuration;
import tech.rfprojects.mybatisboost.core.ConfigurationAware;
import tech.rfprojects.mybatisboost.core.SqlProvider;
import tech.rfprojects.mybatisboost.core.util.MetaObjectUtils;
import tech.rfprojects.mybatisboost.core.util.function.UncheckedFunction;

import java.sql.Connection;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Intercepts(@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}))
public class MapperInterceptor implements Interceptor {

    private Configuration configuration;
    private ConcurrentMap<Object, SqlProvider> providerMap = new ConcurrentHashMap<>();

    public MapperInterceptor() {
        this(new Configuration());
    }

    public MapperInterceptor(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MetaObject metaObject = MetaObjectUtils.getRealMetaObject(invocation.getTarget());
        BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
        if (Objects.equals(boundSql.getSql(), SqlProvider.MYBATIS_BOOST)) {
            MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
            Class providerType = (Class)
                    SystemMetaObject.forObject(mappedStatement.getSqlSource()).getValue("providerType");
            SqlProvider provider = providerMap.computeIfAbsent(providerType, UncheckedFunction.of(k -> {
                SqlProvider p = (SqlProvider) providerType.newInstance();
                if (p instanceof ConfigurationAware) {
                    ((ConfigurationAware) p).setConfiguration(configuration);
                }
                return p;
            }));
            if (provider != null) {
                provider.replace(metaObject, mappedStatement, boundSql);
            }
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
