package cn.mybatisboost.lang;

import cn.mybatisboost.core.Configuration;
import cn.mybatisboost.core.ConfigurationAware;
import cn.mybatisboost.core.SqlProvider;
import cn.mybatisboost.lang.provider.*;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LangProviderChain implements SqlProvider {

    private Configuration configuration;
    private List<SqlProvider> providers;

    public LangProviderChain(Configuration configuration) {
        this.configuration = configuration;
        initProviders();
    }

    protected void initProviders() {
        providers = Collections.unmodifiableList(Arrays.asList(new InsertEnhancement(), new UpdateEnhancement(),
                new TableEnhancement(), new ListParameterEnhancement(), new ParameterMappingEnhancement(),
                new NullEnhancement()));
        for (SqlProvider provider : providers) {
            if (provider instanceof ConfigurationAware) {
                ((ConfigurationAware) provider).setConfiguration(configuration);
            }
        }
    }

    @Override
    public void replace(Connection connection, MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        providers.forEach(p -> p.replace(connection, metaObject, mappedStatement, boundSql));
    }
}
