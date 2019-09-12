package cn.mybatisboost.mapper;

import cn.mybatisboost.core.Configuration;
import cn.mybatisboost.core.ConfigurationAware;
import cn.mybatisboost.core.SqlProvider;
import cn.mybatisboost.util.MyBatisUtils;
import cn.mybatisboost.util.function.UncheckedFunction;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;

import java.sql.Connection;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MapperSqlProvider implements SqlProvider {

    private Configuration configuration;
    private ConcurrentMap<Class<?>, SqlProvider> providerMap = new ConcurrentHashMap<>();

    public MapperSqlProvider(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void replace(Connection connection, MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        if (Objects.equals(boundSql.getSql(), SqlProvider.MYBATIS_BOOST)) {
            Class<?> providerType = (Class<?>)
                    MyBatisUtils.getMetaObject(mappedStatement.getSqlSource()).getValue("providerType");
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
                provider.replace(connection, metaObject, mappedStatement, boundSql);
            }
        }
    }
}
