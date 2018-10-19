package cn.mybatisboost.mapper;

import cn.mybatisboost.core.Configuration;
import cn.mybatisboost.core.ConfigurationAware;
import cn.mybatisboost.core.SqlProvider;
import cn.mybatisboost.core.util.MyBatisUtils;
import cn.mybatisboost.core.util.function.UncheckedFunction;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MapperInterceptor implements Interceptor {

    private Configuration configuration;
    private ConcurrentMap<Class<?>, SqlProvider> providerMap = new ConcurrentHashMap<>();

    public MapperInterceptor(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Object intercept(Invocation invocation) {
        MetaObject metaObject = MyBatisUtils.getRealMetaObject(invocation.getTarget());
        BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
        if (Objects.equals(boundSql.getSql(), SqlProvider.MYBATIS_BOOST)) {
            MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
            Class<?> providerType = (Class<?>)
                    SystemMetaObject.forObject(mappedStatement.getSqlSource()).getValue("providerType");
            SqlProvider provider = providerMap.get(providerType);
            if (provider == null) {
                synchronized (providerType) {
                    provider = providerMap.computeIfAbsent(providerType, UncheckedFunction.of(k -> {
                        SqlProvider p = (SqlProvider) providerType.newInstance();
                        if (p instanceof ConfigurationAware) {
                            ((ConfigurationAware) p).setConfiguration(configuration);
                        }
                        return p;
                    }));
                }
            }
            if (provider != null) {
                provider.replace(metaObject, mappedStatement, boundSql);
            }
        }
        return null;
    }

    @Override
    public Object plugin(Object target) {
        return target;
    }

    @Override
    public void setProperties(Properties properties) {
    }
}
