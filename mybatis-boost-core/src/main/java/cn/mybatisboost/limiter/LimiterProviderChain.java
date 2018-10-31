package cn.mybatisboost.limiter;

import cn.mybatisboost.core.Configuration;
import cn.mybatisboost.core.ConfigurationAware;
import cn.mybatisboost.core.SqlProvider;
import cn.mybatisboost.limiter.provider.MySQL;
import cn.mybatisboost.limiter.provider.PostgreSQL;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LimiterProviderChain implements SqlProvider {

    private Configuration configuration;
    private Map<String, SqlProvider> providerMap = new HashMap<>();
    private volatile SqlProvider provider;

    public LimiterProviderChain(Configuration configuration) {
        this.configuration = configuration;
        initProviders();
    }

    protected void initProviders() {
        Arrays.asList(new MySQL(), new PostgreSQL()).forEach(p -> providerMap.put(p.toString(), p));
        for (SqlProvider provider : providerMap.values()) {
            if (provider instanceof ConfigurationAware) {
                ((ConfigurationAware) provider).setConfiguration(configuration);
            }
        }
    }

    @Override
    public void handle(Connection connection, MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        SqlProvider provider = this.provider;
        if (provider == null || configuration.isMultipleDatasource()) {
            try {
                String databaseName = connection.getMetaData().getDatabaseProductName();
                this.provider = provider = providerMap.get(databaseName);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        if (provider != null) {
            provider.handle(connection, metaObject, mappedStatement, boundSql);
        }
    }
}
